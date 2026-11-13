package io.github.lapcatt.myfirstmile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lapcatt.myfirstmile.model.VehicleState
import io.github.lapcatt.myfirstmile.ui.theme.DriveCoachTheme
import io.github.lapcatt.myfirstmile.vehicle.VehicleSignalRepository

/**
 * 1단계: 차량 신호가 실제로 들어오는지 확인하는 화면.
 * 값은 Logcat(태그 VehicleSignal)과 화면 둘 다에 보인다.
 */
class MainActivity : ComponentActivity() {

    private lateinit var repository: VehicleSignalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = VehicleSignalRepository(this)
        repository.start()

        setContent {
            DriveCoachTheme {
                val state by repository.state.collectAsState()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignalDebugScreen(state, Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onDestroy() {
        repository.stop()
        super.onDestroy()
    }
}

@Composable
private fun SignalDebugScreen(state: VehicleState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(32.dp)) {
        Text("차량 신호 확인 (1단계)", style = MaterialTheme.typography.headlineMedium)
        Text("기어: ${state.gear ?: "-"}", style = MaterialTheme.typography.titleLarge)
        Text("조향각(앞바퀴): ${state.steeringAngleDeg ?: "-"}°", style = MaterialTheme.typography.titleLarge)
        Text("방향지시등: ${state.turnSignal ?: "-"}", style = MaterialTheme.typography.titleLarge)
        Text("브레이크: ${state.brakePressed ?: "-"}", style = MaterialTheme.typography.titleLarge)
        Text("크루즈: ${state.cruise ?: "-"}", style = MaterialTheme.typography.titleLarge)
    }
}
