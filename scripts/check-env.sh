#!/usr/bin/env bash
# 개발 환경 점검: SDK 도구, JDK, Pleos 에뮬레이터 이미지·AVD, 기기 연결, Virtual Console 포워딩, Vehicle SDK
# 읽기만 하고 아무것도 바꾸지 않는다.
set -u
source "$(dirname "$0")/env.sh"

ok()   { echo "  ✓ $*"; }
bad()  { echo "  ✗ $*"; }
info() { echo "    $*"; }

echo "[SDK]"
[ -d "$SDK_DIR" ]       && ok "SDK: $SDK_DIR"       || bad "SDK 없음: $SDK_DIR"
[ -x "$ADB" ]           && ok "adb"                 || bad "adb 없음 (SDK Manager > Platform-Tools)"
[ -x "$EMULATOR" ]      && ok "emulator"            || bad "emulator 없음 (SDK Manager > Android Emulator)"

echo "[JDK]"
if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  ok "JAVA_HOME: $("$JAVA_HOME/bin/java" -version 2>&1 | head -1)"
else
  bad "JAVA_HOME 없음 (Android Studio 설치 확인)"
fi

echo "[Pleos 에뮬레이터 이미지] (API 34)"
# 이미지 디렉터리 이름은 [미확인]. API 34 아래에 있는 것을 전부 보여준다.
if [ -d "$SDK_DIR/system-images/android-34" ]; then
  ok "android-34 이미지 있음"
  for d in "$SDK_DIR"/system-images/android-34/*/*; do info "$(basename "$(dirname "$d")")/$(basename "$d")"; done
else
  bad "android-34 이미지 없음 → docs/02-emulator-setup.md 1단계"
fi

echo "[AVD]"
if [ -x "$EMULATOR" ]; then
  avds="$("$EMULATOR" -list-avds 2>/dev/null)"
  if [ -n "$avds" ]; then
    ok "AVD 목록"
    echo "$avds" | while read -r a; do info "$a"; done
  else
    bad "AVD 없음 → docs/02-emulator-setup.md 2단계"
  fi
fi

echo "[기기 연결]"
if [ -x "$ADB" ]; then
  devices="$("$ADB" devices | tail -n +2 | grep -v '^$')"
  if [ -n "$devices" ]; then
    ok "연결됨"
    echo "$devices" | while read -r d; do info "$d"; done
    sdk_level="$("$ADB" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')"
    info "API level: ${sdk_level:-?}"
  else
    bad "연결된 기기 없음 (에뮬레이터 실행 중인지 확인)"
  fi

  echo "[Virtual Console 포워딩]"
  if "$ADB" forward --list 2>/dev/null | grep -q 'tcp:8088'; then
    ok "tcp:8088 포워딩됨 → http://127.0.0.1:8088/"
  else
    bad "포워딩 없음 → scripts/vc.sh"
  fi
fi

echo "[Vehicle SDK]"
cache="$HOME/.gradle/caches/modules-2/files-2.1/ai.pleos.playground/Vehicle"
if [ -d "$cache" ]; then
  ok "Gradle 캐시: $(ls "$cache" | tr '\n' ' ')"
else
  bad "Gradle 캐시에 없음 (Android Studio에서 Sync)"
fi
