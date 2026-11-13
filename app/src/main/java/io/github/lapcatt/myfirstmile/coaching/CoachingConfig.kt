package io.github.lapcatt.myfirstmile.coaching

/**
 * 코칭 임계값을 한곳에 모은다. 숫자를 바꿀 때는 docs/06-coaching-rules.md에 근거도 같이 적는다.
 *
 * 조향각은 에뮬레이터 실측 범위 ±540°(핸들 회전각 수준) 기준이다.
 */
data class CoachingConfig(
    /** 이 각도 이상 꺾으면 "크게 꺾었다"로 본다. 초기값: 핸들 1/4바퀴. 근거 [미확인] */
    val bigSteerDeg: Float = 90f,
    /** 조건이 이만큼 계속돼야 말한다. 순간적인 값이나 직후 깜빡이 조작을 걸러내기 위함. 초기값 */
    val holdMs: Long = 800,
    /** 같은 규칙을 다시 말하기까지 기다리는 시간. 초기값 */
    val cooldownMs: Long = 10_000,
)
