package com.nvsp.manta_terminal.viewmodels

import android.util.Log
import android.view.Menu
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nvsp.manta_terminal.BaseApp
import com.nvsp.manta_terminal.models.Operator
import com.nvsp.manta_terminal.workplaces.Workplace
import com.nvsp.nvmesapplibrary.architecture.CommunicationViewModel

import com.nvsp.nvmesapplibrary.communication.volley.ServiceVolley
import com.nvsp.nvmesapplibrary.database.LibRepository
import com.nvsp.nvmesapplibrary.login.models.User
import com.nvsp.nvmesapplibrary.menu.MenuButton
import com.nvsp.nvmesapplibrary.menu.MenuButtonTerm
import com.nvsp.nvmesapplibrary.menu.MenuDef
import com.nvsp.nvmesapplibrary.queue.models.*
import com.nvsp.nvmesapplibrary.rpc.OutData
import com.nvsp.nvmesapplibrary.rpc.Rpc
import com.nvsp.nvmesapplibrary.rpc.RpcParam


class MainFragmentViewModel (private val repository: LibRepository, private val api: ServiceVolley):
    CommunicationViewModel(repository,api){
    var selectedWPId:Int = 0
    val workplaces = MutableLiveData<List<Workplace>>()
    val menu= MutableLiveData<Array<Array<MenuButtonTerm>>>()
    var menuDef : MenuDef?=null
    val operatorsList = MutableLiveData<List<Operator>>()
    val onProgressWQ = MutableLiveData<Boolean>(false)
    val onProgressOP = MutableLiveData<Boolean>(false)
    var gridWQ: EditableModuleLayoutDefinition = EditableModuleLayoutDefinition(0, 0)
    var definitonsWQ: List<ViewDefinition> = emptyList()
    val contentWQ = MutableLiveData<List<ViewData>>(mutableListOf())



    fun loadWorkplaces(){
        api.request(
            com.nvsp.nvmesapplibrary.communication.models.Request(
            Request.Method.GET,
            Workplace.getUrl(BaseApp.remoteSettings?.id?:(-1)),""
        )
        ){ code, response ->
            val gson = Gson()
            val itemType = object : TypeToken<List<Workplace>>(){}.type
            val list = gson.fromJson<List<Workplace>>(response.array.toString(),itemType)
            Log.d("LOADER", "item size:${list.size}")
            workplaces.value=(list)
        }
    }
    fun loadDefs(user: User?=null, idWP:Int) {
        onProgressWQ.value=true
        api.request(
            com.nvsp.nvmesapplibrary.communication.models.Request(
               Request.Method.GET,
                ViewDefinition.getURL(WORK_QUEUE_ID),
                "",
                user,
                null
            ),
            hideProgressOnEnd =  false,
            showProgress = false
        ) { code, response ->
            if(code==200)
            response.getSingleObject()?.let {
                gridWQ = ViewDefinition.getGrid(it)
                definitonsWQ = ViewDefinition.createList(it)
               // Log.d("WQ_VIEWMODEL", "grid: $grid")
               // Log.d("WQ_VIEWMODEL", "data: $definitons")
                loadContent(user = user)
            }
        }
    }
  //  fun  isAll(act:Int?)=dataMaxSize.value==act

    fun loadContent(all:Boolean=false, user: User?=null){
        val jsonFilter = mutableListOf<SystemFilters>(
            SystemFilters("WorkplaceID", selectedWPId.toString())

        )
        Log.d("VIEWMODEL MAIN", "jsonFilter: $jsonFilter")
        api.request(
            com.nvsp.nvmesapplibrary.communication.models.Request(
                com.android.volley.Request.Method.POST,
                ViewData.getURL(WORK_QUEUE_ID),
                "",
                user,
                ViewData.generateParam(0, user?.id, SystemFilters.getJsonArray(jsonFilter))
            ), showProgress = false
        ) { _, response ->
            response.getSingleObject()?.let {
                //dataMaxSize.value = QueueData.getDataSize(it)
               // isNextAvalaible= QueueData.getNextAvalaible(it)
                contentWQ.value = ViewData.createList(definitonsWQ,it, gridWQ)
                onProgressWQ.value=false

            }
        }
    }


    fun logOutOperation(id:Int, selectedWPId:Int, ret:(Boolean)->Unit){
        OutData.logInOutOperationParams(login.value?.idEmployee, login = false, idWP = selectedWPId, idOperation = id)
        OutData.execute(api){ i, apiResponse ->
            ret(i==200)
        }
    }
    fun loadEmployees(){
        onProgressOP.value=true
        api.request(
            com.nvsp.nvmesapplibrary.communication.models.Request(
                Request.Method.GET,
                Operator.getUrl(selectedWPId),
                "",
                null,
                null
            )
        ) { code, response ->
            onProgressOP.value=false
            val gson = Gson()
            val itemType = object : TypeToken<List<Operator>>(){}.type
            operatorsList.value = gson.fromJson<List<Operator>>(response.array.toString(),itemType)
        }


    }
    fun loginOperator(){
        OutData.logInOutOperatorParams(login.value?.idEmployee, login = true, idWP = selectedWPId)
        OutData.execute(api){i, apiResponse ->
            refreshData()
        }
    }
    fun logoutOperator(idOperator:Int){
        OutData.logInOutOperatorParams(login.value?.idEmployee, login = false, idWP = selectedWPId)
        OutData.execute(api){i, apiResponse ->
            refreshData()
        }
    }
    fun loadMenu(wpId:Int, role:Long){
        api.request(
            com.nvsp.nvmesapplibrary.communication.models.Request(
                Request.Method.GET,
                MenuDef.urlTerm2(role, wpId),""
            )
        ){ code, response ->

            val gson = Gson()
            val obj= response.getSingleObject()
            obj?.let {
            menuDef=gson.fromJson(obj.getJSONObject("menuDefinition").toString(), MenuDef::class.java)
            Log.d("menuDef", "$menuDef")


                menuDef?.let { def->

                    menu.postValue(MenuButtonTerm.createList(obj.getJSONArray("menuButtons"), def))
                }
            }
        }

    }
    fun verifyOperationCode(code:String, ret:(Int)->Unit){
        val rpc = Rpc("SPMANTA_FindOpBarCode")
        val params = mutableListOf<RpcParam>()
        params.add(RpcParam.putInt("TerminalID", (BaseApp.remoteSettings?.id?:(1)).toInt()))
        params.add(RpcParam.putInt("EmployeeID", (login.value?.idEmployee?.toInt())))
        params.add(RpcParam.putInt("WorkPlaceID", selectedWPId))
        params.add(RpcParam.putString("Barcode", code))
        rpc.addParams(params)
        rpc.execute(api) { code, response ->
            response.getSingleObject()?.let {
                ret(it.getInt("ID"))
            }

        }
    }
    fun loginOperation(id:Int,  ret:(Boolean)->Unit){
        OutData.logInOutOperationParams(login.value?.idEmployee, login = true, idWP = selectedWPId, idOperation = id)
        OutData.execute(api){ i, apiResponse ->
            ret(i==200)
        }
    }
    fun refreshData(){
    loadEmployees()
    }
    }
