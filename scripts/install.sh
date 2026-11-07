#!/usr/bin/env bash
# 디버그 빌드 → 에뮬레이터에 설치 → 앱 실행
set -eu
source "$(dirname "$0")/env.sh"
require_device

cd "$ROOT"
./gradlew :app:installDebug
"$ADB" shell am start -n "$APP_ID/.MainActivity"
