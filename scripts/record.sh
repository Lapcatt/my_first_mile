#!/usr/bin/env bash
# 에뮬레이터 화면 녹화 (시연 영상용). Enter를 누르면 멈추고 captures/에 저장한다.
# 사용: scripts/record.sh [이름]   (최대 180초, screenrecord 제한)
set -eu
source "$(dirname "$0")/env.sh"
require_device

name="${1:-demo}_$(date +%Y%m%d-%H%M%S).mp4"
remote="/sdcard/$name"
mkdir -p "$ROOT/captures"

"$ADB" shell screenrecord --time-limit 180 "$remote" &
rec_pid=$!
echo "녹화 중... Enter를 누르면 멈춥니다 (최대 180초)"
read -r

# Ctrl+C로 adb를 죽이면 mp4가 깨질 수 있어서, 기기 쪽 screenrecord에 SIGINT를 보내 정상 종료시킨다
"$ADB" shell pkill -INT screenrecord || true
wait "$rec_pid" || true
sleep 1

"$ADB" pull "$remote" "$ROOT/captures/$name"
"$ADB" shell rm "$remote"
echo "저장: captures/$name"
