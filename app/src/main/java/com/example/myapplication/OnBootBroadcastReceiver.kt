package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class OnBootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context, p1: Intent) {

        if (Intent.ACTION_BOOT_COMPLETED == p1.action){

            Toast.makeText(p0, "BOOT completed you have not w8 enough", Toast.LENGTH_LONG).show()
            val listeningService = Intent(p0,ListeningService::class.java)
            p0.startForegroundService(listeningService)
        }
    }
}