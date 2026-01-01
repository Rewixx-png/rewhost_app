package com.rewhost.app

import android.app.Application
import com.rewhost.app.di.appModule
import com.rewhost.app.utils.PlatformSettingsFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class RewHostApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@RewHostApplication)
            modules(appModule, androidModule)
        }
    }
}

val androidModule = module {
    single { PlatformSettingsFactory(get()) }
}