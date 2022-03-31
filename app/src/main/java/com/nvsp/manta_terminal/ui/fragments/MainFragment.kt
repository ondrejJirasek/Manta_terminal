package com.nvsp.manta_terminal.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nvsp.manta_terminal.R
import com.nvsp.manta_terminal.databinding.FragmentMainBinding
import com.nvsp.manta_terminal.viewmodels.MainFragmentViewModel
import com.nvsp.manta_terminal.workplaces.Workplace
import com.nvsp.manta_terminal.workplaces.WorkplaceAdapter
import com.nvsp.nvmesapplibrary.App
import com.nvsp.nvmesapplibrary.architecture.BaseFragment
import com.nvsp.nvmesapplibrary.queue.NORMAL
import com.nvsp.nvmesapplibrary.queue.OPERATOR_MAIN
import com.nvsp.nvmesapplibrary.queue.QueueAdapter
import com.nvsp.nvmesapplibrary.utils.PaginationScrollListener

class MainFragment : BaseFragment<FragmentMainBinding, MainFragmentViewModel>(MainFragmentViewModel::class) {
    val wpAdapter:WorkplaceAdapter by lazy { WorkplaceAdapter(){
        selectedWorkplace(it)
    } }
    val   adapterWQ: QueueAdapter by lazy {
        QueueAdapter(context, OPERATOR_MAIN,
        {queueData, i ->  //item Click
        },{ _, _ -> //item Long Click
          } )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadDefs(App.user)
    }


    companion object {

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
        binding.tabWP.apply {
            val lm = LinearLayoutManager(context)
            lm.orientation = LinearLayoutManager.HORIZONTAL
            layoutManager = lm
            setHasFixedSize(true)
            adapter = wpAdapter
        }
       viewModel.workplaces.observe(this){
           Log.d("OBSERVER", "getList: ${it.size} items")
          wpAdapter.setNewList(it.toMutableList())
       }
        viewModel.onProgressWQ.observe(viewLifecycleOwner){
            binding.operRefresher.isRefreshing=it
        }
        viewModel.contentWQ.observe(viewLifecycleOwner){
            if(it.size>0)
                Log.d("TAG","data = ${it[0].data}")
            Log.d("OBSERVER", "data: $it")

            context?.let {_->
                adapterWQ.setNewItems(it)
                //adapter.differ.submitList(it)
               // adapterWQ.lastLoadedPosition = adapterWQ.itemCount
                Log.d("FRAGMENT", "scroll to position: ${adapterWQ.lastLoadedPosition}")
               // bckpData=it
            }
            initAdapter()


        }
        binding.operRefresher.setOnRefreshListener {
            viewModel.loadContent(user = App.user)
        }
    }

    override fun onActivityCreated() {
        viewModel.loadWorkplaces()
    }
    fun selectedWorkplace(item:Workplace){
        binding.apply {


            tvWorkplace.text = item.lname

            if(item.notificationStatus){
                ivNot.visibility = View.VISIBLE
            }else{
                ivNot.visibility = View.INVISIBLE
            }
            ivTypeWp.setImageResource(
                if(item.isRobot)
                    R.drawable.ic_roboticarm
                else
                    R.drawable.ic_handwork
            )

            if(item.state!="--"){
                tvWorkplaceState.text = item.state
                if(item.state.isNotEmpty()){
                    tvWorkplaceState.visibility=View.VISIBLE
                    tvWorkplaceState.backgroundTintList = ColorStateList.valueOf(item.getColorHex())
                }else {
                    context?.let {
                        tvWorkplaceState.visibility=View.INVISIBLE
                    }
                }}else
                context?.let {
                    tvWorkplaceState.text = ""
                    tvWorkplaceState.visibility=View.INVISIBLE
                }
        }


    }
    private fun initAdapter() {
        binding.operationRecycler.let {
            val lm = LinearLayoutManager(context)
            it.layoutManager=lm
            it.addItemDecoration(
                DividerItemDecoration(
                    context,
                    LinearLayoutManager.VERTICAL
                )
            )
            it.setHasFixedSize(true)
            it.adapter=adapterWQ
            
        }
    }
}