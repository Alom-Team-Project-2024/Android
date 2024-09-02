package com.example.alom_team_project

import android.util.Log
import com.example.alom_team_project.chat.ChatData
import com.example.alom_team_project.chat.ChatMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class MyStompClient {
    // MessageListener 인터페이스 정의
    interface MessageListener {
        fun onMessageReceived(chatMessage: ChatMessage)
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val chatMessageAdapter = moshi.adapter(ChatMessage::class.java).lenient()

    private var session: StompSession? = null
    private var messageListener: MessageListener? = null

    fun setMessageListener(listener: MessageListener) {
        messageListener = listener
    }

    fun connect(token: String, chatRoomId: Long) = runBlocking {
        try {
            val client = StompClient(OkHttpWebSocketClient())
            session = client.connect(
                "ws://15.165.213.186:8080/ws-stomp",
                customStompConnectHeaders = mapOf("Authorization" to token)
            )
            Log.d("StompC", "Connected to WebSocket server with Authorization header")
            subscribeToChatRoom(chatRoomId) // 채팅방 ID를 사용하여 구독
        } catch (e: Exception) {
            Log.e("StompC", "Failed to connect", e)
        }
    }

    fun subscribeToChatRoom(roomId: Long) {
        session?.let { currentSession ->
            val destination = "/sub/chats/room/$roomId"
            val subscribeHeaders = StompSubscribeHeaders(destination)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    currentSession.subscribe(subscribeHeaders).collect { message ->
                        val rawMessage = message.body.toString()
                        Log.d("StompS", "Raw message: $rawMessage")

                        try {
                            val jsonPart = rawMessage.substringAfter("Text(text=").substringBeforeLast(")")
                            val chatMessage = chatMessageAdapter.fromJson(jsonPart)

                            Log.d("StompS", "Received message: $chatMessage")

                            // 메시지 수신을 리스너에 전달
                            chatMessage?.let {
                                withContext(Dispatchers.Main) {
                                    messageListener?.onMessageReceived(it)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("StompS", "Failed to parse message", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("StompS", "Failed to subscribe", e)
                }
            }
            Log.d("StompS", "Subscribed to chat room $roomId")
        } ?: Log.e("StompS", "Session is not initialized. Please connect first.")
    }

    fun sendMessage(chatRoomId: Long, sender: String, messageContent: String) {
        session?.let { currentSession ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val chatMessage = ChatMessage(chatRoomId, sender, messageContent)
                    Log.d("StompS", "ChatMessage before sending: $chatMessage")
                    val jsonMessage = chatMessageAdapter.toJson(chatMessage)
                    val sendHeaders = StompSendHeaders(destination = "/pub/chats/messages")
                    currentSession.send(sendHeaders, FrameBody.Text(jsonMessage))
                    Log.d("StompS", "Sent message: $jsonMessage")
                } catch (e: Exception) {
                    Log.e("StompS", "Failed to send message", e)
                }
            }
        } ?: Log.e("StompS", "Session is not initialized. Please connect first.")
    }
}

