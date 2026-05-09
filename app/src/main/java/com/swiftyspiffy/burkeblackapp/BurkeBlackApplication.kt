package com.swiftyspiffy.burkeblackapp

import android.app.Application
import com.swiftyspiffy.burkeblackapp.data.AppSettings
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.push.PushNotificationManager
import com.swiftyspiffy.burkeblackapp.util.AppLogger

class BurkeBlackApplication : Application() {

    lateinit var appSettings: AppSettings
        private set

    override fun onCreate() {
        super.onCreate()
        AppLogger.log("App: onCreate")
        appSettings = AppSettings(this)
        GiveawayWebSocketManager.instance.appSettings = appSettings
        PushNotificationManager.createNotificationChannels(this)
    }
}
