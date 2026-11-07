# Pleos Connect Emulator 설치 (macOS Apple Silicon)

출처 (2026-11-07 확인):
- https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator
- https://document.pleos.ai/docs/connect/guide/getting-started/application-development/configure-pleos-connect-emulator
- https://document.pleos.ai/docs/connect/guide/getting-started/application-development/use-vehicle-emulator

## 1. 시스템 이미지 설치
이전 버전 Pleos Connect Emulator가 있으면 먼저 삭제.

1. Android Studio → Tools → SDK Manager → **SDK Update Sites** 탭
2. 왼쪽 위 `+`
   - Name: `Pleos Connect System Image`
   - URL (Mac): `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
3. OK
4. **SDK Platforms** 탭 → 아래 **Show Package Details** 체크
5. Android API 34 아래 Pleos Connect 시스템 이미지 체크 → OK → 설치 → Finish
6. Android Studio 재시작

## 2. AVD 만들기
1. Tools → Device Manager → Create device
2. Form Factor: `Automotive` → Device: `Pleos Connect v2.1.0` → Next
3. System image: API 34 → `Pleos Connect v2.1.0 APIs`
4. Advanced Settings
   - Startup > Default boot: **Cold** (필수. Quick boot면 VHAL이 꺼짐)
   - Emulated Performance > RAM: **4 GB**
5. Finish → Device Manager에서 실행

## 3. Virtual Console
```sh
adb forward tcp:8088 tcp:8088
```
브라우저에서 http://127.0.0.1:8088/

조작 가능: 기어, 브레이크, 조향각, 크루즈, 안전벨트·경고등 등. (차속 카드는 없음)

안 열릴 때: 에뮬레이터 실행 중인지 → `adb devices` → forward 다시 → 8088 포트 충돌 확인.

## 4. 참고
- 공식 요구사항에 GPU가 NVIDIA/AMD만 적혀 있음. Apple Silicon 언급 없음 → 실제로 돌려 보고 결과를 여기 기록
- 기본 언어 한국어. 영어: 차량 설정 > 일반 설정 > 언어/Language
- 마이크(STT 테스트): Extended Controls > Microphone에서 오디오 입력 켜기
- 블루투스 동글 패스스루 미지원

## 설치 결과 기록
- [ ] 시스템 이미지 설치
- [ ] AVD 생성·부팅 (부팅 시간, 버벅임 여부)
- [ ] `adb devices`에 잡힘
- [ ] 템플릿 앱 설치·실행
- [ ] Virtual Console 열림, 조향각 바꾸기 동작
