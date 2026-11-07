# 앱 구조

CLAUDE.md "코드 구조" 원칙을 패키지로 옮긴 것. 아직 코드 없음, 만들면서 갱신한다.

## 흐름
```
[Vehicle SDK] ─┐
[CarPropertyManager] ─┤→ VehicleSignalRepository ─(StateFlow)─┐
                                                          ├→ CoachingEngine (coaching/) → CoachingEvent
[PerceptionSource] ─(차선·옆 차·실선)───────────────────────┘          │
                                                            ┌────────┼────────┐
                                                        VoiceCoach   TripRecorder   (화면)
                                                        (TTS)        → 리포트(P단)
```

## 패키지 (예정)
| 패키지 | 역할 | Android 의존 |
|---|---|---|
| `vehicle/` | `VehicleSignalRepository`: SDK에 접근하는 유일한 곳. 조향각·방향지시등·브레이크·크루즈·경고등·기어·차속을 StateFlow로 노출 | O |
| `coaching/` | 코칭 규칙, `CoachingConfig`(임계값), `SteeringStabilityAnalyzer`. 입력은 순수 데이터, 출력은 `CoachingEvent` | **X** (JVM 단위 테스트) |
| `perception/` | `PerceptionSource` 인터페이스, `SimulatedPerceptionSource` (나중 `AdasPerceptionSource`) | X |
| `voice/` | TTS 재생: 150~200ms 선행 묵음, 오디오 포커스, Gleo 명령 시 즉시 중단 | O |
| `report/` | 주행 기록 → P단 리포트 화면·이메일 | O |
| `ui/` | Compose 화면. 기어 ≠ P면 최소 정보만 | O |

## 원칙
- coaching/은 `android.*` import 금지 → 에뮬레이터 없이 `./gradlew test`로 검증
- 멘트 문자열은 `strings.xml`, coaching/은 문자열 대신 이벤트 종류만 내보냄
- 임계값은 전부 `CoachingConfig`에 모음 (튜닝·PT 설명용)

## [미확인]
- Vehicle SDK 각 API의 클래스·리스너 이름, 값 단위(조향각 도? 라디안?), 콜백 주기
- 필요한 퍼미션 이름
- 기어 값을 Vehicle SDK로 받을지 CarPropertyManager로 받을지
