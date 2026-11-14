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
- 차선·후측방 API 없음. ADAS SDK는 현재 목록에서 빠짐 (2026-11-02). 공지 1(2026-11-12): 미공개 SDK는 공모전 기간 제공 안 됨
- 공식 문서: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro

### SDK 소스에서 확인한 것 (2026-11-12, `Vehicle-2.3.2-sources.jar`, 패키지 `ai.pleos.playground.vehicle`)
공식 문서가 아니라 라이브러리 소스·KDoc 기준. 문서와 다르면 문서를 우선하고 여기 고친다.
| API | 메서드 / 리스너 | 값 | 퍼미션 (KDoc) |
|---|---|---|---|
| `SteeringWheel` | `registerSteeringWheelAngle(SteeringWheelAngleListener)` → `onSteeringWheelAngleUpdated(angle: Float?)` | **앞바퀴 조향각(도)**, 왼쪽이 음수. 핸들 회전각 아님 | `CAR_DRIVE` (normal) |
| `TurnSignal` | `registerTurnSignalState` → `TurnSignalValue` | `OFF / RIGHT / LEFT` | `CAR_LIGHTS` (normal) |
| `Brake` | `registerBrakePedalStatus` → `Boolean?` | **밟음 여부만**. 압력·퍼센트 없음 | `CAR_DRIVE` (normal) |
| `DrivingMode` | `registerCruiseControlState` → `CruiseControl` | `OTHER / ENABLED / ACTIVATED / USER_OVERRIDE / SUSPENDED / FORCED_DEACTIVATION_WARNING` | `CAR_DRIVE` (normal) |
| `Gear` | `registerGearSelection(GearSelectionListener)` → `VehicleGear` | `GEAR_UNKNOWN / NEUTRAL / REVERSE / PARK / DRIVE` | `CAR_DRIVE` (normal) |
| `Safety` | `registerTelltales(TelltaleInfoListener)` → `Telltales` (`TelltaleData(type, state, color, priority, warningCount)`) | `TelltaleState`: `OFF / ON / BLINK` | **`PRIVATE_CAR_SAFETY` (signature\|privileged)** → 서드파티 앱이 받을 수 있는지 [미확인] |

- `TelltaleType` 전체(92개): PARKING_BRAKE, ELECTRONIC_PARKING_BRAKE, SEAT_BELT, DOOR_OPEN, ABS, MDPS, TPMS, LOW_VOLT_BATTERY, ESC, ESC_OFF, AIRBAG, AFS, MASTER_WARNING, AUTO_HOLD, AUTO_HIGH_BEAM, HEADLAMP_LOW/HIGH, POSITION_LAMP, FRONT/REAR_FOG_LAMP, ENGINE_OIL_PRESSURE, ENGINE_TEMPERATURE, EMERGENCY_CALL, EV_SYSTEM, POWER_DOWN, RBS, IMMOBILIZER, ENGINE, LED_HEADLAMP, PREHEAT, PARTICLE_FILTER, H2_SENSOR, H2_LEAK, PASSENGER_AIRBAG_OFF/ON, TURN_SIGNAL_LEFT/RIGHT, ICY_ROAD, OVER_SPEED, WATER_SEPARATOR, SCR, LOW_EV_BATTERY, HEV/FCEV/EREV_SYSTEM, LPI, EV_CHARGING_CONNECTOR, AWD_LOCK, HEV_MODE, ISG, DBC, REV, XEV_READY, I_PEDAL, LOW_FUEL, LOW_FUEL_FCEV, EV_BATTERY_CONDITIONING, TRUNK_OPEN, CHILD_PRESENCE_DETECTION_OFF, DRIVE_MODE(+SPORT/ECO/COMFORT/TERRAIN), FORCED_ALL_WHEEL_DRIVE, FOUR_WHEEL_DRIVE_MODE_2H/4A/4H/4L, TOW_MODE, ESC_SPORT, CREEP_OFF, DRIFT/TRACK/UTILITY_MODE, GREEN_ZONE, PHEV_MODE(+HEV/AUTO/EV_PLUS), EREV_MODE, LAUNCH_MODE, ACCEL_RESPONSE_DASH/ACCEL, N_BATTERY_DRAG/SPRINT/ENDURANCE, **FCA(300), LKA(301), LFA(302), LCA_LEFT, LCA_RIGHT, ELK, ICC, DRIVER_ATTENTION**
  - 차선·충돌 관련 경고등 값은 **있다** (FCA·LKA·LFA·LCA·ELK). 단 퍼미션이 PRIVATE라 쓸 수 있는지는 에뮬레이터에서 확인 필요.
- 차속: Vehicle SDK 공개 API에 없음. 내부 `PleosPropertyIds`에 `PERF_VEHICLE_SPEED`가 있지만 외부로 노출되는 메서드 없음 → AAOS `CarPropertyManager` 경로 그대로 [미확인]
- `Display.getCurrentSpeedDisplayUnit()`로 속도 **단위**(km/h·mph)만 읽을 수 있음

## TTS / STT
- ON_DEVICE 모드면 네트워크·앱 등록 불필요 (2026-11-02)
- 음성 규칙: 150~200ms 선행 묵음, 오디오 포커스 요청, Gleo 음성 명령 시 즉시 중단
- TTS 문서: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro
- 오디오 포커스: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/audio-focus-development-guide/audio-focus-development

## 에뮬레이터
- Pleos Connect Emulator v2.1.0, API 34, 화면 2560×1440 · 160dpi · 12.8" (2026-11-13 `adb shell wm size/density`). 설치 절차는 [02-emulator-setup.md](02-emulator-setup.md) (2026-11-07)
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

## 에뮬레이터 내부 확인 (2026-11-13, adb)
- `cmd car_service get-property-value`는 "requires non-user build"로 막힘 → 셸로 차량 속성값을 직접 못 읽음. 값 수신 확인은 앱으로 해야 함
- 퍼미션 보호 수준 (`pm list permissions -f`, `dumpsys package permission`)
  - `pleos.car.permission.CAR_DRIVE`, `CAR_LIGHTS`: normal (설치만으로 허용)
  - `pleos.car.permission.PRIVATE_CAR_SAFETY`: **에뮬레이터에서는 normal** (SDK KDoc은 signature|privileged). 실차와 다를 수 있음
  - `android.car.permission.CAR_SPEED`: **dangerous** → 런타임 권한 요청 필요
  - `android.car.permission.CAR_POWERTRAIN`: normal
- 설치된 Pleos 서비스: `ai.pleos.playground.service.vehicle`, `.service.navi`, `.service.location.fused`, `.service.adas`, `.caas`(SpeechService 제공), 내비 앱 `ai.umos.maps.android.navigation.app`
  - ADAS 서비스 패키지가 있지만 공지 1에 따라 공모전 기간 SDK 미제공 → 사용 안 함

## 1단계 앱으로 신호 수신 확인 (2026-11-13, Vehicle SDK 2.3.2, 에뮬레이터 v2.1.0)
- 5개 모두 수신: 조향각, 방향지시등(LEFT/RIGHT/OFF), 브레이크(Boolean), 기어(P/D), 크루즈(longitudinal 모드 → ENABLED, ACTIVATED)
- 조향각: 약 0.1~3초 간격으로 **같은 값이 반복** 전달됨 → 코칭은 값 변화 기준으로 처리해야 함
- 조향각 부호(확정): 오른쪽 끝 → SDK **-540**, 왼쪽 끝 → SDK **+540**. 즉 **왼쪽 양수/오른쪽 음수**. KDoc("왼쪽 음수")과 반대, Virtual Console 표시와도 부호 반대
- 조향각 범위: **±540°** = 핸들 회전각 범위로 보임 (KDoc은 "앞바퀴 각도, 핸들 회전각 아님"이라 하지만 앞바퀴는 보통 ±35° 내외). 임계값은 이 범위 기준으로 정함
- 기어·방향지시등·브레이크·크루즈는 시작 시 현재값이 오지 않고 **바뀔 때만** 옴 (기어는 시작 시 1회 옴)
- **앱이 백그라운드로 가면 콜백이 멈춤** (내비 앱이 앞에 있던 6분간 0건). 앱을 다시 앞으로 가져오자 밀린 이벤트가 한 시각에 몰아서 전달됨 (순서도 섞임). Android cached app freezer로 추정
  → 코칭은 포그라운드 서비스에서 돌려야 함
  → **해결 확인**: `DriveCoachService`(foregroundServiceType=specialUse)로 옮긴 뒤, 내비 앱이 앞에 있어도 방향지시등·브레이크·조향각이 조작 시각에 실시간 수신됨 (2026-11-13)
- Virtual Console 상태(`/vehicle/state`)에 `controls.speed_kph`가 있음 → 차속은 Console 내부에 존재. 앱 수신 여부는 미확인 (조작 목록 catalog에는 PERF_VEHICLE_SPEED 없음)
- 방향지시등은 기본 화면이 아니라 Virtual Console의 **Test Console** 패널에 있음

## 시동 상태·차속 확인 (2026-11-14, 임시 시험 코드 → 확인 후 삭제)
- 방법: `useLibrary("android.car")` + `CarPropertyManager`로 `IGNITION_STATE`, `PERF_VEHICLE_SPEED` 구독
- **시동 상태(IGNITION_STATE): 사용 불가.** car_service 속성 목록에 아예 없음(VHAL 미지원). Vehicle SDK에도 `IgnitionState` enum만 있고 구독 메서드는 공개 안 됨
  - Virtual Console에는 "Power System Actual"(Off/Ready/On/Driving) 조작이 있으나 앱이 받을 수 있는 차량 속성으로 연결되지 않음 (catalog의 car_property 비어 있음)
- **차속(PERF_VEHICLE_SPEED): 수신 가능.** `android.car.permission.CAR_SPEED`(dangerous) 필요
  - 이 에뮬레이터는 멀티유저(현재 사용자 10). `adb shell pm grant`는 `--user 10`을 붙여야 적용됨
  - 단위 m/s(Float). 권한 허용 후 `PERF_VEHICLE_SPEED`, `PERF_VEHICLE_SPEED_DISPLAY`, `WHEEL_TICK`가 보이게 됨
  - 실제 앱에서는 런타임 권한 요청 화면이 필요 (주행 중엔 띄우면 안 되므로 P단 최초 실행 시)
- 기어는 `CAR_POWERTRAIN`(normal)만으로 `GEAR_SELECTION`, `CURRENT_GEAR`도 보임 (Pleos Gear API와 별개 경로)
- Virtual Console 조작 API는 **PUT**으로 보내야 함 (POST는 `emulator_inactive`). 예: `PUT /vehicle/turn-signal?mode=BlinkLeft`, `PUT /vehicle/gear?gear=Drive`, `PUT /vehicle/controls?steering=-180`

## 경고등(Telltales) 수신 확인 (2026-11-14, 임시 시험 코드 → 확인 후 삭제)
- `Safety.registerTelltales` + `pleos.car.permission.PRIVATE_CAR_SAFETY` 선언 → **수신 성공** (에뮬레이터에서 normal 권한)
  - `checkTelltalesCapabilities` → `CapabilityConfig(accessType=READ)`
  - 콜백마다 **현재 켜져 있는 경고등 전체 목록**이 옴 (`TelltaleData(type, state, color, priority, warningCount)`). 꺼진 항목은 목록에서 빠짐
  - 예: TPMS On → [TPMS], SeatBelt On → [SEAT_BELT, TPMS], TPMS Off → [SEAT_BELT]
- Virtual Console에서 켤 수 있는 경고등은 37종 (`PUT /vehicle/telltales/simulator?type=TPMS&state=On&color=Red`)
  - ParkingBrake, ElectronicParkingBrake, SeatBelt, DoorOpen, ABS, MDPS, TPMS, LowVoltBattery, ESC, ESCOff, Airbag, AFS, MasterWarning, AutoHold, AutoHighBeam, HeadlampLow/High, PositionLamp, Front/RearFogLamp, EngineOilPressure, EngineTemperature, EmergencyCall, EVSystem, PowerDown, RBS, Immobilizer, Engine, LEDHeadlamp, Preheat, ParticleFilter, H2Sensor, H2Leak, PassengerAirbagOff/On, TurnSignalLeft/Right
  - **차로·충돌·후측방(FCA, LKA, LFA, LCA, ELK)은 Console에 없음** → SDK enum에는 있지만 에뮬레이터에서 켜 볼 방법 없음
- 실차에서 PRIVATE_CAR_SAFETY가 서드파티 앱에 허용되는지는 여전히 미확인 (KDoc은 signature|privileged)

## NaviHelper 회전 안내(TBT) 수신 확인 (2026-11-14, 임시 시험 코드 → 확인 후 삭제)
- 의존성 `ai.pleos.playground:NaviHelper:3.0.5` (+ navi-aidl, navi-data). 퍼미션 `pleos.car.permission.NAVI_ROUTE`(정보 조회), `NAVI_ROUTE_SEARCH`(경로 요청)
- 공식 문서의 API 상세 페이지(TBTInfo, 리스너 시그니처)는 본문이 비어 있어 AAR을 javap로 확인:
  - `NaviHelper(context)` → `initialize(onConnected, onFailed)` → `addListener(NaviHelperEventListener)` → `getTBTInfo()` / `requestRoute(RouteInfo)`
  - `NaviHelperEventListener.onTBTInfo(List<TBTInfo>)` (default 메서드라 필요한 것만 override)
  - `TBTInfo(type: TurnType, distance: Double, description: String)`
  - `TurnType` 45종: TURN_LEFT/RIGHT/STRAIGHT, KEEP_L/R, SHARP_L/R, UTURN, ENTER/EXIT_IC_*, ENTER/AVOID_UNDER/OVER_*, ROUNDABOUT_*, ARRIVE, TUNNEL, TOLL, SERVICEAREA, RESTAREA 등
- 결과: 경로가 없으면 `TBT: []`. `requestRoute` 후 `onRouteStarted` → `NavigationStatus.ROUTE_DRIVE` → **TBT 목록 수신**
  - 예 (강남역 근처 좌표): 우회전 700m 대왕판교IC 방면 → 우회전 50m → IC 진입 → 톨게이트 → IC 진출 10km → 좌회전 450m 서초구청 → 우회전 1.5km 강남역 방면 → 도착
  - 에뮬레이터 현재 위치는 문서 예시 목적지(42dot)에서 약 50m (판교 쪽으로 보임)
- **미확인**: 차가 움직일 때 `distance`가 "다음 회전까지 남은 거리"로 줄어드는지 (정지 상태에서만 받음). 회전 예고 타이밍 결정에 필요
- `RouteInfo`의 poiId·address는 빈 문자열로도 경로 탐색 성공
- 앱 서비스(exported=false)는 `adb am start-foreground-service`로 호출 불가 (정상)

## Virtual Console 차속 조작 확인 (2026-11-14)
- 콘솔 조작 API는 **브라우저 콘솔 페이지가 열려 있을 때만** 받는다. 페이지가 1초마다 `POST /browser/session` 하트비트를 보내고, 끊기면 모든 PUT이 `emulator_inactive` (state의 `browser.timeout_ms: 3000`). 에뮬레이터를 켠 뒤 `scripts/vc.sh`로 페이지를 먼저 연다
- 차속은 콘솔 페이지의 ACCEL/BRAKE(고속도로 화면)로 바뀐다. 페이지가 계산한 값을 `PUT /vehicle/controls?speed=&accelerator=&brake=`로 140ms마다 보낸다
- 콘솔 `speed_kph`와 차량 `PERF_VEHICLE_SPEED`가 일치함 (88 km/h ↔ 24.44 m/s, `dumpsys car_service`의 lastEvent로 확인). 앱의 CarPropertyManager 수신은 앞 절에서 확인함
- 주의: 페이지가 열려 있으면 페이지 자체의 주행 계산이 curl로 보낸 speed 값을 곧바로 덮어쓴다. 차속 시연은 페이지 버튼으로 하거나, 자동화하려면 페이지를 닫고 하트비트를 직접 보내야 함 [미확인]
