package com.nvsp.manta_terminal.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nvsp.manta_terminal.BaseApp
import com.nvsp.manta_terminal.workplaces.Workplace
import com.nvsp.nvmesapplibrary.architecture.CommunicationViewModel
import com.nvsp.nvmesapplibrary.communication.models.Request
import com.nvsp.nvmesapplibrary.communication.volley.ServiceVolley
import com.nvsp.nvmesapplibrary.database.LibRepository
import com.nvsp.nvmesapplibrary.login.models.User
import com.nvsp.nvmesapplibrary.queue.models.*

class MainFragmentViewModel (private val repository: LibRepository, private val api: ServiceVolley):
    CommunicationViewModel(repository,api){

    val workplaces = MutableLiveData<List<Workplace>>()
   //WQ
    val onProgressWQ = MutableLiveData<Boolean>(false)
    var gridWQ: EditableModuleLayoutDefinition = EditableModuleLayoutDefinition(0, 0)
    var definitonsWQ: List<QueueDefinition> = emptyList()
    val contentWQ = MutableLiveData<List<QueueData>>(mutableListOf())


    fun loadWorkplaces(){
        api.request(Request(
            com.android.volley.Request.Method.GET,
            Workplace.getUrl(BaseApp.remoteSettings?.id?:(-1)),""
        )){code, response ->
            val gson = Gson()
            val itemType = object : TypeToken<List<Workplace>>(){}.type
            val list = gson.fromJson<List<Workplace>>(response.array.toString(),itemType)
            Log.d("LOADER", "item size:${list.size}")
            workplaces.value=(list)
        }
    }
    fun loadDefs(user: User?=null) {
        onProgressWQ.value=true
        api.request(
            com.nvsp.nvmesapplibrary.communication.models.Request(
                com.android.volley.Request.Method.POST,
                WORK_QUEUE_DEFINITION,
                "",
                user,
                null
            ),
            hideProgressOnEnd =  false,
            showProgress = false
        ) { _, response ->
            response.getSingleObject()?.let {
                gridWQ = QueueDefinition.getGrid(it)
                definitonsWQ = QueueDefinition.createList(it)
               // Log.d("WQ_VIEWMODEL", "grid: $grid")
               // Log.d("WQ_VIEWMODEL", "data: $definitons")
                loadContent(user = user)
            }
        }
    }
  //  fun  isAll(act:Int?)=dataMaxSize.value==act

    fun loadContent(all:Boolean=false, user: User?=null){
        api.request(
            com.nvsp.nvmesapplibrary.communication.models.Request(
                com.android.volley.Request.Method.POST,
                WORK_QUEUE_DATA,
                "",
                user,
                QueueData.generateParam(0)
            ), showProgress = false
        ) { _, response ->
            response.getSingleObject()?.let {
                //dataMaxSize.value = QueueData.getDataSize(it)
               // isNextAvalaible= QueueData.getNextAvalaible(it)
                contentWQ.value = QueueData.createList(definitonsWQ,it, gridWQ)
                onProgressWQ.value=false

            }
        }
    }
    }