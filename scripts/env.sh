# 다른 스크립트가 source 해서 쓰는 공통 설정. 직접 실행하지 않는다.

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# SDK 경로: local.properties → ANDROID_HOME → macOS 기본 위치
SDK_DIR="$(grep '^sdk.dir=' "$ROOT/local.properties" 2>/dev/null | cut -d= -f2-)"
SDK_DIR="${SDK_DIR:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"

ADB="$SDK_DIR/platform-tools/adb"
EMULATOR="$SDK_DIR/emulator/emulator"

# Gradle은 Android Studio 내장 JDK로 돌린다 (시스템 java 버전과 무관하게)
STUDIO_JBR="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
if [ -z "${JAVA_HOME:-}" ] && [ -d "$STUDIO_JBR" ]; then
  export JAVA_HOME="$STUDIO_JBR"
fi

APP_ID="io.github.lapcatt.myfirstmile"

require_device() {
  if ! "$ADB" get-state >/dev/null 2>&1; then
    echo "연결된 에뮬레이터/기기가 없습니다. 에뮬레이터를 먼저 켜세요." >&2
    exit 1
  fi
}
