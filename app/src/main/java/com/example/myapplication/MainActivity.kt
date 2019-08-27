package com.example.myapplication

import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {

    val PREFS_FILE = "com.example.myapplication.prefs"
    val RES_BITS = "res_bits"
    val PORT = "port"
    var prefs: SharedPreferences? = null

    val ACTION = "com.example.myapplication.set_bits"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        prefs = this.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        setContentView(R.layout.activity_main)
        val filter = IntentFilter(ACTION)
//        registerReceiver(setBitsBroadcastReceiver,filter)

    }

    override fun onResume() {

        Log.v("MainActivity","onResume "+intent?.getStringExtra("setbits"))

//        if (intent.getBooleanExtra("setbits",false)){
//            Log.v("MainActiity","go go go alert dialog")
//            val builder = AlertDialog.Builder(this)
//            val bits = arrayOf("8","16","64")
//            builder.setItems(bits){dialog, item ->
//                Log.v("DialogAlert","Res bits set to ${bits[item]}")
//                dialog.dismiss()
//            }
//            builder.show()
//        }
        super.onResume()
    }

    class GetAsyncXML : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg p0: String?): String {

            val fullUrl= p0[0]+"/config.xml"
            Log.v("getAsync",p0[0].toString())
            val client = OkHttpClient.Builder()
//                .connectTimeout(60,TimeUnit.SECONDS)
//                .writeTimeout(60,TimeUnit.SECONDS)
//                .readTimeout(60,TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url(fullUrl)
                .build()
            Log.e("getXML","url: $fullUrl")
            val call : Call = client.newCall(request)
            val myResponse : Response = call.execute()

            return myResponse.body()!!.string()
        }

    }

    private fun parseXML(xml:String) {

        Log.v("parseXML",xml)
        val streamXML : ByteArrayInputStream = xml.toByteArray().inputStream()
        val xmlDoc : Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(streamXML)
        xmlDoc.documentElement.normalize()
        val editor = prefs!!.edit()

        Log.v("parseXML",xmlDoc.documentElement.nodeName)
        Log.v("parseXML","port = "+xmlDoc.documentElement.getAttribute("port"))
        editor.putString(PORT,xmlDoc.documentElement.getAttribute("port"))
        Log.v("parseXML","res_bits = " + xmlDoc.documentElement.getAttribute("res_bits"))
        editor.putString(RES_BITS,xmlDoc.documentElement.getAttribute("res_bits"))

        val cmdList : NodeList = xmlDoc.getElementsByTagName("cmd")
        var cmdNode:Node
        var method :String
        var value: String

        for (i in 0 until cmdList.length) {

            cmdNode = cmdList.item(i)
            method = cmdNode.attributes.getNamedItem("method").nodeValue
            value = cmdNode.attributes.getNamedItem("value").nodeValue
            Log.v("parseXML","cmd $i : $value - > $method")
            editor.putString(value,method)
        }
        editor.apply()
    }

    private fun changeBroadCastFlag(state:Boolean){

        Log.v("changeBroadCastFlag","incoming state: $state")
        val broadcastState = when (state) {

            true -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            false -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        val componentName = ComponentName(this,OnBootBroadcastReceiver::class.java)
        Log.v("changeBroadCastFlag","before :"+packageManager.getComponentEnabledSetting(componentName).toString())
        packageManager.setComponentEnabledSetting(componentName,broadcastState,PackageManager.DONT_KILL_APP)
        Log.v("changeBroadCastFlag","after :"+packageManager.getComponentEnabledSetting(componentName).toString())
    }

    fun startService(v:View){

        val urlConfig:EditText = findViewById(R.id.config_url)
        val task = GetAsyncXML()
        task.execute(urlConfig.text.toString())
        val stringXML = task.get()
//        val stringXML = "<config port='2222' res_bits='32'>\n" +
//                "<cmd value='00A1' method='sum'/>\n" +
//                "<cmd value='00B1' method='mul'/>\n" +
//                "<cmd value='00С1' method='pow'/>\n" +
//                "<cmd value='00D1' method='set_bits'/>\n" +
//                "</config>"

        parseXML(stringXML)
        changeBroadCastFlag(true)
        val serviceIntent = Intent(this,ListeningService::class.java)
        startService(serviceIntent)

        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(startMain)
    }

    fun stopService(v:View){

        val serviceIntent = Intent(this,ListeningService::class.java)
        stopService(serviceIntent)
        changeBroadCastFlag(false)
    }
}


//                        Log.v("DialogAlert","1")
//                        val builder = AlertDialog.Builder(context_)
//                        Log.v("DialogAlert","2")
//                        val bits = arrayOf("8","16","64")
//                        Log.v("DialogAlert","3")
//                        builder.setItems(bits){dialog, item ->
//                                Log.v("DialogAlert","Res bits set to ${bits[item]}")
//                                dialog.dismiss()
//                        }
//                        builder.show().window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
//                        Log.v("DialogAlert","4")
//                        builder.show()
//                        Log.v("DialogAlert","5")
