package com.nvsp.manta_terminal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.nvsp.manta_terminal.di.*
import com.nvsp.manta_terminal.ui.activities.MainActivity
import com.nvsp.nvmesapplibrary.App
import com.nvsp.nvmesapplibrary.constants.Keys
import com.nvsp.nvmesapplibrary.utils.model.RemoteSettings
import org.intellij.lang.annotations.Language
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import java.util.*
import kotlin.system.exitProcess

class BaseApp : Application() {
    lateinit var guid:String
    var urlApi:String ="http:/asda/"
    //var langCode="cz"
   // var locale=Locale("cz")


    @SuppressLint("HardwareIds")

    override fun onCreate() {
        super.onCreate()
        instance=this
        appContext = applicationContext
        guid=android.provider.Settings.Secure.getString(appContext.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        App.params(appContext,Const.dbName)
        startKoin()
    }

    fun startKoin(){
        org.koin.core.context.startKoin {
            //Zde inicializujeme vse pro DI

            App.di(appContext)
            androidLogger(Level.ERROR)  //logovani Koinu
            androidContext(appContext)
            modules(
                viewModelModule,
                repositoryModule,
                databaseModule,
                daoModule,
                volleyModule,
                apiModule
                //settingsModule
            ) //pole modulu
        }
    }

    fun refresh(){
        Log.d("BASEAPP", "RESTART KOIN")
        stopKoin()
        startKoin()
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        val mPendingIntentId = 1
        val mPendingIntent = PendingIntent.getActivity(
            applicationContext,
            mPendingIntentId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val mgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
        exitProcess(0)
    }
    fun setLocale(act:Activity, langCode:String?){
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val resources = act.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }


    companion object {
        var remoteSettings:RemoteSettings?=null
        fun reset(){
            this.instance.restartApp()
        }
        lateinit var instance: BaseApp
        lateinit var appContext: Context
            private set

        fun createLoginBundle(): Bundle {
            val bundle= Bundle()
            bundle.putString(Keys.APP_NAME, App.appName)
            bundle.putInt(Keys.DEFAULT_LOGIN_METHOD, com.nvsp.nvmesapplibrary.constants.Const.BY_NAME)
            bundle.putBoolean(Keys.DISABLE_ALTERNATIVE_METHOD, false)
            bundle.putString(Keys.APP_ID, BuildConfig.APPLICATION_ID)
            return bundle
        }


    }
}