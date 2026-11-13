package io.github.lapcatt.myfirstmile.model

/**
 * 앱이 쓰는 차량 상태 한 장면(snapshot).
 *
 * Pleos SDK 타입을 그대로 쓰지 않고 우리 타입으로 옮겨 둔다.
 * 그래야 코칭 로직(coaching/)이 SDK·Android 없이 JVM 단위 테스트로 돌아간다.
 * 값이 아직 안 들어왔으면 null.
 */
data class VehicleState(
    /** 앞바퀴 조향각(도). 왼쪽이 음수. 핸들 회전각이 아님 (SDK KDoc 기준). */
    val steeringAngleDeg: Float? = null,
    val turnSignal: TurnSignal? = null,
    /** 브레이크 페달을 밟고 있는지. SDK가 압력·퍼센트는 주지 않는다. */
    val brakePressed: Boolean? = null,
    val gear: Gear? = null,
    val cruise: CruiseState? = null,
)

enum class TurnSignal { OFF, LEFT, RIGHT }

enum class Gear { PARK, REVERSE, NEUTRAL, DRIVE, UNKNOWN }

enum class CruiseState {
    OTHER, ENABLED, ACTIVATED, USER_OVERRIDE, SUSPENDED, FORCED_DEACTIVATION_WARNING
}
