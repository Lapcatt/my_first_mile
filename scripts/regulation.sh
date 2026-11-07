#!/usr/bin/env bash
# 주행 규제 켜기/끄기 (기어 ≠ P일 때 화면 제한 동작 테스트용)
# 사용: scripts/regulation.sh on|off
set -eu
source "$(dirname "$0")/env.sh"

case "${1:-}" in
  on|off) ;;
  *) echo "사용: $0 on|off" >&2; exit 1 ;;
esac

require_device
"$ADB" shell cmd hmg_car_service --service drivingstate driving_regulation "$1"
echo "driving_regulation $1"
