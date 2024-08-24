package com.example.setong_alom.chat

import CustomDialogC
import CustomDialogR
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.setong_alom.MyStompClient
import com.example.setong_alom.R
import com.example.setong_alom.RetrofitClient
import com.example.setong_alom.chatlist.ChatListActivity
import com.example.setong_alom.databinding.AssessDialogBinding
import com.example.setong_alom.databinding.FragmentChatBinding
import com.example.setong_alom.dialog.UserProfileActivity
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

        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIzMDExNjc2Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXI0OTciLCJpYXQiOjE3MjQ1MjIwMjIsImV4cCI6MTcyNDUyMzgyMn0.qtoPzhPkIA6NBlUsHaI1HqB6X3qXwaC3gnw8K72J8B8"

        val chatRoomId = arguments?.getLong("ChatRoomId") ?: 0L
        val myNick = "user497"

        Log.d("ChatFragment", "Received ChatRoom ID: $chatRoomId")

        // STOMP 클라이언트 연결 및 구독
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

        binding.sendBtn.setOnClickListener {
            if (binding.etMessage.length() > 0) {
                chattingList.add(ChatData(binding.etMessage.text.toString(), 1))
                adapter.notifyDataSetChanged()
                binding.etMessage.setText("")
                binding.rcvChatting.scrollToPosition(chattingList.size - 1)

                val messageContent = binding.etMessage.text.toString()
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        stompClient.sendMessage(messageContent)
                        Log.d("WebsocketM", "Sent message: $messageContent")
                    } catch (e: Exception) {
                        Log.e("WebsocketM", "Error sending message", e)
                    }
                }
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
            val call = userInfo.getChatRoomById(token, chatRoomId)

            call.enqueue(object : Callback<ChatRoomResponse> {
                override fun onResponse(call: Call<ChatRoomResponse>, response: Response<ChatRoomResponse>) {
                    if (response.isSuccessful) {
                        val chatRoom: ChatRoomResponse? = response.body()
                        if (chatRoom != null) {
                            val nicknames: List<String> = chatRoom.userResponseList.map { it.nickname }
                            val profileImages: List<String> = chatRoom.userResponseList.map { it.profileImage }

                            val firstUsername: String? = nicknames.getOrNull(0)
                            val secondUsername: String? = nicknames.getOrNull(1)

                            val firstUserProfile: String? = profileImages.getOrNull(0)
                            val secondUserProfile: String? = profileImages.getOrNull(1)

                            var chatTitle: String? = null
                            var profile: String? = null

                            if (nickname == firstUsername) {
                                chatTitle = secondUsername
                                profile = secondUserProfile
                            } else {
                                chatTitle = firstUsername
                                profile = firstUserProfile
                            }

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

        val ratingBar = dialogR.findViewById<RatingBar>(R.id.ratingBar)
        val button3 = dialogR.findViewById<Button>(R.id.button3)
        val buttonX = dialogR.findViewById<Button>(R.id.buttonX)

        dialogR.show()
    }
}

