package com.nvsp.manta_terminal.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.nvsp.manta_terminal.BaseApp

import com.nvsp.nvmesapplibrary.architecture.BaseViewModel
import com.nvsp.nvmesapplibrary.constants.Const
import com.nvsp.nvmesapplibrary.database.LibRepository
import com.nvsp.nvmesapplibrary.settings.models.Settings

import com.nvsp.nvmesapplibrary.App
import com.nvsp.nvmesapplibrary.communication.models.Request
import com.nvsp.nvmesapplibrary.communication.volley.ServiceVolley
import com.nvsp.nvmesapplibrary.communication.volley.VolleySingleton
import com.nvsp.nvmesapplibrary.rpc.OutData
import com.nvsp.nvmesapplibrary.utils.CommonUtils
import com.nvsp.nvmesapplibrary.utils.model.RemoteSettings
import org.json.JSONObject
import java.io.IOException

const val LOADED_SETTING=1
const val CONECTED=2
const val UPDATED=3
const val LOADED_REMOTE_SETTING=4
const val  LOGGED=5
const val  NOT_LOGGED=105
const val NO_CONECTED=102
const val NOT_LOADED_REMOTE_SETTING=104

class SplashViewModel(private val repository: LibRepository): BaseViewModel(repository){
    var splasCheckstatus= MutableLiveData<Int>(0)

    val api :ServiceVolley by lazy { ServiceVolley(VolleySingleton.getInstance(), BaseApp.instance.urlApi) }

    fun loadSettings(set:Settings){
        Const.URL_BASE = set.baseUrl()
        BaseApp.instance.urlApi=set.baseUrl()
        Log.d("URL", "URL IS: ${Const.URL_BASE}")
        BaseApp.instance.refresh()
        splasCheckstatus.value=LOADED_SETTING
        App.setip(set.ipAddress)

    }
    fun skipSettings(){
        splasCheckstatus.value=0
    }
    fun testLogin(){
       login.value?.let {
           splasCheckstatus.postValue(LOGGED)
       }?: kotlin.run {
           splasCheckstatus.postValue(NOT_LOGGED)
       }
    }
    fun loadRemoteSettings(context:Context){
        api.request(
          Request(com.android.volley.Request.Method.POST,
          "Devices/CheckRegistration","",
              json = CommonUtils.getRegisterJson(context)
          )
        ){code, response ->
            val remoteSettings=Gson().fromJson(response.getSingleObject().toString(),RemoteSettings::class.java)
            Log.d("REMOTE", remoteSettings.toString())
            BaseApp.remoteSettings=remoteSettings
            OutData.idTerminal = remoteSettings.id
           ServiceVolley.deviceID=remoteSettings.id
            splasCheckstatus.postValue(LOADED_REMOTE_SETTING)
        }



    }
fun testVersion(){
    splasCheckstatus.postValue(UPDATED)
}
 fun testConection(ip:String){
     var countOKConection=0
     var numOfLostPacket=0
     for(i in 0 until 10){ //5
         if(isNetworkAvailable(ip)) {
             numOfLostPacket = 0
             splasCheckstatus.postValue(CONECTED)
         }else{
             numOfLostPacket++
            Log.d("COMM", "LOST PACKET $numOfLostPacket")
         }
         if(numOfLostPacket >5)//2
             splasCheckstatus.postValue(NO_CONECTED)
     }


 }

    fun isNetworkAvailable(ip:String): Boolean {
        if(ip.contains("ngrok"))
            return true
        else{


            val runtime = Runtime.getRuntime()
            try {


                val ipProcess = runtime.exec("/system/bin/ping -c 1 ${ip}") // TODO -> upravit adresu
                val exitValue = ipProcess.waitFor()
                 Log.d("PING", "PING \"/system/bin/ping -c 1 ${ip}\" RET: ${ipProcess.waitFor() }")
                return exitValue == 0

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return false
        }
    }

}