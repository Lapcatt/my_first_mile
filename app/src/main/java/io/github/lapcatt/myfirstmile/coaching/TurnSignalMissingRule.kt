package io.github.lapcatt.myfirstmile.coaching

import io.github.lapcatt.myfirstmile.model.Gear
import io.github.lapcatt.myfirstmile.model.TurnSignal
import io.github.lapcatt.myfirstmile.model.VehicleState
import kotlin.math.abs

/**
 * 규칙 1: 방향지시등 없이 핸들을 크게 꺾음.
 *
 * "꺾음(에피소드)" = 기어 D에서 |조향각| ≥ bigSteerDeg 상태가 시작돼서 끝날 때까지.
 * 깜빡이 상태와 관계없이 핸들 각도로만 나눈다.
 *
 * 말하는 조건: 한 꺾음이 holdMs 이상 계속됐는데, 그동안 깜빡이를 한 번도 켜지 않았다.
 * - 꺾는 중에 깜빡이를 한 번이라도 켰으면 그 꺾음은 말하지 않는다.
 *   (회전 중에 깜빡이를 끄는 건 흔한 정상 조작. 2026-11-13 에뮬레이터에서 오탐 발견 후 변경)
 * - 한 꺾음에서는 최대 한 번만 말한다.
 * - 직전 안내 후 cooldownMs가 안 지났으면 그 꺾음은 말하지 않고 넘긴다.
 * - 깜빡이 상태를 모르면(null) 말하지 않는다: 확실하지 않으면 말하지 않는다.
 *
 * 시간은 밖에서 받는다(nowMs). 그래서 테스트에서 시간을 마음대로 흘려볼 수 있다.
 */
class TurnSignalMissingRule(private val config: CoachingConfig) {

    private var episodeStartMs: Long? = null
    private var signalUsedInEpisode = false
    private var episodeHandled = false
    private var lastSpokenMs: Long? = null

    fun evaluate(state: VehicleState, nowMs: Long): CoachingEvent? {
        if (!isBigSteerWhileDriving(state)) {
            // 꺾음이 끝나면 기억을 지우고 다음 꺾음을 새로 본다.
            episodeStartMs = null
            signalUsedInEpisode = false
            episodeHandled = false
            return null
        }

        val start = episodeStartMs ?: nowMs.also { episodeStartMs = it }
        if (state.turnSignal != TurnSignal.OFF) {
            // LEFT/RIGHT면 사용한 것으로 기억. null(모름)도 말하지 않는 쪽으로 처리.
            signalUsedInEpisode = true
        }

        if (episodeHandled || signalUsedInEpisode) return null
        if (nowMs - start < config.holdMs) return null

        episodeHandled = true
        val last = lastSpokenMs
        if (last != null && nowMs - last < config.cooldownMs) return null

        lastSpokenMs = nowMs
        return CoachingEvent.TURN_SIGNAL_MISSING
    }

    private fun isBigSteerWhileDriving(state: VehicleState): Boolean {
        val angle = state.steeringAngleDeg ?: return false
        return state.gear == Gear.DRIVE && abs(angle) >= config.bigSteerDeg
    }
}
