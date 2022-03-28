package com.nvsp.manta_terminal.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
                    mViewModel.testConection(mViewModel.activeSetting.value?.ipAddress ?: ("NOIP"))
                }
                NO_CONECTED -> {
                    startActivity(SettingsActivity.createIntent(this))
                }
                CONECTED -> {
                    mViewModel.testVersion()
                }
                UPDATED -> {
                    mViewModel.loadRemoteSettings(this)
                }
                LOADED_REMOTE_SETTING -> {
                    mViewModel.testLogin()
                }
                NOT_LOGGED -> {
                    startApp()
                }
                LOGGED -> {
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