package com.rewhost.app.di

import com.rewhost.app.api.RewHostApi
import com.rewhost.app.utils.AppSettings
import com.rewhost.app.utils.PlatformSettingsFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule = module {
    // PlatformSettingsFactory предоставляется через платформо-специфичный модуль
    single { get<PlatformSettingsFactory>().create() }
    
    single { AppSettings(get()) }
    
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    prettyPrint = true
                })
            }
        }
    }
    
    single { RewHostApi(get(), get()) }
}