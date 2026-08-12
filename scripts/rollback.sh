#!/bin/bash
set -euo pipefail

PROJECT_DIR="$HOME"
UPSTREAM_CONF="$PROJECT_DIR/nginx/conf.d/upstream.conf"
MAX_RETRIES=20
RETRY_INTERVAL=3

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# 현재 활성 색상 판별
if grep -q "passroute-blue" "$UPSTREAM_CONF"; then
    CURRENT="blue"
    ROLLBACK_TO="green"
elif grep -q "passroute-green" "$UPSTREAM_CONF"; then
    CURRENT="green"
    ROLLBACK_TO="blue"
else
    log "ERROR: 현재 활성 컨테이너를 판별할 수 없습니다."
    exit 1
fi

ROLLBACK_CONTAINER="passroute-${ROLLBACK_TO}"

# 롤백 대상 컨테이너 존재 확인
if ! docker ps -a --format '{{.Names}}' | grep -q "^${ROLLBACK_CONTAINER}$"; then
    log "ERROR: 롤백 대상 컨테이너(${ROLLBACK_CONTAINER})가 존재하지 않습니다."
    exit 1
fi

log "롤백 시작: ${CURRENT} → ${ROLLBACK_TO}"

# 이전 컨테이너 재시작
cd "$PROJECT_DIR"
docker compose --profile "$ROLLBACK_TO" start "$ROLLBACK_TO"

# 헬스체크
log "헬스체크 시작"
for i in $(seq 1 $MAX_RETRIES); do
    CONTAINER_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$ROLLBACK_CONTAINER" 2>/dev/null || echo "")

    if [ -n "$CONTAINER_IP" ]; then
        STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${CONTAINER_IP}:8080/api" 2>/dev/null || echo "000")
    else
        STATUS_CODE="000"
    fi

    if [ "$STATUS_CODE" = "200" ] || [ "$STATUS_CODE" = "403" ] || [ "$STATUS_CODE" = "401" ]; then
        log "헬스체크 성공 (HTTP ${STATUS_CODE})"
        break
    fi

    if [ "$i" -eq "$MAX_RETRIES" ]; then
        log "ERROR: 롤백 헬스체크 실패."
        exit 1
    fi

    log "헬스체크 대기중... (HTTP ${STATUS_CODE}) - ${i}/${MAX_RETRIES}"
    sleep $RETRY_INTERVAL
done

# nginx upstream 전환
cat > "$UPSTREAM_CONF" << EOF
upstream app {
    server passroute-${ROLLBACK_TO}:8080;
}
EOF

docker exec passroute-nginx nginx -s reload
log "nginx upstream을 ${ROLLBACK_TO}으로 전환 완료"

# 문제 컨테이너 중지
sleep 5
docker compose --profile "$CURRENT" stop "$CURRENT"

log "롤백 완료: ${ROLLBACK_TO} 활성화"
