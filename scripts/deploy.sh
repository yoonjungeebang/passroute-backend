#!/bin/bash
set -euo pipefail

PROJECT_DIR="/home/ubuntu"
UPSTREAM_CONF="$PROJECT_DIR/nginx/conf.d/upstream.conf"
MAX_RETRIES=40
RETRY_INTERVAL=3
DOCKER_IMAGE="${1:?DOCKER_IMAGE 인자가 필요합니다}"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

DEPLOY_SUCCESS=false
CLEANUP_DONE=false

cleanup_all() {
    if [ "$DEPLOY_SUCCESS" = true ] || [ "$CLEANUP_DONE" = true ]; then
        return
    fi
    CLEANUP_DONE=true
    log "ERROR: 배포 실패. 모든 컨테이너를 정리합니다."
    cd "$PROJECT_DIR"
    docker compose --profile blue --profile green down 2>/dev/null || true
    rm -f "$UPSTREAM_CONF" 2>/dev/null || true
    docker image prune -f 2>/dev/null || true
}

trap 'cleanup_all; exit 1' SIGTERM SIGINT
trap cleanup_all EXIT

# .env 확인 (CD 파이프라인에서 동기화됨)
if [ ! -f "$PROJECT_DIR/.env" ]; then
    log "ERROR: .env 파일이 없습니다. CD 파이프라인에서 동기화되었는지 확인하세요."
    exit 1
fi
log ".env 확인 완료"

get_active_color() {
    if [ -f "$UPSTREAM_CONF" ]; then
        if grep -q "passroute-blue" "$UPSTREAM_CONF"; then
            echo "blue"
        elif grep -q "passroute-green" "$UPSTREAM_CONF"; then
            echo "green"
        else
            echo "none"
        fi
    else
        echo "none"
    fi
}

ACTIVE=$(get_active_color)

if [ "$ACTIVE" = "blue" ]; then
    NEW_COLOR="green"
    OLD_COLOR="blue"
elif [ "$ACTIVE" = "green" ]; then
    NEW_COLOR="blue"
    OLD_COLOR="green"
else
    NEW_COLOR="blue"
    OLD_COLOR=""
    log "최초 배포 감지. blue로 시작합니다."

    # 기존 단일 컨테이너(passroute-dev) 정리 — 포트 8080 충돌 방지
    if docker ps -a --format '{{.Names}}' | grep -q "^passroute-dev$"; then
        log "기존 컨테이너(passroute-dev) 제거"
        docker stop passroute-dev 2>/dev/null || true
        docker rm passroute-dev 2>/dev/null || true
    fi
fi

NEW_CONTAINER="passroute-${NEW_COLOR}"

log "현재 활성: ${ACTIVE}, 새로 배포할 색상: ${NEW_COLOR}"

# 새 이미지 Pull
log "이미지 Pull: ${DOCKER_IMAGE}"
docker pull "$DOCKER_IMAGE"

# 새 컨테이너 기동
log "${NEW_COLOR} 컨테이너 시작"
cd "$PROJECT_DIR"

docker compose --profile "$NEW_COLOR" stop "$NEW_COLOR" 2>/dev/null || true
docker compose --profile "$NEW_COLOR" rm -f "$NEW_COLOR" 2>/dev/null || true

mkdir -p "$PROJECT_DIR/nginx/conf.d"

# upstream.conf가 없으면 초기 파일 생성 (nginx 기동용)
if [ ! -f "$UPSTREAM_CONF" ]; then
    cat > "$UPSTREAM_CONF" << EOF
upstream app {
    server passroute-${NEW_COLOR}:8080;
}
EOF
fi

docker compose --profile "$NEW_COLOR" up -d redis nginx "$NEW_COLOR"

# 헬스체크
log "헬스체크 시작 (최대 ${MAX_RETRIES}회, 간격 ${RETRY_INTERVAL}초)"

for i in $(seq 1 $MAX_RETRIES); do
    CONTAINER_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$NEW_CONTAINER" 2>/dev/null || echo "")

    if [ -n "$CONTAINER_IP" ]; then
        STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${CONTAINER_IP}:8080/api" 2>/dev/null || echo "000")
    else
        STATUS_CODE="000"
    fi

    if [ "$STATUS_CODE" = "200" ] || [ "$STATUS_CODE" = "302" ] || [ "$STATUS_CODE" = "403" ] || [ "$STATUS_CODE" = "401" ]; then
        log "헬스체크 성공 (HTTP ${STATUS_CODE}) - ${i}/${MAX_RETRIES}"
        break
    fi

    if [ "$i" -eq "$MAX_RETRIES" ]; then
        log "ERROR: 헬스체크 실패. 모든 컨테이너를 정리합니다."
        exit 1
    fi

    log "헬스체크 대기중... (HTTP ${STATUS_CODE}) - ${i}/${MAX_RETRIES}"
    sleep $RETRY_INTERVAL
done

# nginx upstream 전환
log "nginx upstream을 ${NEW_COLOR}으로 전환"
cat > "$UPSTREAM_CONF" << EOF
upstream app {
    server passroute-${NEW_COLOR}:8080;
}
EOF

docker exec passroute-nginx nginx -s reload
log "nginx reload 완료"

DEPLOY_SUCCESS=true

# 이전 컨테이너 중지 (삭제하지 않음 — 롤백 대비)
if [ -n "$OLD_COLOR" ]; then
    log "이전 컨테이너(${OLD_COLOR}) 중지 (롤백 대비 유지)"
    sleep 5
    docker compose --profile "$OLD_COLOR" stop "$OLD_COLOR" || true
fi

# 정리
docker image prune -f || true
log "배포 완료: ${NEW_COLOR} 활성화"
