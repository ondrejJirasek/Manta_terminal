package com.nvsp.manta_terminal.viewmodels


import android.util.Log
import com.nvsp.manta_terminal.BaseApp

import com.nvsp.nvmesapplibrary.architecture.BaseViewModel
import com.nvsp.nvmesapplibrary.architecture.CommunicationViewModel
import com.nvsp.nvmesapplibrary.communication.volley.ServiceVolley
import com.nvsp.nvmesapplibrary.constants.Const
import com.nvsp.nvmesapplibrary.database.LibRepository
import com.nvsp.nvmesapplibrary.rpc.OutData
import com.nvsp.nvmesapplibrary.settings.models.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel(private val repository: LibRepository, private val api: ServiceVolley):CommunicationViewModel(repository,api){
    fun loadSettings(set:Settings){
                Const.URL_BASE = set.baseUrl()
                BaseApp.instance.urlApi=set.baseUrl()
                Log.d("URL", "URL IS: ${Const.URL_BASE}")
                BaseApp.instance.refresh()

    }
    override suspend fun logOut(){
        OutData.login=false
        OutData.terminalEvent=1
        OutData.execute(api){
            status, apiResponse ->
            Log.d("OUTDATA", "logout, status:$status, response:${apiResponse.getSingleObject()}")
        }
        super.logOut()

    }

}