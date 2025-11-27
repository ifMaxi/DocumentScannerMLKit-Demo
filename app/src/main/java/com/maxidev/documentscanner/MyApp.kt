package com.maxidev.documentscanner

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.allowHardware
import coil3.request.allowRgb565
import coil3.request.crossfade
import coil3.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class MyApp: Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val debugLogger = DebugLogger()

        return ImageLoader.Builder(context)
            .crossfade(true)
            .allowRgb565(true)
            .allowHardware(true)
            .coroutineContext(context = Dispatchers.IO)
            .logger(logger = debugLogger)
            .build()
    }
}