package io.github.lapcatt.myfirstmile.coaching

import io.github.lapcatt.myfirstmile.model.VehicleState

/**
 * 코칭 규칙들을 모아 한 번에 평가한다. 한 번에 한 문장만 돌려준다.
 *
 * 지금은 규칙이 하나. 규칙이 늘면 여기서 우선순위를 정한다 (동시에 여러 안내 금지 원칙).
 */
class CoachingEngine(config: CoachingConfig = CoachingConfig()) {

    private val turnSignalMissing = TurnSignalMissingRule(config)

    fun evaluate(state: VehicleState, nowMs: Long): CoachingEvent? =
        turnSignalMissing.evaluate(state, nowMs)
}
