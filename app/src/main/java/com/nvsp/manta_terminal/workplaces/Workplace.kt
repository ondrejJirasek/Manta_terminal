package com.nvsp.manta_terminal.workplaces

import android.util.Log
import com.google.gson.annotations.SerializedName


data class Workplace(
    @SerializedName("id")
    val id:Int,
    @SerializedName("code")
    val code:String,
    @SerializedName("name")
    val lname:String,
    @SerializedName("state")
    val state:String,
    @SerializedName("color")
    val color:Int,
    @SerializedName("isRobot")
    val isRobot: Boolean,
    @SerializedName("notificationStatus")
    val notificationStatus:Boolean
){
    fun getColorHex():Int{
        //   val hexVal = Integer.toHexString(color)
        // return (0xff000000 + Integer.parseInt(hexVal,16)).toInt()
        val hexVal = color.toString()
        val r= (color ) and 0xff
        val g = (color shr 8) and 0xff
        val b = (color shr 16) and 0xff

       // Log.d(TAG, "HEX: $hexVal R:$r G:$g B:$b")
        //    (0xff000000 + Integer.parseInt(hexVal, 16)).toInt()*/
        return android.graphics.Color.rgb(r,g,b)
    }
    companion object{
        fun getUrl(id:Long):String = "Devices/$id/Workplaces"
    }
}
