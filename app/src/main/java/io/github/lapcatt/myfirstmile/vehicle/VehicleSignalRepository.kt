package io.github.lapcatt.myfirstmile.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.constant.area.TurnSignalValue
import ai.pleos.playground.vehicle.constant.control.CruiseControl
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.BrakePedalStatusListener
import ai.pleos.playground.vehicle.listener.CruiseControlStateListener
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.SteeringWheelAngleListener
import ai.pleos.playground.vehicle.listener.TurnSignalStateListener
import android.content.Context
import android.util.Log
import io.github.lapcatt.myfirstmile.model.CruiseState
import io.github.lapcatt.myfirstmile.model.Gear
import io.github.lapcatt.myfirstmile.model.TurnSignal
import io.github.lapcatt.myfirstmile.model.VehicleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Pleos Vehicle SDK에 접근하는 유일한 곳.
 *
 * - SDK 콜백(리스너) 5개를 등록하고, 값이 오면 [state]를 갱신한다.
 * - 나머지 코드는 SDK를 모르고 [state] (StateFlow)만 구독한다.
 *
 * 왜 StateFlow인가: 콜백은 "값이 바뀌었다"는 이벤트를 흩어진 곳에서 따로따로 준다.
 * StateFlow는 "지금 차 상태" 하나를 항상 들고 있어서, 화면·코칭 로직이
 * 언제 구독해도 최신 상태를 바로 받는다.
 */
class VehicleSignalRepository(context: Context) {

    private val vehicle = Vehicle(context.applicationContext)

    private val _state = MutableStateFlow(VehicleState())
    val state: StateFlow<VehicleState> = _state.asStateFlow()

    // 리스너는 해제할 때 같은 객체를 넘겨야 해서 필드로 들고 있는다.
    private val steeringListener = object : SteeringWheelAngleListener {
        override fun onSteeringWheelAngleUpdated(angle: Float?) {
            val normalized = angle?.let { it * STEERING_SIGN }
            log("steering", normalized) // 앱 기준(왼쪽 음수)으로 찍는다. Virtual Console 표시와 같음
            _state.update { it.copy(steeringAngleDeg = normalized) }
        }
        override fun onFailed(e: Exception) = logError("steering", e)
    }

    private val turnSignalListener = object : TurnSignalStateListener {
        override fun onTurnSignalStateUpdated(state: TurnSignalValue?) {
            log("turnSignal", state)
            _state.update { it.copy(turnSignal = state?.toModel()) }
        }
        override fun onFailed(e: Exception) = logError("turnSignal", e)
    }

    private val brakeListener = object : BrakePedalStatusListener {
        override fun onBrakePedalStatusUpdated(status: Boolean?) {
            log("brake", status)
            _state.update { it.copy(brakePressed = status) }
        }
        override fun onFailed(e: Exception) = logError("brake", e)
    }

    private val gearListener = object : GearSelectionListener {
        override fun onSelected(gear: VehicleGear?) {
            log("gear", gear)
            _state.update { it.copy(gear = gear?.toModel()) }
        }
        override fun onFailed(e: Exception) = logError("gear", e)
    }

    private val cruiseListener = object : CruiseControlStateListener {
        override fun onCruiseControlStateUpdated(state: CruiseControl?) {
            log("cruise", state)
            _state.update { it.copy(cruise = state?.toModel()) }
        }
        override fun onFailed(e: Exception) = logError("cruise", e)
    }

    /** 차량 서비스에 연결하고 리스너를 등록한다. */
    fun start() {
        Log.d(TAG, "start")
        vehicle.initialize()
        vehicle.getSteeringWheel().registerSteeringWheelAngle(steeringListener)
        vehicle.getTurnSignal().registerTurnSignalState(turnSignalListener)
        vehicle.getBrake().registerBrakePedalStatus(brakeListener)
        vehicle.getGear().registerGearSelection(gearListener)
        vehicle.getDrivingMode().registerCruiseControlState(cruiseListener)
    }

    /** 리스너를 먼저 해제하고 SDK를 놓는다. */
    fun stop() {
        Log.d(TAG, "stop")
        vehicle.getSteeringWheel().unregisterSteeringWheelAngle(steeringListener)
        vehicle.getTurnSignal().unregisterTurnSignalState(turnSignalListener)
        vehicle.getBrake().unregisterBrakePedalStatus(brakeListener)
        vehicle.getGear().unregisterGearSelection(gearListener)
        vehicle.getDrivingMode().unregisterCruiseControlState(cruiseListener)
        vehicle.release()
    }

    private fun log(name: String, value: Any?) = Log.d(TAG, "$name = $value")

    private fun logError(name: String, e: Exception) {
        Log.e(TAG, "$name failed: ${e.message}", e)
    }

    companion object {
        const val TAG = "VehicleSignal"

        /**
         * SDK 조향각을 앱 규칙(왼쪽 음수, 오른쪽 양수)으로 맞추는 부호.
         * 에뮬레이터 v2.1.0 실측: 왼쪽 끝 +540, 오른쪽 끝 -540 (SDK 문서와 반대, 2026-11-13).
         * 실차에서 반대로 나오면 이 값만 1f로 바꾼다.
         */
        private const val STEERING_SIGN = -1f
    }
}

// --- SDK 타입 → 우리 타입 변환. 모르는 값은 안전한 쪽(OFF/UNKNOWN/OTHER)으로. ---

private fun TurnSignalValue.toModel(): TurnSignal = when (this) {
    TurnSignalValue.LEFT -> TurnSignal.LEFT
    TurnSignalValue.RIGHT -> TurnSignal.RIGHT
    else -> TurnSignal.OFF
}

private fun VehicleGear.toModel(): Gear = when (this) {
    VehicleGear.GEAR_PARK -> Gear.PARK
    VehicleGear.GEAR_REVERSE -> Gear.REVERSE
    VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
    VehicleGear.GEAR_DRIVE -> Gear.DRIVE
    else -> Gear.UNKNOWN
}

private fun CruiseControl.toModel(): CruiseState = when (this) {
    CruiseControl.ENABLED -> CruiseState.ENABLED
    CruiseControl.ACTIVATED -> CruiseState.ACTIVATED
    CruiseControl.USER_OVERRIDE -> CruiseState.USER_OVERRIDE
    CruiseControl.SUSPENDED -> CruiseState.SUSPENDED
    CruiseControl.FORCED_DEACTIVATION_WARNING -> CruiseState.FORCED_DEACTIVATION_WARNING
    else -> CruiseState.OTHER
}
