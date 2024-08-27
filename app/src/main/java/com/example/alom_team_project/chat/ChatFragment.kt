package com.example.alom_team_project.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.alom_team_project.MyStompClient
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.chat.dialog.CustomDialogC
import com.example.alom_team_project.chat.dialog.CustomDialogR
import com.example.alom_team_project.chat.dialog.UserProfileActivity
import com.example.alom_team_project.databinding.AssessDialogBinding
import com.example.alom_team_project.databinding.FragmentChatBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var assessDialog: AssessDialogBinding
    private val chattingList = ArrayList<ChatData>()
    private lateinit var adapter: ChatAdapter
    private lateinit var stompClient: MyStompClient
    private lateinit var binding: FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 레이아웃 인플레이트
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stompClient = MyStompClient()
        val token = getJwtToken()
        val chatRoomId = arguments?.getLong("ChatRoomId") ?: 0L
        val myNick = getUsernick()!!
        Log.d("MyNick", "$myNick")

        Log.d("ChatFragment", "Received ChatRoom ID: $chatRoomId")

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (token.isNotEmpty()) {
                    stompClient.connect(token)
                    Log.d("WebsocketM", "Connected to STOMP server")
                } else {
                    Log.e("Token Error", "Token is empty, cannot connect to STOMP server")
                }
            } catch (e: Exception) {
                Log.e("WebsocketM", "Error during STOMP connection or subscription", e)
            }
        }

        if (chatRoomId != 0L) {
            setupUserProfile(token, chatRoomId, myNick)
        }
        setupRecyclerView()

        fetchChatHistory(token, chatRoomId, myNick)

        binding.sendBtn.setOnClickListener {
            val messageContent = binding.etMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                chattingList.add(ChatData(messageContent, 1))
                adapter.notifyDataSetChanged()
                binding.rcvChatting.scrollToPosition(chattingList.size - 1)

                Log.d("SendMessage", "Sending message: $messageContent")

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        stompClient.sendMessage(chatRoomId, myNick, messageContent)
                        Log.d("WebsocketM", "Sent message: $messageContent")
                    } catch (e: Exception) {
                        Log.e("WebsocketM", "Error sending message", e)
                    }
                }

                binding.etMessage.setText("")
            }
        }

        binding.backIcon.setOnClickListener {
            val intent = Intent(requireActivity(), ChatListActivity::class.java)
            startActivity(intent)
        }

        setupBottomSheet()

        binding.menuBtn.setOnClickListener {
            bottomSheetDialog.show()
        }

        binding.profileImg.setOnClickListener {
            val intent = Intent(requireActivity(), UserProfileActivity::class.java)
            startActivity(intent)
        }

        binding.nicknameTv.setOnClickListener {
            val intent = Intent(requireActivity(), UserProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupUserProfile(token: String, chatRoomId: Long, nickname: String) {
        if (token.isNotEmpty()) {
            val userInfo = RetrofitClient.instance.create(ChatService::class.java)
            val call = userInfo.getChatRoomById("Bearer $token", chatRoomId)

            call.enqueue(object : Callback<ChatRoomResponse> {
                override fun onResponse(call: Call<ChatRoomResponse>, response: Response<ChatRoomResponse>) {
                    if (response.isSuccessful) {
                        val chatRoom: ChatRoomResponse? = response.body()
                        chatRoom?.let {
                            val nicknames = it.userResponseList.map { user -> user.nickname }
                            val profileImages = it.userResponseList.map { user -> user.profileImage }

                            val firstUsername = nicknames.getOrNull(0)
                            val secondUsername = nicknames.getOrNull(1)

                            val firstUserProfile = profileImages.getOrNull(0)
                            val secondUserProfile = profileImages.getOrNull(1)

                            val chatTitle = if (nickname == firstUsername) secondUsername else firstUsername
                            val profile = if (nickname == firstUsername) secondUserProfile else firstUserProfile

                            Log.d("Chat", "User: $chatTitle")

                            binding.nicknameTv.text = chatTitle

                            Glide.with(requireContext())
                                .load(profile)
                                .into(binding.profileImg)
                        }
                    } else {
                        Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ChatRoomResponse>, t: Throwable) {
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("Token Error", "Token is empty")
        }
    }


    private fun setupRecyclerView() {
        adapter = ChatAdapter(chattingList)
        binding.rcvChatting.adapter = adapter
        binding.rcvChatting.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchChatHistory(token: String, chatRoomId: Long, nickname: String) {
        if (token.isNotEmpty()) {
            val chatHistory = RetrofitClient.instance.create(ChatService::class.java)
            val call = chatHistory.getChatHistory("Bearer $token", chatRoomId)

            call.enqueue(object : Callback<List<ChatHistoryResponse>> {
                override fun onResponse(call: Call<List<ChatHistoryResponse>>, response: Response<List<ChatHistoryResponse>>) {
                    if (response.isSuccessful) {
                        val chatMessages = response.body()
                        if (chatMessages != null) {
                            chattingList.clear()

                            chatMessages.forEach { chatHistoryResponse ->
                                val chatSender = chatHistoryResponse.sender
                                val message = chatHistoryResponse.message
                                Log.d("msg", "$message")

                                if (chatSender != nickname) {
                                    chattingList.add(
                                        ChatData(
                                            chat = message,
                                            viewType = 0
                                        )
                                    )
                                }
                                else {
                                    chattingList.add(
                                        ChatData(
                                            chat = message,
                                            viewType = 1
                                        )
                                    )
                                }
                            }
                        }
                        adapter.notifyDataSetChanged()

                    } else {
                        Log.e("API Error", "Response Code: ${response.code()}, Message: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<List<ChatHistoryResponse>>, t: Throwable) {
                    Log.e("API Error", "Failure: ${t.message}")
                }
            })
        } else {
            Log.e("Token Error", "Token is empty")
        }

    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.chat_btm_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<AppCompatButton>(R.id.rating_btn).setOnClickListener {
            bottomSheetDialog.dismiss()
            showRatingDialog()
        }

        bottomSheetView.findViewById<AppCompatButton>(R.id.success_btn).setOnClickListener {
            bottomSheetDialog.dismiss()
            showConfirmDialog()
        }
    }

    private fun showConfirmDialog() {
        val dialogViewC = layoutInflater.inflate(R.layout.confirm_dialog, null)
        val dialogC = CustomDialogC(requireContext())

        dialogC.setItemClickListener(object : CustomDialogC.ItemClickListener {
            override fun onClick(message: String) {
                //
            }
        })
        dialogC.show()
    }

    private fun showRatingDialog() {
        val dialogR = CustomDialogR(requireContext())
        dialogR.setContentView(R.layout.assess_dialog)

        val chatNick = binding.nicknameTv.text.toString()
        Log.d("temperature", "$chatNick")
        dialogR.setChatNick(chatNick)


        val ratingBar = dialogR.findViewById<RatingBar>(R.id.ratingBar)
        val button3 = dialogR.findViewById<Button>(R.id.button3)
        val buttonX = dialogR.findViewById<Button>(R.id.buttonX)

        dialogR.show()
    }

    private fun getJwtToken(): String {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun getUsernick(): String? {
        val sharedPref = requireContext().getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
        return sharedPref.getString("nickname", null)
    }
}