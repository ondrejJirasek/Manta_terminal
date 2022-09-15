package com.nvsp.manta_terminal.models

import android.graphics.Color
import android.util.Log
import com.google.gson.annotations.SerializedName

data class Operator(
    @SerializedName("ID")
    val id: Int,
    @SerializedName("Code_EM")
    val codeEm:Int,
    @SerializedName("Name_EM")
    val nameEM: String,
    @SerializedName("Code_Unit")
    val codeUnit:String?,
    @SerializedName("Name_Unit")
    val nameUnit:String?,
    @SerializedName("Status_EM")
    val statusEM:String,
    @SerializedName("Color")
    val color: Int?
) {
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