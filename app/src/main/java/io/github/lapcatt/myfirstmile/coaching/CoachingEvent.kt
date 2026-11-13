package io.github.lapcatt.myfirstmile.coaching

/**
 * 코칭 결과. 문장 자체는 여기 두지 않는다 (문장은 strings.xml, 연결은 서비스에서).
 * 그래서 이 패키지는 Android 리소스 없이 테스트할 수 있다.
 */
enum class CoachingEvent {
    /** 방향지시등 없이 핸들을 크게 꺾음 → "방향지시등을 먼저 켜 주세요" */
    TURN_SIGNAL_MISSING,
}
