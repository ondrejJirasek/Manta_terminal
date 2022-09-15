package com.nvsp.manta_terminal.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.nvsp.manta_terminal.BaseApp
import com.nvsp.manta_terminal.R
import com.nvsp.manta_terminal.viewmodels.*
import com.nvsp.manta_terminal.viewmodels.SplashViewModel

import com.nvsp.nvmesapplibrary.constants.Keys
import com.nvsp.nvmesapplibrary.login.LoginActivity
import com.nvsp.nvmesapplibrary.settings.SettingsActivity
import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.getViewModel

class SplashScreen : AppCompatActivity() {
    private val mViewModel: SplashViewModel by lazy {
        getViewModel(
            null,
            SplashViewModel::class
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        lifecycleScope.launch {
            mViewModel.logOut()
        }
        mViewModel.activeSetting.observe(this) {
            it?.let { set ->
                mViewModel.loadSettings(set)
            } ?: (startActivity(SettingsActivity.createIntent(this)))

        }
        mViewModel.login.observe(this) {
            it?.let {
                mViewModel.splasCheckstatus.postValue(LOGGED)
            }
        }
        mViewModel.splasCheckstatus.observe(this) { action ->
            when (action) {
                LOADED_SETTING -> {
                    Log.d("SPLASH", "LOADED SETTINGS")
                    mViewModel.activeSetting.value?.let {    mViewModel.testConnection(it.ipAddress, it.port) }
                }
                NO_CONECTED -> {
                    Log.d("SPLASH", "NOT CONECTED")
                    startActivity(SettingsActivity.createIntent(this))
                }
                CONECTED -> {
                    Log.d("SPLASH", "CONECTED")
                    mViewModel.testVersion()
                }
                UPDATED -> {
                    Log.d("SPLASH", "UPDATED")
                    mViewModel.loadRemoteSettings(this)
                }
                LOADED_REMOTE_SETTING -> {
                    Log.d("SPLASH", "LOADED REMOTE SETTINGS")
                    mViewModel.testLogin()
                }
                NOT_LOGGED -> {
                    Log.d("SPLASH", "NOT_LOGED")
                    startApp()
                }
                LOGGED -> {
                    Log.d("SPLASH", "STARTAPP")
                    startApp()
                }

            }
        }

    }
    fun startApp(){
        startActivity(MainActivity.createIntent(this))
        finish()
    }
    fun startLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtras(BaseApp.createLoginBundle())
        startActivity(intent)
    }
}