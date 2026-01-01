package com.rewhost.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.rewhost.app.di.appModule
import com.rewhost.app.utils.PlatformSettingsFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализация Koin с Android Context
        startKoin {
            androidContext(this@MainActivity)
            modules(appModule, androidModule())
        }

        setContent {
            App()
        }
    }
}

fun androidModule() = module {
    single { PlatformSettingsFactory(get()) }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}