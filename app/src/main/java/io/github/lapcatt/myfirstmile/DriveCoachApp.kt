package io.github.lapcatt.myfirstmile

import android.app.Application
import io.github.lapcatt.myfirstmile.vehicle.VehicleSignalRepository

/**
 * 앱 전체에서 하나만 있어야 하는 객체를 보관한다.
 *
 * Repository를 서비스(신호 수신)와 화면(표시)이 같이 써야 해서,
 * 둘 중 어느 쪽도 아닌 Application에 둔다.
 * 시작·정지는 [io.github.lapcatt.myfirstmile.service.DriveCoachService]만 한다.
 */
class DriveCoachApp : Application() {
    val vehicleSignals: VehicleSignalRepository by lazy { VehicleSignalRepository(this) }
}
