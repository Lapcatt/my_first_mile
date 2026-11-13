package io.github.lapcatt.myfirstmile.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.lapcatt.myfirstmile.DriveCoachApp
import io.github.lapcatt.myfirstmile.R
import io.github.lapcatt.myfirstmile.coaching.CoachingEngine
import io.github.lapcatt.myfirstmile.coaching.CoachingEvent
import io.github.lapcatt.myfirstmile.voice.VoiceCoach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 주행 중 차량 신호를 계속 받기 위한 포그라운드 서비스.
 *
 * 왜 필요한가: 운전 중에는 내비 화면이 앞에 있다. 일반 Activity는 화면 뒤로 가면
 * 시스템이 프로세스를 얼려서 SDK 콜백이 멈춘다 (2026-11-13 에뮬레이터에서 확인).
 * 포그라운드 서비스는 "지금 사용자를 위해 일하는 중"이라고 알림으로 밝히는 대신
 * 뒤에서도 계속 실행된다.
 */
class DriveCoachService : Service() {

    private val repository get() = (application as DriveCoachApp).vehicleSignals
    private lateinit var voice: VoiceCoach
    private val engine = CoachingEngine()

    // 서비스 수명에 묶인 코루틴 범위. onDestroy에서 취소한다.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createChannel()
        // Android 14(API 34)부터는 서비스 타입을 같이 넘겨야 한다. Pleos 에뮬레이터는 API 34.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        repository.start()

        // 2단계 확인용: 서비스가 뜨면 한 문장 말한다.
        voice = VoiceCoach(this)
        voice.start()
        voice.speak(getString(R.string.voice_coach_started))

        startCoachingLoop()
    }

    /**
     * 일정 간격으로 최신 차량 상태를 코칭 엔진에 넣는다.
     *
     * 왜 "값이 바뀔 때"가 아니라 주기적으로 보나: 규칙 1은 "0.8초 동안 계속"을 본다.
     * 핸들을 꺾은 채 가만히 있으면 새 값이 안 와도 시간은 흐르므로, 시간 기준으로 다시 봐야 한다.
     */
    private fun startCoachingLoop() {
        scope.launch {
            while (isActive) {
                val event = engine.evaluate(repository.state.value, SystemClock.elapsedRealtime())
                if (event != null) {
                    Log.d(TAG, "coaching: $event")
                    voice.speak(getString(event.messageRes()))
                }
                delay(EVAL_INTERVAL_MS)
            }
        }
    }

    private fun CoachingEvent.messageRes(): Int = when (this) {
        CoachingEvent.TURN_SIGNAL_MISSING -> R.string.voice_turn_signal_first
    }

    // 시스템이 서비스를 죽였다가 다시 살릴 수 있으면 다시 시작한다.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        scope.cancel()
        voice.release()
        repository.stop()
        super.onDestroy()
    }

    // 다른 컴포넌트가 bind해서 쓰는 서비스가 아니다.
    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.coach_channel_name),
            NotificationManager.IMPORTANCE_LOW, // 소리·팝업 없이 상태만 표시
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.coach_notification_title))
            .setContentText(getString(R.string.coach_notification_text))
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG = "DriveCoachService"
        private const val CHANNEL_ID = "drive_coach"
        private const val NOTIFICATION_ID = 1
        private const val EVAL_INTERVAL_MS = 200L

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, DriveCoachService::class.java),
            )
        }
    }
}
