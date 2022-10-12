package com.nvsp.manta_terminal.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.nvsp.LOGIN_OPERATION
import com.nvsp.MODE_QUEUE
import com.nvsp.TEAM_WORKING
import com.nvsp.WORKPLACE_ID
import com.nvsp.manta.MAINTERENCE
import com.nvsp.manta.SICO_MODULE
import com.nvsp.manta.WORK_QUEUE
import com.nvsp.manta_terminal.BaseApp
import com.nvsp.manta_terminal.R
import com.nvsp.manta_terminal.adapters.OperatorAdapter
import com.nvsp.manta_terminal.databinding.DialogFillQrLogOperationBinding
import com.nvsp.manta_terminal.databinding.FragmentMainBinding
import com.nvsp.manta_terminal.viewmodels.MainFragmentViewModel
import com.nvsp.manta_terminal.workplaces.Workplace
import com.nvsp.manta_terminal.workplaces.WorkplaceAdapter
import com.nvsp.nvmesapplibrary.TITAN_CLASS
import com.nvsp.nvmesapplibrary.TITAN_ID
import com.nvsp.nvmesapplibrary.architecture.BaseFragment
import com.nvsp.nvmesapplibrary.communication.socket.MessageListener
import com.nvsp.nvmesapplibrary.communication.socket.WebSocketManager
import com.nvsp.nvmesapplibrary.databinding.DialogMainterenceFilterBinding
import com.nvsp.nvmesapplibrary.menu.MenuButton
import com.nvsp.nvmesapplibrary.menu.MenuButtonTerm
import com.nvsp.nvmesapplibrary.menu.MenuDef
import com.nvsp.nvmesapplibrary.queue.OPERATOR_MAIN_LOGGED
import com.nvsp.nvmesapplibrary.queue.OPERATOR_MAIN_NOT_LOGGED
import com.nvsp.nvmesapplibrary.queue.QueueAdapter
import com.nvsp.nvmesapplibrary.queue.models.SystemFilters
import com.nvsp.nvmesapplibrary.queue.models.WORK_QUEUE_ID
import com.nvsp.nvmesapplibrary.queue.work_queue.WorkQueueActivity
import com.nvsp.nvmesapplibrary.utils.CaptureActivity
import com.nvsp.nvmesapplibrary.utils.ScreenUtils
import com.nvsp.nvmesapplibrary.views.NButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragment :
    BaseFragment<FragmentMainBinding, MainFragmentViewModel>(MainFragmentViewModel::class),
    MessageListener {

   private  val barcode=MutableLiveData<String>("")
   private  val wpAdapter: WorkplaceAdapter by lazy {
        WorkplaceAdapter {
            selectedWorkplace(it)
        }
    }
    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                refreshData()
            }
        }
    private val resultLauncherCam =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if(intentResult.contents != null) {
                barcode.value = intentResult.contents
            }
        }
    }
   private fun scanByQR(code:(String)->Unit){
        barcode.value=""
        val integrator =  IntentIntegrator(activity).apply {
            captureActivity = CaptureActivity::class.java
            setRequestCode(1)
        }
        resultLauncherCam.launch(integrator.createScanIntent())
        barcode.observe(viewLifecycleOwner) {
            Log.d("observeBarcode", "code: $it")
            if(it.isNotEmpty())
                code(it)
        }
    }
   private  val adapterWQ: QueueAdapter by lazy {
        QueueAdapter(context, OPERATOR_MAIN_NOT_LOGGED,
            { queueData, i ->  //item Click
            }, { _, _ -> //item Long Click
            }, { item ->
                viewModel.logOutOperation(item.getId().toInt(), viewModel.selectedWPId) {
                    refreshData()
                }

            })
    }
    private val adapterOperator: OperatorAdapter by lazy {
        OperatorAdapter(context, OPERATOR_MAIN_NOT_LOGGED, mutableListOf(),
            { item ->  //item Click
            }, { item -> //remove
                viewModel.logoutOperator(item.id)

            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

    }


    companion object;

    override fun onResume() {
        refreshData()
        CoroutineScope(Dispatchers.IO).launch {
            WebSocketManager.connect()
        }
        super.onResume()
    }

    override fun onPause() {
        WebSocketManager.close()
        super.onPause()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycle.addObserver(this)
    }

    override fun onDetach() {
        super.onDetach()
        lifecycle.removeObserver(this)
    }

    override val bindingInflater: (LayoutInflater) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate

    override fun initViews() {
        viewModel.activeSetting.observe(viewLifecycleOwner){


        }

        viewModel.login.observe(viewLifecycleOwner) { usr ->

            if (usr == null) {
                adapterWQ.changeMode(OPERATOR_MAIN_NOT_LOGGED)
                adapterOperator.changeMode(OPERATOR_MAIN_NOT_LOGGED, usr?.idEmployee)
                hideMenu()
            } else {

                adapterWQ.changeMode(OPERATOR_MAIN_LOGGED)
                adapterOperator.changeMode(OPERATOR_MAIN_LOGGED, usr.idEmployee)
                viewModel.loadMenu(viewModel.selectedWPId, usr.role?:(0))
            }

            binding.ibAddOperation.isEnabled = usr != null
            binding.ibAddOperator.isEnabled = usr != null

        }
        viewModel.loadDefs(viewModel.login.value, viewModel.selectedWPId)
        binding.tabWP.apply {
            val lm = LinearLayoutManager(context)
            lm.orientation = LinearLayoutManager.HORIZONTAL
            layoutManager = lm
            setHasFixedSize(true)
            adapter = wpAdapter
        }

        viewModel.workplaces.observe(this) {
            Log.d("OBSERVER", "getList: ${it.size} items")
            wpAdapter.setNewList(it.toMutableList())
        }
        viewModel.onProgressWQ.observe(viewLifecycleOwner) {
            binding.operRefresher.isRefreshing = it
        }
        viewModel.contentWQ.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                Log.d("TAG", "data = ${it[0].data}")
            Log.d("OBSERVER", "data: $it")

            context?.let { _ ->
                adapterWQ.setNewItems(it)
            }
            initAdapter()


        }
        viewModel.operatorsList.observe(viewLifecycleOwner) {
            it.forEach { op ->
                Log.d("OPERATOR", op.toString())
            }
            adapterOperator.setNewItems(it)


        }
        binding.ibAddOperator.setOnClickListener {
            viewModel.loginOperator()
        }
        binding.ibAddOperation.setOnClickListener {
            showAddOperationDialog()

        }
        binding.operRefresher.setOnRefreshListener {
            viewModel.loadContent(user = viewModel.login.value)
        }
        binding.operatorRefresher.setOnRefreshListener {
            viewModel.loadEmployees()
        }
        viewModel.onProgressOP.observe(viewLifecycleOwner) {
            binding.operatorRefresher.isRefreshing = it
        }
        viewModel.menu.observe(viewLifecycleOwner){btnList->
            viewModel.menuDef?.let {btnDef->
                showMenu(btnDef,btnList)
            }
        }
    }

    private fun refreshData() {
        viewModel.loadContent(user = viewModel.login.value)
        viewModel.loadEmployees()
    }

    override fun onActivityCreated() {

        viewModel.loadWorkplaces()
    }

    private fun selectedWorkplace(item: Workplace) {
        val url = viewModel.activeSetting.value?.getIpAndPort()
        val devId = BaseApp.remoteSettings?.id
        WebSocketManager.close()
        val urlAddress="ws://$url/API/Devices/$devId/Status/Workplace/${item.id}?roleId=${viewModel.login.value?.role}&editableListId=$WORK_QUEUE_ID&editableListFilterJson=[{\"argumentKey\":\"WorkplaceID\",\"argumentValue\":\"${item.id}\"}]"
        Log.d("SOCKET INIT", "ip and port:$urlAddress ")
        WebSocketManager.init(urlAddress, this)
        CoroutineScope(Dispatchers.IO).launch {
            WebSocketManager.connect()
        }
        viewModel.login.value?.let {
            viewModel.loadMenu(item.id, it.role?:(0))


        }

        binding.apply {
            viewModel.selectedWPId = item.id
            refreshData()
            tvWorkplace.text = item.lname

            if (item.notificationStatus) {
                ivNot.visibility = View.VISIBLE
            } else {
                ivNot.visibility = View.INVISIBLE
            }
            ivTypeWp.setImageResource(
                if (item.isRobot)
                    R.drawable.ic_roboticarm
                else
                    R.drawable.ic_handwork
            )

            if (item.state != "--") {
                tvWorkplaceState.text = item.state
                if (item.state.isNotEmpty()) {
                    tvWorkplaceState.visibility = View.VISIBLE
                    tvWorkplaceState.backgroundTintList = ColorStateList.valueOf(item.getColorHex())
                } else {
                    context?.let {
                        tvWorkplaceState.visibility = View.INVISIBLE
                    }
                }
            } else
                context?.let {
                    tvWorkplaceState.text = ""
                    tvWorkplaceState.visibility = View.INVISIBLE
                }
        }


    }

    private fun initAdapter() {
        binding.operatornRecycler.let {
            val lm = LinearLayoutManager(context)
            it.layoutManager = lm
            it.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
            it.setHasFixedSize(true)
            it.adapter = adapterOperator
        }


        binding.operationRecycler.let {
            val lm = LinearLayoutManager(context)
            it.layoutManager = lm
            it.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
            it.setHasFixedSize(true)
            it.adapter = adapterWQ

        }
    }

    fun showMenu(menuDef:MenuDef, buttons:Array<Array<MenuButtonTerm>>){
        binding.menuGrid.visibility=View.VISIBLE
        binding.menuGrid.removeAllViews()
        if(buttons.isNotEmpty()){
            binding.menuGrid.animate().translationX(0f)
        }else{
            binding.menuGrid.animate().translationX(((150+8)*menuDef.colCount).toFloat())
            binding.menuGrid.layoutParams.width = 0
        }
        if(menuDef.hasItems()){
            binding.menuGrid.columnCount=menuDef.colCount+1
            binding.menuGrid.rowCount = menuDef.rowCount+1
            fillMenuGrid(buttons)

        }
        if(viewModel.login.value==null ){
            binding.menuGrid.layoutParams.width = 0
        }
    }
    fun hideMenu(){
        binding.menuGrid.animate().translationX(((150+8)*(viewModel.menuDef?.colCount?:(2))).toFloat())
        binding.menuGrid.visibility=View.GONE
        //binding.menuGrid.layoutParams.width = 0
    }

    override fun onConnectSuccess() {
        addText(" Connected successfully \n ")
    }

    override fun onConnectFailed() {
        addText(" Connection failed \n ")
    }

    override fun onClose() {
        addText(" Closed successfully \n ")
    }

    override fun onMessage(text: String?) {
            addText( " Receive message: $text \n " )// todo receiuve data  vypnuto at to nespamuje
    }

    private fun addText(text: String?) {
        Log.d("SOCKET", "SOCKET MESSAGE: $text")
    }


    private fun fillMenuGrid(dataset:Array<Array<MenuButtonTerm>>){
        dataset.forEachIndexed {row, buttonRow ->
            buttonRow.forEachIndexed { col, menuButton ->
                when(menuButton.specialFunction){
                    "separator"->{
                        addDivider(menuButton)
                    }
                    "SPACE"->{
                        addSpace(row,col)
                    }
                    else->{
                        addButton(menuButton)
                    }
                }
            }
        }
    }

    fun showAddOperationDialog(){
        var id:Int?=null
        val builder = AlertDialog.Builder(context)
            .create()
        val bindingDialog = DialogFillQrLogOperationBinding.inflate(LayoutInflater.from(context))
        builder.setView(bindingDialog.root)

        fun verifyOperation(){
            viewModel.verifyOperationCode(bindingDialog.edBarcode.text.toString()){
                if(it>0){
                    id=it
                    bindingDialog.btnOk.isEnabled=true
                }else {
                    id=null
                    bindingDialog.btnOk.isEnabled=false
                }
            }
        }

        with(bindingDialog){
            btnBack.setOnClickListener {
                builder.dismiss()
            }
            btnChooseFromList.setOnClickListener {
                val intent = Intent(context, WorkQueueActivity::class.java).apply {
                    putExtra(MODE_QUEUE, LOGIN_OPERATION)
                    putExtra(WORKPLACE_ID, viewModel.selectedWPId)
                }
                resultLauncher.launch(intent)
                builder.dismiss()
            }
            bindingDialog.rfidInputLayout.setEndIconOnClickListener {
                Log.d("ONCLICK", "CLICK ON SCANNER")
                scanByQR{
                    Log.d("SCAN", "QR=$it")
                    bindingDialog.edBarcode.setText(it)
                    verifyOperation()
                }
            }
            bindingDialog.edBarcode.setOnEditorActionListener { textView, i, keyEvent ->
                Log.d("barcode done", "i=$i")
                if(i== EditorInfo.IME_ACTION_DONE)
                    verifyOperation()
                false
            }
            btnOk.setOnClickListener {
                id?.let {id->
                    viewModel.loginOperation(id){
                        refreshData()
                    }
                }

                builder.dismiss()
            }
        }
        builder.show()


    }
    private fun addDivider(buttonDef:MenuButtonTerm){
        val view = ImageView(requireContext())
        view.setImageResource(com.nvsp.nvmesapplibrary.R.drawable.separator)
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = 0
        layoutParams.height = 10
        layoutParams.columnSpec = GridLayout.spec(0, binding.menuGrid.columnCount, 1f)
        layoutParams.rowSpec = GridLayout.spec(buttonDef.posRow, GridLayout.FILL, 1f)
        view.layoutParams = layoutParams
        binding.menuGrid.addView(view)
    }
    private  fun addSpace(row:Int, col:Int){
        val view =View(requireContext())
        val layoutParams = GridLayout.LayoutParams()
        layoutParams.width = 0
        layoutParams.height = 1
        layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
        layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
        view.layoutParams = layoutParams
        binding.menuGrid.addView(view)
    }
    private fun addButton(buttonDef: MenuButtonTerm){
        val view = NButton(requireContext())
            view.setBorder(null)
        view.setText(buttonDef.label ?: "")
        view.isEnabled = buttonDef.permission>0
        view.setOnClickListener {
            onClick(buttonDef)
        }

        val layoutParams = GridLayout.LayoutParams()
        layoutParams.setMargins(8)
        layoutParams.height = 80
        if (buttonDef.width==1)
            layoutParams.width = 150.toPix()
        else
            layoutParams.width = 308.toPix()

        layoutParams.columnSpec = GridLayout.spec(buttonDef.posCol, buttonDef.width, 1f)
        layoutParams.rowSpec = GridLayout.spec(buttonDef.posRow, GridLayout.FILL, 1f)
        view.layoutParams = layoutParams
        binding.menuGrid.addView(view)
    }

    private fun onClick(button: MenuButtonTerm) {
        Log.d("BTN MENU CLICK", "click on :I${button.objectId}")
        when(button.objectId.toInt()){
            1001->{
                launchExternalModule(TITAN_ID, TITAN_CLASS)
            }
            100002->{
                launchEvidenceTitan()
            }
        }
    }
    fun Int.toPix()=(this*(context?.resources?.displayMetrics?.density?:(1f))+0.5f).toInt()

    private fun launchEvidenceTitan(){
        val sendIntent = Intent(Intent.ACTION_MAIN)
        sendIntent.setComponent(ComponentName(TITAN_ID, TITAN_CLASS))
        viewModel.login.value?.fillIntent(sendIntent)
        sendIntent.putExtra(WORKPLACE_ID, viewModel.selectedWPId)
        sendIntent.putExtra(TEAM_WORKING, viewModel.workplaces.value?.find { it.id == viewModel.selectedWPId }?.isRobot)
        /* val sendIntent = requireActivity().packageManager.getLaunchIntentForPackage("com.nvsp.sico")
         viewModel.login.value?.fillIntent(sendIntent)*/
        startActivity(sendIntent)
    }
    private fun launchExternalModule(appId:String, appClass:String){
        val sendIntent = Intent(Intent.ACTION_MAIN)
        sendIntent.setComponent(ComponentName(appId, appClass))
        viewModel.login.value?.fillIntent(sendIntent)
        /* val sendIntent = requireActivity().packageManager.getLaunchIntentForPackage("com.nvsp.sico")
         viewModel.login.value?.fillIntent(sendIntent)*/
        startActivity(sendIntent)
    }

}