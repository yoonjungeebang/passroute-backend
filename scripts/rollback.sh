#!/bin/bash
set -euo pipefail

PROJECT_DIR="/home/ubuntu"
UPSTREAM_CONF="$PROJECT_DIR/nginx/conf.d/upstream.conf"
PREVIOUS_IMAGE_FILE="$PROJECT_DIR/.previous_image"
CURRENT_SLOT_FILE="$PROJECT_DIR/.current_slot"
MAX_RETRIES=40
RETRY_INTERVAL=3

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 롤백 정보 확인
if [ ! -f "$PREVIOUS_IMAGE_FILE" ] || [ ! -f "$CURRENT_SLOT_FILE" ]; then
    log "ERROR: 롤백 불가 - 이전 배포 정보 없음"
    exit 1
fi

PREVIOUS_IMAGE=$(cat "$PREVIOUS_IMAGE_FILE")
CURRENT_SLOT=$(cat "$CURRENT_SLOT_FILE")

if [ -z "$PREVIOUS_IMAGE" ] || [ -z "$CURRENT_SLOT" ]; then
    log "ERROR: 롤백 불가 - 이전 배포 정보가 비어있음"
    exit 1
fi

# 롤백 대상 슬롯 결정 (현재의 반대)
if [ "$CURRENT_SLOT" = "blue" ]; then
    ROLLBACK_TO="green"
else
    ROLLBACK_TO="blue"
fi

log "롤백 시작: $CURRENT_SLOT → $ROLLBACK_TO ($PREVIOUS_IMAGE)"

cd "$PROJECT_DIR"

# 롤백 실패 시 롤백 컨테이너 정리
CLEANUP_TARGET="$ROLLBACK_TO"
cleanup() {
    if [ -n "$CLEANUP_TARGET" ]; then
        log "롤백 실패 → $CLEANUP_TARGET 컨테이너 정리 중..."
        docker compose --profile "$CLEANUP_TARGET" stop "$CLEANUP_TARGET" 2>/dev/null || true
        docker compose --profile "$CLEANUP_TARGET" rm -f "$CLEANUP_TARGET" 2>/dev/null || true
    fi
}
trap cleanup EXIT

# 이전 이미지 Pull
log "이전 이미지 Pull: $PREVIOUS_IMAGE"
docker pull "$PREVIOUS_IMAGE"

# .env의 DOCKER_IMAGE를 이전 이미지로 설정
sed -i "s|^DOCKER_IMAGE=.*|DOCKER_IMAGE=$PREVIOUS_IMAGE|" "$PROJECT_DIR/.env"

# 롤백 컨테이너 시작
docker compose --profile "$ROLLBACK_TO" up -d "$ROLLBACK_TO" || true

# 헬스체크
log "헬스체크 시작 (최대 ${MAX_RETRIES}회, 간격 ${RETRY_INTERVAL}초)"
for i in $(seq 1 $MAX_RETRIES); do
    CONTAINER_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "passroute-${ROLLBACK_TO}" 2>/dev/null || echo "")

    if [ -n "$CONTAINER_IP" ]; then
        STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${CONTAINER_IP}:8080/api" 2>/dev/null || echo "000")
    else
        STATUS_CODE="000"
    fi

    if [ "$STATUS_CODE" = "200" ] || [ "$STATUS_CODE" = "302" ] || [ "$STATUS_CODE" = "403" ] || [ "$STATUS_CODE" = "401" ]; then
        log "헬스체크 성공 (HTTP ${STATUS_CODE})"
        break
    fi

    if [ "$i" -eq "$MAX_RETRIES" ]; then
        log "ERROR: 롤백 헬스체크 실패. 컨테이너 로그:"
        docker logs "passroute-${ROLLBACK_TO}" --tail 50 2>&1 || true
        exit 1
    fi

    log "헬스체크 대기중... (HTTP ${STATUS_CODE}) - ${i}/${MAX_RETRIES}"
    sleep $RETRY_INTERVAL
done

# nginx upstream 전환
mkdir -p "$PROJECT_DIR/nginx/conf.d"
cat > "$UPSTREAM_CONF" << EOF
upstream app {
    server passroute-${ROLLBACK_TO}:8080;
}
EOF

# nginx 기동 또는 reload
if docker ps --format '{{.Names}}' | grep -q "^passroute-nginx$"; then
    docker exec passroute-nginx nginx -s reload
else
    docker compose up -d nginx
fi
log "nginx upstream을 ${ROLLBACK_TO}으로 전환 완료"

# 문제 컨테이너 정리
docker compose --profile "$CURRENT_SLOT" stop "$CURRENT_SLOT" 2>/dev/null || true
docker compose --profile "$CURRENT_SLOT" rm -f "$CURRENT_SLOT" 2>/dev/null || true

# 롤백 성공 → cleanup 비활성화
CLEANUP_TARGET=""

# 슬롯 정보 업데이트
echo "$ROLLBACK_TO" > "$CURRENT_SLOT_FILE"

docker image prune -f || true
log "롤백 완료: ${ROLLBACK_TO} 활성화"
