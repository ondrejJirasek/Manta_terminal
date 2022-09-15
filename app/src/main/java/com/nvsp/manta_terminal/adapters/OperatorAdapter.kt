package com.nvsp.manta_terminal.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nvsp.manta_terminal.R
import com.nvsp.manta_terminal.databinding.ItemEmployeeBinding
import com.nvsp.manta_terminal.models.Operator
import com.nvsp.nvmesapplibrary.queue.NORMAL

import com.nvsp.nvmesapplibrary.queue.OPERATOR_MAIN_LOGGED
import com.nvsp.nvmesapplibrary.queue.OPERATOR_MAIN_NOT_LOGGED

class OperatorAdapter (val context:Context?, var mode:Int= NORMAL,val dataset:MutableList<Operator>, val itemClick:(Operator)->Unit, val  removeItem:(Operator)->Unit):RecyclerView.Adapter<OperatorAdapter.Holder>(){
    var loggedUserId:Int = -1
    var selectedItem : Operator? = null

    inner class Holder(itemView: View, val itemClick:(Operator)->Unit, val removeItem:(Operator)->Unit):RecyclerView.ViewHolder(itemView){
        val binding=ItemEmployeeBinding.bind(itemView)

         fun setOperatorMainWQMode(item:Operator) {
             if(item.id!=loggedUserId){
                 binding.btnRemove.visibility=View.INVISIBLE
             }else
                 binding.btnRemove.visibility=View.VISIBLE

           // binding.btnRemove.visibility = View.VISIBLE
            binding.btnRemove.setOnClickListener {
                Log.d("ITEM", "CLICK ON ")
            }
        }
         fun setOperatorMainWQModeNotLogged(item:Operator) {
            binding.btnRemove.visibility = View.INVISIBLE

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding=ItemEmployeeBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(binding.root, itemClick, removeItem )
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = dataset[position]
        with(holder.binding){
            when(mode){
                NORMAL->{
                    btnRemove.visibility = View.VISIBLE
                    btnRemove.setOnClickListener {
                        Log.d("ITEM", "CLICK ON ")
                    }
                }
                OPERATOR_MAIN_LOGGED->{
                    holder.setOperatorMainWQMode(item)
                }
                OPERATOR_MAIN_NOT_LOGGED->{
                    holder.setOperatorMainWQModeNotLogged(item)
                }
            }
            tvname.text = item.nameEM
            tvState.text = item.statusEM

            if(item.statusEM.isNotEmpty()){
                tvState.visibility=View.VISIBLE
                item.getColorHex()?.let {
                    tvState.backgroundTintList = ColorStateList.valueOf(it)
                }
            }else{
                tvState.visibility=View.INVISIBLE
            }
            layoutEmployee.setOnClickListener {
                Log.d("OPERATOR CLICK", "operatorClick")
                selectedItem = item
                itemClick(item)
                notifyDataSetChanged()
            }
            btnRemove.setOnClickListener {
                selectedItem=null
                Log.d("OPERATOR CLICK remove", "operatorClickremove")
                removeItem(item)
            }
            context?.let {
                if(item.id == selectedItem?.id){
                    layoutEmployee.setBackgroundColor(it.getColor(R.color.aquariumAlpha40))
                }else{
                    layoutEmployee.setBackgroundColor(it.getColor(com.nvsp.nvmesapplibrary.R.color.zxing_transparent))
                }
            }


        }
    }
    fun changeMode(mMode:Int, idEmployee:Long?){
        mode=mMode
        loggedUserId=(idEmployee?:(-1)).toInt()
        notifyDataSetChanged()
    }
    fun setNewItems(dataSetNew: List<Operator>) {
        val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return dataset.size
            }
            override fun getNewListSize(): Int {
                return dataSetNew.size
            }
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val state =
                    dataset[oldItemPosition].id.equals(dataSetNew[newItemPosition].id)
                return state
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val state = dataset[oldItemPosition] == dataSetNew[newItemPosition]
                return state
            }
        })
        dataset.clear()
        dataset.addAll(dataSetNew)
        diffUtil.dispatchUpdatesTo(this)

    }
}