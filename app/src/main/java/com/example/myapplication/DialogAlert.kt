package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog

class DialogAlert : Activity (){

    val TAG = "DialogAlert"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog)

        val PREFS_FILE = "com.example.myapplication.prefs"
        val prefs: SharedPreferences = this.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val RES_BITS = "res_bits"
        val FLAG_BITS = "flag_bits"
        val out_l = prefs.getString(RES_BITS, "")!!.toInt() / 8
        val editor = prefs.edit()

//        ClientHandler.getSocket()
        val builder = AlertDialog.Builder(this)
        val bits = arrayOf("8","16", "24", "32","40","48","56","64")
        builder.setItems(bits){dialog, item ->
            Log.v("DialogAlert","Res bits set to ${bits[item]}")
            editor.putBoolean(FLAG_BITS,true)
            editor.putString(RES_BITS,bits[item])
            editor.apply()
            dialog.dismiss()
            this.finish()
        }
        builder.show()
    }
}