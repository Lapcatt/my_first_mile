package io.github.lapcatt.myfirstmile.voice

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale

/**
 * Pleos TextToSpeech SDK에 접근하는 유일한 곳. 코칭 문장을 소리로 바꾼다.
 *
 * - ON_DEVICE 모드: 차량 내장 엔진. 네트워크·앱 등록이 필요 없다 (공식 문서 TextToSpeech/intro).
 * - 말하는 중에 새 문장이 오면 끊지 않는다. 가장 최근 문장 하나만 기억해 두었다가
 *   지금 문장이 끝나면 말한다. (2026-11-13: 앞 문장이 잘리는 것을 에뮬레이터에서 확인)
 *   여러 개를 쌓지 않는 이유: 주행 안내는 "지금" 의미가 있어서, 밀린 안내를 연달아 하면 상황과 어긋난다.
 * - SDK 콜백은 다른 스레드에서 오므로 상태 변경은 모두 메인 스레드에서 한다.
 *
 * 아직 안 한 것: 오디오 포커스, 선행 묵음, 규칙별 우선순위.
 */
class VoiceCoach(context: Context) {

    private val tts = TextToSpeech(context.applicationContext, Mode.ON_DEVICE)
    private val main = Handler(Looper.getMainLooper())

    private var ready = false
    private var speaking = false
    private var pending: String? = null

    private val eventListener = object : EventListener {
        override fun onReady() { Log.d(TAG, "onReady") }
        override fun onStart() { Log.d(TAG, "onStart") }
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onDone() { Log.d(TAG, "onDone"); main.post { onFinished() } }
        override fun onStop() { Log.d(TAG, "onStop"); main.post { onFinished() } }
        override fun onError(errorType: ErrorType) {
            Log.e(TAG, "onError: $errorType")
            main.post { onFinished() }
        }
    }

    fun start() {
        tts.initialize(object : OnInitializeListener {
            override fun onSuccess() {
                main.post {
                    Log.d(TAG, "initialize success")
                    tts.addEventListener(eventListener)
                    tts.setLocale(Locale.KOREA)
                    ready = true
                    speakNextIfIdle()
                }
            }

            override fun onFailure(errorType: ErrorType) {
                Log.e(TAG, "initialize failed: $errorType")
            }
        })
    }

    /** 메인 스레드에서 부른다. */
    fun speak(text: String) {
        pending = text
        speakNextIfIdle()
    }

    fun stop() {
        pending = null
        if (ready) tts.stop()
    }

    /** 리스너를 먼저 떼고 SDK를 놓는다 (문서 순서). release 후에는 재사용 불가. */
    fun release() {
        main.removeCallbacksAndMessages(null)
        tts.removeEventListener(eventListener)
        tts.release()
        ready = false
    }

    private fun onFinished() {
        speaking = false
        speakNextIfIdle()
    }

    private fun speakNextIfIdle() {
        if (!ready || speaking) {
            pending?.let { Log.d(TAG, "waiting: $it") }
            return
        }
        val text = pending ?: return
        pending = null
        speaking = true
        Log.d(TAG, "speak: $text")
        tts.speak(text, true)
    }

    companion object {
        private const val TAG = "VoiceCoach"
    }
}
