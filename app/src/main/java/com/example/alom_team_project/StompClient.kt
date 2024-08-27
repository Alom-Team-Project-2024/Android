package com.example.alom_team_project

import android.util.Log
import com.example.alom_team_project.chat.ChatMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.frame.StompFrame
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

class MyStompClient {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val chatMessageAdapter = moshi.adapter(ChatMessage::class.java)
    private var session: StompSession? = null // 세션을 nullable로 유지
    private val coroutineScope = CoroutineScope(Dispatchers.IO) // CoroutineScope 생성

    // STOMP 서버에 연결하는 메서드
    fun connect(token: String) = runBlocking {
        try {
            val client = StompClient(OkHttpWebSocketClient())
            session = client.connect(
                "ws://15.165.213.186:8080/ws-stomp",
                customStompConnectHeaders = mapOf("Authorization" to token)
            )
            Log.d("MyStompClient", "Connected to WebSocket server with Authorization header")
        } catch (e: Exception) {
            Log.e("MyStompClient", "Failed to connect: ${e.message}")
        }
    }

    // 구독을 설정하는 메서드
    fun subscribe(token: String, roomId: String) {
        session?.let { currentSession ->
            coroutineScope.launch {
                try {
                    val subscribeHeaders = StompSubscribeHeaders(
                        destination = "/sub/chats/room/$roomId", // URL 수정
                        customHeaders = mapOf("Authorization" to token)
                    )

                    val messageFlow: Flow<StompFrame.Message> =
                        currentSession.subscribe(subscribeHeaders)

                    messageFlow.collect { frame ->
                        val chatMessage = chatMessageAdapter.fromJson(frame.bodyAsText)
                        if (chatMessage != null) {
                            Log.d("WebsocketS", "Received message: $chatMessage")
                        } else {
                            Log.e("WebsocketS", "Failed to parse message: ${frame.bodyAsText}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WebsocketS", "Failed to subscribe: ${e.message}")
                }
            }
        } ?: Log.e("WebsocketS", "Session is not initialized. Please connect first.")
    }

    // 서버로 메시지를 보내는 메서드
    fun sendMessage(chatRoomId: Long, sender: String, messageContent: String) {
        session?.let { currentSession ->
            coroutineScope.launch {
                try {
                    val chatMessage = ChatMessage(chatRoomId, sender, messageContent)
                    val jsonMessage = chatMessageAdapter.toJson(chatMessage)
                    val sendHeaders = StompSendHeaders(
                        destination = "/pub/chats/messages",
                    )
                    currentSession.send(sendHeaders, FrameBody.Text(jsonMessage))
                    Log.d("WebsocketS", "Sent message: $jsonMessage")
                } catch (e: Exception) {
                    Log.e("WebsocketS", "Failed to send message: ${e.message}")
                }
            }
        } ?: Log.e("WebsocketS", "Session is not initialized. Please connect first.")
    }
}