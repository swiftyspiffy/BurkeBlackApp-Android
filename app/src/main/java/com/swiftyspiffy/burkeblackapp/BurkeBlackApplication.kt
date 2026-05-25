package com.swiftyspiffy.burkeblackapp

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.swiftyspiffy.burkeblackapp.data.AppSettings
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.push.PushNotificationManager
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.widget.StreamStatusWorker

class BurkeBlackApplication : Application(), ImageLoaderFactory {

    lateinit var appSettings: AppSettings
        private set

    override fun onCreate() {
        super.onCreate()
        AppLogger.log("App: onCreate")
        appSettings = AppSettings(this)
        GiveawayWebSocketManager.instance.appSettings = appSettings
        PushNotificationManager.createNotificationChannels(this)
        StreamStatusWorker.schedule(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}
