package com.example.myapplication

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import kotlin.math.pow
import kotlin.reflect.full.functions

class ListeningService: Service() {

    val CHANNEL_ID = "ListeningServiceNotification"
    val PREFS_FILE = "com.example.myapplication.prefs"
    var prefs: SharedPreferences? = null
    val PORT = "port"
    lateinit var server : SocketServer

    private fun createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "ListeningService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onCreate() {

        prefs = this.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("ListeningService")
            .setContentText("port: "+ prefs!!.getString(PORT,""))
            .setSmallIcon(R.drawable.ic_hearing_black_24dp)
            .build()
        startForeground(1, notification)

        server = SocketServer(this)
        server.start()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {

        server.interrupt()
        super.onDestroy()
    }
}

class SocketServer(context: Context) : Thread(){

    val PREFS_FILE = "com.example.myapplication.prefs"
    val prefs:SharedPreferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    val PORT = "port"
    val port = prefs.getString(PORT,"")!!.toInt()
    val context_ = context

    override fun run(){
        val server = ServerSocket(port)
        println("Server is running on port ${server.localPort}")

        while (true) {
            val client = server.accept()
            println("Client connected: ${client.inetAddress.hostAddress}")

            // Run client in it's own thread.
            thread { ClientHandler(context_,client).run() }
        }
    }
}

class ClientHandler(context:Context,client_: Socket) {

    private val client: Socket = client_
//    private val reader: Scanner = Scanner(client.getInputStream())
//    private val reader = client.getInputStream()
    private val writer: OutputStream = client.getOutputStream()
    private var running: Boolean = false
    private val PREFS_FILE = "com.example.myapplication.prefs"
    private val prefs:SharedPreferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    private val RES_BITS = "res_bits"
    private val FLAG_BITS = "flag_bits"
    val context_ = context

    fun run() {

        running = true

        while (running) {

            try {
                Log.v("running",client.toString())
                    val reader = client.getInputStream()
                    val bytes_buffer = ByteArray(20)
                    reader.read(bytes_buffer)
                    var command_bytes = ""
                    var params = ""

                    val bytes_list = bytes_buffer.toUByteArray().dropLast(18-bytes_buffer[1].toInt())
                    for (each in bytes_list) {
                        Log.v("Reader","value: ${each.toString(16).toUpperCase()}")
                    }
//                    Log.v("Reader","size " + bytes_list.size.toString())
//                    Log.v("Command code",bytes_list.slice(2..3).toString())

                    for (i in 2..3){

                        if (bytes_list[i].toString(16).length == 1) command_bytes += "0" + bytes_list[i].toString(16).toUpperCase()
                        else command_bytes += bytes_list[i].toString(16).toUpperCase()
                    }
                if (prefs.getBoolean(FLAG_BITS,true)) {
                    for (i in 4 until bytes_list.size){

                        if (bytes_list[i].toString(16).length == 1) params += "0"+bytes_list[i].toString(16).toUpperCase()
                        else params += bytes_list[i].toString(16).toUpperCase()
                    }
//                    Log.v("Command code",bytes_list.slice(4 until bytes_list.size).toString())
//                    Log.v("Params",params)
//                Log.v("Command code",command_code)
//                val text = reader.nextLine()
//                if (text == "EXIT"){
//
//                    shutdown()
//                    continue
//                }
//                if (prefs.getBoolean(FLAG_BITS,true)) {

                    val out_l = prefs.getString(RES_BITS, "")!!.toInt() / 8
//                    val values = text.split(' ')
//                    Log.v("Client Handler", "values : $values")
                    val command_code = prefs.getString(command_bytes, "")
                    Log.v("Client Handler", "command : $command_code")
                    if (command_code != "") {

                        val functionsClass = Calculator2(params, out_l)
                        val reflectFunction = Calculator2::class.functions.find { it.name == command_code }
                        val s = reflectFunction?.call(functionsClass) as String

                        if (s == "") {

                            val editor = prefs.edit()
                            editor.putBoolean(FLAG_BITS,false)
                            editor.apply()
                            val intent = Intent(context_, DialogAlert::class.java)
                            context_.startActivity(intent)
                            while (!prefs.getBoolean(FLAG_BITS,false)) sleep(1000)
                            var out_ = java.lang.Integer.toHexString(prefs.getString(RES_BITS, "")!!.toInt())
                            println(prefs.getString(RES_BITS, "")!!.toInt())
                            println(out_)
                            if (out_.length < out_l) out_ = out_.padStart(out_l, '0').toUpperCase()
                            else out_ = out_.takeLast(out_l).toUpperCase()
                            write(command_bytes,out_)
                        } else write(command_bytes, s)
                    } else write(command_bytes, "0BAD")
                } else write(command_bytes,"DEAD")

            } catch (ex: Exception) {

                ex.printStackTrace()
                // TODO: Implement exception handling
                shutdown()
            }
            finally {

            }
        }
    }

    private fun write(command:String, message: String) {
        val len = command.length + message.length
        var len_l : String
        val byte = ByteArray (len/2 + 2)
        var buff = ByteBuffer.wrap(byte)
        val TAG = "Writer"
//        val



        if (len.toString(16).length < 4) len_l = len.toString(16).padStart(4, '0').toUpperCase()
        else len_l = len.toString(16).takeLast(4).toUpperCase()

        for ( i in 0 until len_l.length step 2){
            Log.v(TAG,len_l[i].toString()+len_l[i+1].toString())
            buff.put(len_l.slice(i..i+1).toInt(16).toByte())

        }

        for ( i in 0 until command.length step 2){
            Log.v(TAG,command[i].toString()+command[i+1].toString())
            buff.put(command.slice(i..i+1).toInt(16).toByte())
        }

        for ( i in 0 until message.length step 2){
            Log.v(TAG,message[i].toString()+message[i+1].toString())
            buff.put(message.slice(i..i+1).toInt(16).toByte())
        }

//        len.toByte()
//        buff.put(len.toByte())
//        buff.put(command.toLong(16).toByte())
//        buff.put(message.toLong(16).toByte())
        val out_array = buff.array()
//        val TAG = "Writer"
//
//        Log.v(TAG,"Command: $command len ${command?.length} and Message: $message len ${message.length}")
//        Log.v(TAG,"Command to hex: ${command?.toInt(16)?.toString(16)}")

        for (each in out_array){
            Log.v(TAG,each.toString())
        }

//        writer.write(("$command $message \n").toByteArray(Charset.defaultCharset()))
        writer.write(out_array)
    }

    private fun shutdown() {

        running = false
        client.close()
        println("${client.inetAddress.hostAddress} closed the connection")
    }


}
//
//fun ClientHandler.getSocket() : Socket {
//    return ClientHandler.client
//}

//
class Calculator2(a : String = "", length : Int = 4){

    private val values = a.chunked(a.length/2)
    private val l = length

    fun set_bits():String{

        Log.e("Calculator2","sel_bits call")
        return ""
    }


    private fun calculation_out (a: Long, length: Int = l) :String{

        val result = a.toString(16)
        if (result.length < length) return result.padStart(length, '0').toUpperCase()
        else return result.takeLast(length).toUpperCase()
    }

    fun sum() :String {

//        Log.e("Calculator2","add call. ${values[0]} ${values[1]}")

        val result = values[0].toLong(16) + values[1].toLong(16)
//        println(result)

        return calculation_out(result)
    }

    fun pow() :String {

        Log.e("Calculator2","pow call")
        val result = values[0].toLong(16).toDouble().pow(values[1].toLong(16).toDouble())
//        println(result)

        return calculation_out(result.toLong())
    }

    fun mul() :String {

        Log.e("Calculator2","mul call")
        val result = values[0].toLong(16) * values[1].toLong(16)
//        println(result)

        return calculation_out(result)
    }
}