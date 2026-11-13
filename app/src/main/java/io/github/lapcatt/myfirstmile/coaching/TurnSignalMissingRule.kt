package io.github.lapcatt.myfirstmile.coaching

import io.github.lapcatt.myfirstmile.model.Gear
import io.github.lapcatt.myfirstmile.model.TurnSignal
import io.github.lapcatt.myfirstmile.model.VehicleState
import kotlin.math.abs

/**
 * 규칙 1: 방향지시등 없이 핸들을 크게 꺾음.
 *
 * 조건: 기어 D + |조향각| ≥ bigSteerDeg + 방향지시등 OFF, 이 상태가 holdMs 이상 계속.
 *
 * "에피소드" = 조건이 참이 된 순간부터 다시 거짓이 될 때까지의 한 번의 꺾음.
 * - 한 에피소드에서는 최대 한 번만 말한다 (계속 꺾고 있는 동안 반복 금지).
 * - 직전 안내 후 cooldownMs가 안 지났으면 그 에피소드는 말하지 않고 넘긴다.
 *
 * 시간은 밖에서 받는다(nowMs). 그래서 테스트에서 시간을 마음대로 흘려볼 수 있다.
 */
class TurnSignalMissingRule(private val config: CoachingConfig) {

    private var episodeStartMs: Long? = null
    private var episodeHandled = false
    private var lastSpokenMs: Long? = null

    fun evaluate(state: VehicleState, nowMs: Long): CoachingEvent? {
        if (!isConditionMet(state)) {
            // 조건이 풀리면 에피소드 종료 → 다음 꺾음을 새로 본다.
            episodeStartMs = null
            episodeHandled = false
            return null
        }

        val start = episodeStartMs ?: nowMs.also { episodeStartMs = it }
        if (episodeHandled) return null
        if (nowMs - start < config.holdMs) return null

        episodeHandled = true
        val last = lastSpokenMs
        if (last != null && nowMs - last < config.cooldownMs) return null

        lastSpokenMs = nowMs
        return CoachingEvent.TURN_SIGNAL_MISSING
    }

    private fun isConditionMet(state: VehicleState): Boolean {
        val angle = state.steeringAngleDeg ?: return false
        return state.gear == Gear.DRIVE &&
            abs(angle) >= config.bigSteerDeg &&
            // null(아직 모름)은 OFF로 치지 않는다: 확실하지 않으면 말하지 않는다.
            state.turnSignal == TurnSignal.OFF
    }
}
