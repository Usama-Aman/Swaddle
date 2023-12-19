package com.android.swaddle.networkManager


import android.util.Log
import com.android.swaddle.utils.Constants.SOCKET_URL

import com.github.nkzawa.socketio.client.IO
import org.json.JSONObject

class IOSocketManager {

    private val mSocket = IO.socket(SOCKET_URL) /// enter you base URL here

    fun init(name: String, callback: (JSONObject) -> Unit) {

        if (!mSocket.connected()) {
            mSocket.connect()
            Log.e("Socket", "Connected")

        }
        mSocket.on(name) { args -> callback(JSONObject(args.first().toString())) }
    }

    fun send(name: String, data: JSONObject) {
        if (!mSocket.connected()) {
            mSocket.connect()
        }
        mSocket.emit(name, data)
    }

    fun disconnect() {
        mSocket.close()
        mSocket.disconnect()
        Log.e("Socket", "Disconnected")
    }
}

class SocketNames {
    companion object {
        var socketReceiveMessage =
            "new_message_receive"

        var socketSendMessage =
            "new_message_send"

        var socketSendNotification =
            "notification_send"

        var socketReceiveNotification =
            "notification_receive"
    }
}