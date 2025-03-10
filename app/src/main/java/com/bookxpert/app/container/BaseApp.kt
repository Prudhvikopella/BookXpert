package com.pxquiz.app.container

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.bookxpert.app.utils.SharedPrefUtil
import com.pxquiz.app.ui.Koin.repoModule
import com.pxquiz.app.ui.Koin.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        globalContext = this
        startKoin {
            androidContext(this@BaseApp)
            modules(listOf(viewModelModule, repoModule))
        }
        }

    companion object {
        lateinit var globalContext: BaseApp
    }



}