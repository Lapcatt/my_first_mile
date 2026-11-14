# My First Mile

**옆자리 연수 강사처럼, 차가 초보 운전자의 조작을 보고 짧은 말로 알려 주는 Pleos Connect 앱.**

주행 중에는 화면 조작 없이 짧은 음성으로 깜빡이·핸들·브레이크·주행 보조 조작 습관을 코칭하고,
정차(P단) 후에는 차 화면에서 주행 리포트를 보여 주는 것을 목표로 합니다.

> 현대자동차그룹 차량용 앱 개발 공모전(Pleos App Market) 출품작입니다.
> 지금 저장소는 1차 접수 시점(2026-11-15)의 프로토타입입니다.

## 현재 상태 (2026-11-15)

| 구분 | 내용 | 상태 |
|---|---|---|
| 차량 신호 수신 | 조향각, 방향지시등, 브레이크, 기어, 크루즈 상태를 Vehicle SDK로 수신 | ✅ 동작 |
| 백그라운드 수신 | 내비 앱이 앞 화면에 있어도 포그라운드 서비스에서 계속 수신 | ✅ 동작 |
| 음성 안내 | Pleos TextToSpeech SDK (ON_DEVICE, ko-KR). 말하는 중에는 끊지 않고 다음 문장을 대기 | ✅ 동작 |
| 첫 코칭 규칙 | 깜빡이 없이 핸들을 크게 꺾으면 "방향지시등을 먼저 켜 주세요." | ✅ 동작 + 단위 테스트 |
| 나머지 코칭 규칙·P단 리포트 | 아래 기능 계획 참고 | 개발 예정 |

지금 앱 화면은 수신한 차량 신호를 보여 주는 **확인용 화면**입니다. 주행 중 화면 제한에 맞춘 최종 화면과 P단 리포트 화면은 개발 예정입니다.

## 기능 계획

기능마다 태그를 붙여, 실제 차량 신호로 동작하는 것과 가상 입력을 쓰는 것을 구분합니다.

- **[실제]** Pleos SDK의 실제 차량 신호로 동작
- **[시연]** 코칭 로직과 음성은 실제이고, 공모전 기간에 제공되지 않는 입력(차선·옆 차·실선)만 가상 데이터. 화면에 항상 "시뮬레이션 데이터" 배지를 표시
- **[확장]** 지금은 구현할 수 없어 문서로만 다룸

| 기능 | 태그 | 입력 신호 | 안내 예시 |
|---|---|---|---|
| 깜빡이 없이 핸들 크게 꺾음 ✅ | [실제] | 조향각, 방향지시등, 기어 | "방향지시등을 먼저 켜 주세요." |
| 깜빡이 켠 채 계속 직진 | [실제] | 방향지시등, 조향각 | "방향지시등이 켜져 있어요." |
| 핸들 흔들림 | [실제] | 조향각 시계열, 차속 | "핸들이 흔들려요, 시선을 멀리 두세요." |
| 잦은 제동 | [실제] | 브레이크 페달, 차속 | "앞차와 거리를 여유 있게 두세요." |
| 주행 보조 해제 안내 | [실제] | 크루즈 상태 | "주행 보조가 해제됐어요, 직접 속도를 조절하세요." |
| 회전 전 깜빡이 예고 | [실제] | 내비 회전 안내(TBT), 방향지시등 | "200미터 앞 우회전이에요, 깜빡이 먼저요." |
| 경고등 해설 | [실제] | 경고등(Telltales) | "타이어 공기압 경고예요, 가까운 곳에서 확인하세요." |
| 주행 리포트 (P단) | [실제] | 주행 중 코칭 기록 | 차 화면 리포트 |
| 차선 물기, 차선 변경 시 옆 차, 실선 변경 | [시연] | 가상 차선·옆 차 + 실제 방향지시등 | "오른쪽 뒤에 차가 있어요, 조금 기다렸다 변경하세요." |
| 이메일 리포트, 음성 질문 | [확장] | — | — |

### 안전 설계 원칙
1. 주행 중에는 화면 조작 없이 음성만 사용하고, 한 문장은 3초 이내로 짧게 말합니다.
2. 같은 안내는 쿨다운 동안 반복하지 않고, 동시에 여러 안내를 겹쳐 말하지 않습니다.
3. 확실하지 않으면 말하지 않습니다. 신호 값을 아직 모르면 판단하지 않습니다.
4. 상세 피드백은 정차(P단) 후에만 보여 줍니다.

## 테스트 환경

| 항목 | 값 |
|---|---|
| **Pleos Connect 에뮬레이터** | **v2.1.0** (AVD 기기 `Pleos Connect v2.1.0`, Android 14 / API 34, arm64-v8a) |
| **화면 규격** | **2560 × 1440, 160 dpi, 12.8"** (`adb shell wm size`, `wm density`로 확인) |
| 개발 PC | macOS, Apple Silicon (arm64) |
| Android Studio | 2026.2 (내장 JDK 25) |
| 빌드 | AGP 9.5.1, Gradle 9.6.0 (wrapper 포함), Kotlin 2.2.10, Jetpack Compose |
| SDK 버전 | minSdk 28, targetSdk 37, compileSdk 37 |
| Pleos SDK | `ai.pleos.playground:Vehicle:2.3.2`, `ai.pleos.playground:TextToSpeech:3.3.1` |

### 필요한 설정
- **에뮬레이터는 Cold boot로 켜야 합니다.** Quick boot로 켜면 차량 신호(VHAL)가 동작하지 않습니다.
  AVD 설정 → Advanced Settings → Startup > Default boot: **Cold**. RAM은 4 GB로 테스트했습니다.
- 에뮬레이터 설치 과정(수동 설치 방법 포함): [docs/02-emulator-setup.md](docs/02-emulator-setup.md)
- Pleos SDK는 Pleos Maven 저장소에서 받습니다. `settings.gradle.kts`에 이미 등록돼 있어 별도 설정은 없습니다.
- 앱이 쓰는 차량 퍼미션(`CAR_DRIVE`, `CAR_LIGHTS`, `TTS_SERVICE`)은 설치만으로 허용됩니다. 런타임 권한 요청이나 API 키는 필요 없습니다.
- 음성은 ON_DEVICE 모드라 네트워크나 앱 등록이 필요 없습니다.

## 실행 방법

### 1. 에뮬레이터 켜기
Android Studio → Device Manager에서 Pleos Connect v2.1.0 AVD를 **Cold boot**로 실행합니다.

### 2. 앱 설치

**APK로 설치**
```sh
adb install -r apk/HoseokChoi_MyFirstMile_release.apk
```

**소스에서 빌드해 설치**
```sh
./gradlew :app:installDebug
# 또는: 설치 후 앱까지 실행
scripts/install.sh
```

### 3. 앱 실행
런처에서 **My First Mile**을 엽니다. 앱을 열면 다음과 같이 동작합니다.
- 포그라운드 서비스가 시작되고 "주행 코치 동작 중" 알림이 뜹니다.
- "주행 코치를 시작합니다."라고 말합니다.
- 화면에 기어·조향각·방향지시등·브레이크·크루즈 값이 실시간으로 표시됩니다.

### 4. Virtual Console로 차량 신호 조작
```sh
scripts/vc.sh   # adb forward tcp:8088 tcp:8088 후 http://127.0.0.1:8088/ 열기
```
방향지시등은 Virtual Console의 **Test Console** 패널에 있습니다.

### 5. 코칭 확인 시나리오

| # | 조작 | 기대 결과 |
|---|---|---|
| 1 | 기어 **D**, 방향지시등 끔, 조향각을 **90° 이상**으로 0.8초 넘게 유지 | "방향지시등을 먼저 켜 주세요." |
| 2 | 조향각을 0으로 되돌린 뒤, 방향지시등을 **켜고** 같은 각도로 꺾기 | 안내 없음 |
| 3 | 방향지시등을 켜고 꺾은 상태에서 회전 중에 방향지시등 끄기 | 안내 없음 (이번 꺾음에서 이미 켰으므로) |
| 4 | 기어 **P**에서 조향각 크게 꺾기 | 안내 없음 (주행 중에만 코칭) |
| 5 | 1번을 10초 안에 반복 | 두 번째는 안내 없음 (쿨다운 10초) |

판단 기준(90°, 0.8초, 쿨다운 10초)은 [`CoachingConfig`](app/src/main/java/io/github/lapcatt/myfirstmile/coaching/CoachingConfig.kt)에 모여 있습니다.
이 에뮬레이터에서 조향각 범위는 ±540°이고, 90°는 핸들 1/4바퀴입니다.

### 6. 단위 테스트
코칭 규칙은 Android에 의존하지 않는 순수 Kotlin이라, 에뮬레이터 없이 테스트할 수 있습니다.
```sh
./gradlew testDebugUnitTest
```

## 구조

```
Vehicle SDK ──→ VehicleSignalRepository ──(StateFlow<VehicleState>)──→ DriveCoachService (포그라운드)
                (SDK에 접근하는 유일한 곳)                                │ 200ms마다 평가
                                                                         ▼
                                                     CoachingEngine (순수 Kotlin, JVM 테스트)
                                                                         │ CoachingEvent
                                                                         ▼
                                                     VoiceCoach (TextToSpeech SDK) ← strings.xml 문장
```

| 패키지 | 역할 |
|---|---|
| `vehicle/` | `VehicleSignalRepository`: Vehicle SDK 리스너를 등록하고, 값을 우리 타입(`VehicleState`)으로 바꿔 StateFlow로 내보냅니다. 조향각 부호도 여기서 맞춥니다 |
| `model/` | `VehicleState`: SDK 타입과 분리된 차량 상태 |
| `coaching/` | `CoachingEngine`, 규칙(`TurnSignalMissingRule`), 임계값(`CoachingConfig`), 결과(`CoachingEvent`). `android.*`를 쓰지 않습니다 |
| `voice/` | `VoiceCoach`: TextToSpeech SDK에 접근하는 유일한 곳. 말하는 중이면 가장 최근 문장 하나만 대기시킵니다 |
| `service/` | `DriveCoachService`: 앱이 화면 뒤로 가도 신호를 계속 받기 위한 포그라운드 서비스 |

차선·옆 차·실선 입력은 `PerceptionSource` 인터페이스로 받도록 설계했습니다. 지금은 가상 데이터(`SimulatedPerceptionSource`)로 시연하고, ADAS 데이터가 열리면 구현만 바꾸는 방식입니다(개발 예정).

## 문서
- [docs/02-emulator-setup.md](docs/02-emulator-setup.md): 에뮬레이터 설치 (macOS Apple Silicon, 수동 설치 포함)
- [docs/03-pleos-platform-notes.md](docs/03-pleos-platform-notes.md): 에뮬레이터에서 직접 확인한 SDK 동작 (조향각 부호와 범위, 백그라운드 수신, 차속·경고등·회전 안내 수신 등)
- [docs/04-architecture.md](docs/04-architecture.md): 앱 구조
- [docs/06-coaching-rules.md](docs/06-coaching-rules.md): 코칭 규칙별 조건과 근거
- [docs/devlog/](docs/devlog/): 날짜별 개발 기록

## 스크립트
| 스크립트 | 하는 일 |
|---|---|
| `scripts/check-env.sh` | SDK·JDK·에뮬레이터 이미지·AVD·기기 연결·Virtual Console 포워딩 점검 (읽기만 함) |
| `scripts/install.sh` | 디버그 빌드 → 설치 → 앱 실행 |
| `scripts/vc.sh` | Virtual Console 포워딩 후 브라우저로 열기 |
| `scripts/regulation.sh on\|off` | 주행 규제(화면 제한) 켜기/끄기 |
| `scripts/record.sh [이름]` | 에뮬레이터 화면 녹화 (`captures/`에 저장) |

## APK
- 위치: [`apk/HoseokChoi_MyFirstMile_release.apk`](apk/HoseokChoi_MyFirstMile_release.apk) (release 서명 빌드)
