package com.nvsp.manta_terminal.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nvsp.manta_terminal.BaseApp
import com.nvsp.manta_terminal.BuildConfig
import com.nvsp.manta_terminal.R
import com.nvsp.manta_terminal.databinding.ActivityMainBinding
import com.nvsp.manta_terminal.viewmodels.MainViewModel
import com.nvsp.manta_terminal.viewmodels.SplashViewModel
import com.nvsp.nvmesapplibrary.App
import com.nvsp.nvmesapplibrary.architecture.BaseActivity
import com.nvsp.nvmesapplibrary.architecture.InfoDialog
import com.nvsp.nvmesapplibrary.architecture.LoadingDialog
import com.nvsp.nvmesapplibrary.constants.Keys
import com.nvsp.nvmesapplibrary.login.LoginActivity
import com.nvsp.nvmesapplibrary.login.models.User
import com.nvsp.nvmesapplibrary.settings.SettingsActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

import java.util.*

class MainActivity : AppCompatActivity() {
    var connectionErrorDialogEnable = true
    private var menu:Menu?=null
    private  val binding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val mViewModel :MainViewModel by lazy {
        getViewModel(
            null,
            MainViewModel::class
        )
        }
    private val infoDialog: InfoDialog by lazy {InfoDialog(this)}
    private val waitDialog: LoadingDialog by lazy {LoadingDialog(this)}
    private lateinit var startActivityForResult : ActivityResultLauncher<Intent>
    companion object {
        fun createIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    Log.d("MAINACTIVITY", "CREATE")
        setContentView(binding.root)
        initToolbar()
        mViewModel.activeSetting.observe(this) {
            it?.let {
                window.statusBarColor = it.getSettingColor(baseContext)
                App.setip(it.ipAddress, it.port)
            }
        }
        mViewModel.connectionLiveData.observe(this){
            //  Log.d("BASEACTIVITY", "connLive Data = $it")
                conectionState(it)
            if(connectionErrorDialogEnable)
                infoDialog.connection(it)
        }


        mViewModel.login.observe(this){
            Log.d("LOGIN", "loginState: ${it}")
            setUser(it)
        }
        startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                restartApp()
            }
        }

        mViewModel.activeSetting.observe(this){
            it?.let{set->
                mViewModel.loadSettings(set)
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        this.menu=menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings ->{
                startSettings()
                true  }
            R.id.action_LogOut->{
                lifecycleScope.launch {  mViewModel.logOut() }
                true
            }
            R.id.action_finish->{
                this.finish()
                true
            }
            R.id.action_login->{
                Log.d("LOGIN", "user: ${mViewModel.login.value}")
                if(mViewModel.login.value!=null){
                    lifecycleScope.launch {  mViewModel.logOut() }
                }else{
                    startLoginActivity()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun startSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        val bundle=Bundle()
        bundle.putString(Keys.APP_NAME, BaseApp.instance.packageName)

        bundle.putString(Keys.APP_ID, BuildConfig.APPLICATION_ID)

        intent.putExtras(bundle)
        startActivityForResult.launch(intent )
    }
   private fun startLoginActivity(){
        Log.d("LOGIN", "start login activity")
     //   waitDialog.show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtras(BaseApp.createLoginBundle())
        intent.putExtra(Keys.BACK_BUTTON_ENABLE,true)
        startActivity(intent)
        overridePendingTransition(com.nvsp.nvmesapplibrary.R.anim.slide_from_up,com.nvsp.nvmesapplibrary.R.anim.slide_to_down)
    }
    private fun initToolbar(){
        LoginActivity.terminalMode=true
        setSupportActionBar(binding.toolbar)
        Objects.requireNonNull(supportActionBar)!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar_clock)
    }
    private fun conectionState(state: Boolean){
        menu?.let { menu ->
            if (state) {
                menu.findItem(R.id.action_state_WS)
                    .setIcon(android.R.drawable.presence_online)
            } else {
                menu.findItem(R.id.action_state_WS)
                    .setIcon(android.R.drawable.presence_offline)
            }
        }
    }
    private fun setUser(user:User?){
        //todo Outdata
        binding.toolbarTitle.text=user?.toString()?:("")
        user?.let {
            menu?.findItem(R.id.action_login)?.setIcon(R.drawable.ic_baseline_logout_24)
        }?:run {
            menu?.findItem(R.id.action_login)?.setIcon(R.drawable.ic_user)
        }
    }
    private fun restartApp(){
        BaseApp.instance.restartApp()
    }
}