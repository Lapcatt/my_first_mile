#!/usr/bin/env bash
# Virtual Console 열기: 에뮬레이터 8088 포트를 맥으로 포워딩하고 브라우저로 연다.
set -eu
source "$(dirname "$0")/env.sh"
require_device

"$ADB" forward tcp:8088 tcp:8088
echo "Virtual Console: http://127.0.0.1:8088/"
open "http://127.0.0.1:8088/"
