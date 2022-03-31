package com.nvsp.manta_terminal.workplaces

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nvsp.manta_terminal.databinding.ItemWorkplaceBinding
import com.nvsp.nvmesapplibrary.App
import com.nvsp.nvmesapplibrary.R

class WorkplaceAdapter(val selectItemCallback:(Workplace)->Unit) : RecyclerView.Adapter<WorkplaceAdapter.WpHolder>() {
    private var wpList= mutableListOf<Workplace>()
    var selectedItem:Workplace?=null

    fun setNewList(dataSetNew: MutableList<Workplace>){
        val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback(){
            override fun getOldListSize(): Int {
                return wpList.size
            }

            override fun getNewListSize(): Int {
                return  dataSetNew.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return wpList[oldItemPosition].id == dataSetNew[newItemPosition].id &&
                        wpList[oldItemPosition].color == dataSetNew[newItemPosition].color&&
                        wpList[oldItemPosition].isRobot == dataSetNew[newItemPosition].isRobot&&
                        wpList[oldItemPosition].notificationStatus == dataSetNew[newItemPosition].notificationStatus&&
                        wpList[oldItemPosition].code == dataSetNew[newItemPosition].code&&
                        wpList[oldItemPosition].lname == dataSetNew[newItemPosition].lname&&
                        wpList[oldItemPosition].state == dataSetNew[newItemPosition].state
            }


            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return wpList[oldItemPosition] == dataSetNew[newItemPosition]
            }

        })
        wpList.clear()
        wpList.addAll(dataSetNew)
        if(wpList.size>0 && selectedItem == null)
            {selectItem(wpList[0])}
        Log.d("ADAPTER", "resultsize: ${wpList.size}")
        diffUtil.dispatchUpdatesTo(this)

    }
    fun selectItem(item:Workplace){
        selectedItem=item
        selectItemCallback(item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WpHolder {
        val itemBinding = ItemWorkplaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WpHolder(itemBinding){
            selectItem(it)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: WpHolder, position: Int) {
        val wpBean: Workplace = wpList[position]
        holder.bind(wpBean, wpBean.id==selectedItem?.id)
    }


    override fun getItemCount(): Int = wpList.size

    class WpHolder(private val itemBinding: ItemWorkplaceBinding, val select:(item:Workplace)->Unit) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(wpBeam: Workplace, isSelected:Boolean) {
            itemBinding.apply {
                tvWorkplaceLabel.text = wpBeam.code
                tvWorkplaceLabel.setTextColor(wpBeam.getColorHex())
                if(isSelected){
                    root.background= (ContextCompat.getDrawable(App.appContext,R.drawable.background_dark_radius_top_no_border))
                }else
                    root.setBackgroundColor(ContextCompat.getColor(App.appContext,R.color.zxing_transparent))

                root.setOnClickListener {
                    select(wpBeam)
                }
            }

            //itemBinding.tvPaymentAmount.text = paymentBean.totalAmount
        }
    }
}