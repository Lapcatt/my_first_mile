# Pleos 플랫폼 노트

확인한 사실만 적는다. 각 항목에 출처와 확인 날짜를 남긴다.
모르는 건 [05-open-questions.md](05-open-questions.md)로.

## Vehicle SDK
- 의존성: `ai.pleos.playground:Vehicle:2.3.2` (2026-11-07, 빌드에서 resolve 성공)
  - 공식 문서 getting-started에는 `2.3.1`로 적혀 있지만 2.3.1은 resolve 실패 (2026-11-07)
  - 같이 내려오는 것: `vehicle-aidl:1.1.1`, `vehicle-data:1.1.11`
- Maven: `https://nexus-playground.pleos.ai/repository/maven-releases/`
- 사용 예정 API: SteeringWheel(조향각), TurnSignal, Brake(페달), DrivingMode(크루즈), Safety(Telltales) (2026-11-02)
- 비동기 콜백. 콜백 끝나기 전 `release()` 금지. 사용 전 `checkXXXCapabilities` (2026-11-02)
- `VehicleSpeedListener` 제거됨 → 차속은 AAOS `CarPropertyManager` (2026-11-02)
- 차선·후측방 API 없음. ADAS SDK는 현재 목록에서 빠짐 (2026-11-02)
- 공식 문서: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro

## TTS / STT
- ON_DEVICE 모드면 네트워크·앱 등록 불필요 (2026-11-02)
- 음성 규칙: 150~200ms 선행 묵음, 오디오 포커스 요청, Gleo 음성 명령 시 즉시 중단
- TTS 문서: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro
- 오디오 포커스: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development

## 에뮬레이터
- Pleos Connect Emulator v2.1.0, API 34. 설치 절차는 [02-emulator-setup.md](02-emulator-setup.md) (2026-11-07)
- 앱 minSdk 28 이상 필수 (차량 보안 서명키 교체·lineage 때문) (2026-11-07)
- Gradle 8.0 이상 (2026-11-07)
- Cold boot 필수. Quick boot면 VHAL 비활성 (2026-11-07)
- Virtual Console: `adb forward tcp:8088 tcp:8088` → http://127.0.0.1:8088/
  기어·조향각·브레이크·방향지시등·크루즈·경고등 조작 가능, 차속 카드 없음 (2026-11-02)
  가이드: https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide

## 주행 규제
- 테스트: `adb shell cmd hmg_car_service --service drivingstate driving_regulation on|off` (2026-11-02)
- 기어 ≠ P일 때 화면 조작·긴 텍스트 금지, 상세 리포트는 P단에서만

## 공모전
- 심사·시상: https://appdevchallenge.auto-hmg.io/guide/judging-awards
- 지원·제출: https://appdevchallenge.auto-hmg.io/guide/submission
- 1차 가산점 APK 경로: `apk/팀명_앱이름_release.apk`, README에 앱 소개·실행 방법·테스트 환경
