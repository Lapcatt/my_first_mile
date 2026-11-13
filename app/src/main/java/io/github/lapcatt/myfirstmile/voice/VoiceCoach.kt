package io.github.lapcatt.myfirstmile.voice

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.util.Log
import java.util.Locale

/**
 * Pleos TextToSpeech SDK에 접근하는 유일한 곳. 코칭 문장을 소리로 바꾼다.
 *
 * - ON_DEVICE 모드: 차량 내장 엔진. 네트워크·앱 등록이 필요 없다 (공식 문서 TextToSpeech/intro).
 * - 초기화는 비동기다. 준비 전에 들어온 문장은 하나만 기억해 두었다가 준비되면 말한다.
 *   (주행 중 안내는 "지금" 의미가 있으므로 오래된 문장을 여러 개 쌓아 두지 않는다.)
 *
 * 아직 안 한 것: 오디오 포커스, 선행 묵음, 우선순위·쿨다운 큐 → 다음 단계.
 */
class VoiceCoach(context: Context) {

    private val tts = TextToSpeech(context.applicationContext, Mode.ON_DEVICE)
    private var ready = false
    private var pending: String? = null

    private val eventListener = object : EventListener {
        override fun onReady() { Log.d(TAG, "onReady") }
        override fun onStart() { Log.d(TAG, "onStart") }
        override fun onUpdatedRms(rms: Double) = Unit
        override fun onDone() { Log.d(TAG, "onDone") }
        override fun onStop() { Log.d(TAG, "onStop") }
        override fun onError(errorType: ErrorType) {
            Log.e(TAG, "onError: $errorType")
        }
    }

    fun start() {
        tts.initialize(object : OnInitializeListener {
            override fun onSuccess() {
                Log.d(TAG, "initialize success")
                tts.addEventListener(eventListener)
                tts.setLocale(Locale.KOREA)
                ready = true
                pending?.let { speakNow(it) }
                pending = null
            }

            override fun onFailure(errorType: ErrorType) {
                Log.e(TAG, "initialize failed: $errorType")
            }
        })
    }

    fun speak(text: String) {
        if (ready) speakNow(text) else pending = text
    }

    fun stop() {
        if (ready) tts.stop()
    }

    /** 리스너를 먼저 떼고 SDK를 놓는다 (문서 순서). release 후에는 재사용 불가. */
    fun release() {
        tts.removeEventListener(eventListener)
        tts.release()
        ready = false
    }

    private fun speakNow(text: String) {
        Log.d(TAG, "speak: $text")
        tts.speak(text, true)
    }

    companion object {
        private const val TAG = "VoiceCoach"
    }
}
