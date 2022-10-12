package com.nvsp.manta_terminal.models

import android.graphics.Color
import android.util.Log
import com.google.gson.annotations.SerializedName

data class Operator(
    @SerializedName("id")
    val id: Int,
    @SerializedName("emCode")
    val codeEm:String,
    @SerializedName("emName")
    val nameEM: String,
    @SerializedName("unitCode")
    val codeUnit:String?,
    @SerializedName("unitName")
    val nameUnit:String?,
    @SerializedName("emStatus")
    val statusEM:String,
    @SerializedName("color")
    val color:Int?,
    @SerializedName("wpId")
    val wpId:Int?
) {
    companion object{
        fun getUrl(wpId:Int) = "Employees/Workplace/$wpId"
    }
    fun getColorHex(): Int? {
        Log.d("TASK_COLOR", "color is $color")
        if (color != null)
            color.let {

                val hexVal = color.toString(16)

                val r = (color) and 0xff
                val g = (color shr 8) and 0xff
                val b = (color shr 16) and 0xff

                //    (0xff000000 + Integer.parseInt(hexVal, 16)).toInt()*/
                return Color.rgb(r, g, b)
            }
        else {

            return null
        }
    }
}