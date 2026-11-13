package io.github.lapcatt.myfirstmile.coaching

import io.github.lapcatt.myfirstmile.model.Gear
import io.github.lapcatt.myfirstmile.model.TurnSignal
import io.github.lapcatt.myfirstmile.model.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * JVM에서 바로 도는 테스트 (에뮬레이터 불필요). 시간은 숫자로 직접 흘린다.
 * 실행: ./gradlew :app:testDebugUnitTest
 */
class TurnSignalMissingRuleTest {

    private val config = CoachingConfig(bigSteerDeg = 90f, holdMs = 800, cooldownMs = 10_000)
    private lateinit var rule: TurnSignalMissingRule

    private val bigLeftNoSignal = VehicleState(
        steeringAngleDeg = -120f, turnSignal = TurnSignal.OFF, gear = Gear.DRIVE,
    )
    private val straight = bigLeftNoSignal.copy(steeringAngleDeg = 0f)

    @Before
    fun setUp() {
        rule = TurnSignalMissingRule(config)
    }

    @Test
    fun `holdMs 동안 계속되면 한 번 말한다`() {
        assertNull(rule.evaluate(bigLeftNoSignal, 0))
        assertNull(rule.evaluate(bigLeftNoSignal, 799))
        assertEquals(CoachingEvent.TURN_SIGNAL_MISSING, rule.evaluate(bigLeftNoSignal, 800))
    }

    @Test
    fun `오른쪽으로 꺾어도 똑같이 본다`() {
        val right = bigLeftNoSignal.copy(steeringAngleDeg = 120f)
        rule.evaluate(right, 0)
        assertEquals(CoachingEvent.TURN_SIGNAL_MISSING, rule.evaluate(right, 800))
    }

    @Test
    fun `순간적으로 꺾었다 돌아오면 말하지 않는다`() {
        rule.evaluate(bigLeftNoSignal, 0)
        rule.evaluate(straight, 500)
        assertNull(rule.evaluate(bigLeftNoSignal, 900)) // 새 에피소드 시작점이 900
    }

    @Test
    fun `같은 꺾음 동안에는 한 번만 말한다`() {
        rule.evaluate(bigLeftNoSignal, 0)
        rule.evaluate(bigLeftNoSignal, 800)
        assertNull(rule.evaluate(bigLeftNoSignal, 5_000))
        assertNull(rule.evaluate(bigLeftNoSignal, 20_000))
    }

    @Test
    fun `쿨다운 안의 새 꺾음은 넘기고, 지나면 다시 말한다`() {
        rule.evaluate(bigLeftNoSignal, 0)
        rule.evaluate(bigLeftNoSignal, 800) // 말함 (lastSpoken = 800)
        rule.evaluate(straight, 1_000)

        rule.evaluate(bigLeftNoSignal, 2_000)
        assertNull(rule.evaluate(bigLeftNoSignal, 2_800)) // 쿨다운 중 → 이 에피소드는 넘김
        assertNull(rule.evaluate(bigLeftNoSignal, 12_000)) // 같은 에피소드라 쿨다운이 끝나도 늦게 말하지 않음

        rule.evaluate(straight, 12_100)
        rule.evaluate(bigLeftNoSignal, 13_000)
        assertEquals(CoachingEvent.TURN_SIGNAL_MISSING, rule.evaluate(bigLeftNoSignal, 13_800))
    }

    @Test
    fun `방향지시등을 켰으면 말하지 않는다`() {
        val withSignal = bigLeftNoSignal.copy(turnSignal = TurnSignal.LEFT)
        rule.evaluate(withSignal, 0)
        assertNull(rule.evaluate(withSignal, 2_000))
    }

    @Test
    fun `꺾는 도중 방향지시등을 켜면 말하지 않는다`() {
        rule.evaluate(bigLeftNoSignal, 0)
        rule.evaluate(bigLeftNoSignal.copy(turnSignal = TurnSignal.LEFT), 500)
        assertNull(rule.evaluate(bigLeftNoSignal.copy(turnSignal = TurnSignal.LEFT), 1_000))
    }

    @Test
    fun `깜빡이를 켜고 꺾다가 꺾은 채로 끄면 말하지 않는다`() {
        // 에뮬레이터에서 발견 (2026-11-13): 회전 중 깜빡이를 끄자 "켜 주세요"가 나옴 → 오탐
        val withSignal = bigLeftNoSignal.copy(turnSignal = TurnSignal.LEFT)
        rule.evaluate(withSignal, 0)
        rule.evaluate(withSignal, 5_000)
        rule.evaluate(bigLeftNoSignal, 11_000) // 꺾은 채로 깜빡이 OFF
        assertNull(rule.evaluate(bigLeftNoSignal, 12_000))
        assertNull(rule.evaluate(bigLeftNoSignal, 30_000))
    }

    @Test
    fun `깜빡이를 쓴 꺾음이 끝나면 다음 꺾음은 새로 본다`() {
        val withSignal = bigLeftNoSignal.copy(turnSignal = TurnSignal.LEFT)
        rule.evaluate(withSignal, 0)
        rule.evaluate(bigLeftNoSignal, 1_000) // 꺾은 채로 끔 → 말하지 않음
        rule.evaluate(straight, 2_000) // 꺾음 끝

        rule.evaluate(bigLeftNoSignal, 3_000) // 새 꺾음, 깜빡이 없음
        assertEquals(CoachingEvent.TURN_SIGNAL_MISSING, rule.evaluate(bigLeftNoSignal, 3_800))
    }

    @Test
    fun `기어 D가 아니면 말하지 않는다`() {
        for (gear in listOf(Gear.PARK, Gear.REVERSE, Gear.NEUTRAL, Gear.UNKNOWN, null)) {
            val r = TurnSignalMissingRule(config)
            val s = bigLeftNoSignal.copy(gear = gear)
            r.evaluate(s, 0)
            assertNull("gear=$gear", r.evaluate(s, 2_000))
        }
    }

    @Test
    fun `임계값보다 작게 꺾으면 말하지 않는다`() {
        val small = bigLeftNoSignal.copy(steeringAngleDeg = -89f)
        rule.evaluate(small, 0)
        assertNull(rule.evaluate(small, 2_000))
    }

    @Test
    fun `방향지시등 상태를 아직 모르면 말하지 않는다`() {
        val unknown = bigLeftNoSignal.copy(turnSignal = null)
        rule.evaluate(unknown, 0)
        assertNull(rule.evaluate(unknown, 2_000))
    }

    @Test
    fun `조향각을 아직 모르면 말하지 않는다`() {
        val unknown = bigLeftNoSignal.copy(steeringAngleDeg = null)
        rule.evaluate(unknown, 0)
        assertNull(rule.evaluate(unknown, 2_000))
    }
}
