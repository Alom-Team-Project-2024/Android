package com.example.alom_team_project.question_board


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.FragmentAnswerBinding
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AnswerFragment : Fragment() {

    private var isRecyclerViewInitialized = false

    //새로고침
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val refreshInterval: Long = 500 // 0.5초

    private var _binding: FragmentAnswerBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter
    private lateinit var adapter: AnswerAdapterClass
    private lateinit var answerList: ArrayList<Reply>
    private lateinit var answerService: AnswerPostService
    private var isLiked = false
    private var isScrapped = false

    //이미지 선택
    private var selectedImageUris: MutableList<Uri> = mutableListOf()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    // 권한 요청 런처
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // 권한이 허용된 경우
                openGallery()
            } else {
                Toast.makeText(context, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAutoRefresh()

        // 뒤로가기 버튼 설정
        val btnBack: ImageButton = view.findViewById(R.id.back_button)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Retrofit 초기화
        answerService = RetrofitClient.instance.create(AnswerPostService::class.java)

        // Arguments에서 questionId 받기
        val questionId = arguments?.getLong("QUESTION_ID")
        //Log.d("AnswerFragment", "Question ID: $questionId")

        // RecyclerView 초기화
        binding.imagePreview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        imagePreviewAdapter = ImagePreviewAdapter(emptyList())  // 초기에는 빈 리스트
        binding.imagePreview.adapter = imagePreviewAdapter

        // 이미지 선택 후 RecyclerView 업데이트
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                val imageUris = mutableListOf<Uri>()

                if (clipData != null) {
                    // 여러 장의 이미지를 선택한 경우
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        imageUris.add(item.uri)
                    }
                } else {
                    // 단일 이미지를 선택한 경우
                    result.data?.data?.let { uri ->
                        imageUris.add(uri)
                    }
                }

                selectedImageUris = imageUris
                // RecyclerView의 어댑터를 업데이트
                binding.imagePreview.visibility= View.VISIBLE
                imagePreviewAdapter = ImagePreviewAdapter(imageUris)
                binding.imagePreview.adapter= imagePreviewAdapter
            }
        }

        // 좋아요 및 스크랩 상태 초기화
        if (questionId != null) {
            isLiked = getLikeStatus(questionId)
            isScrapped = getScrapStatus(questionId)
            updateLikeButtonUI(isLiked)
            updateScrapButtonUI(isScrapped)
        }

        // 좋아요 버튼 클릭 이벤트
        binding.likeButton.setOnClickListener {
            questionId?.let { id ->
                if (!isLiked) {
                    postLike(id)
                } else {
                    Toast.makeText(requireContext(), "이미 좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 스크랩 버튼 클릭 이벤트
        binding.scrapButton.setOnClickListener {
            val username = getUsername()
            questionId?.let { id ->
                if (username != null && !isScrapped) {
                    postScrap(username, id)
                } else {
                    Toast.makeText(requireContext(), "이미 스크랩을 완료했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }

        // RecyclerView 초기화
        answerList = arrayListOf()
        adapter = AnswerAdapterClass(answerList){ answer, position->

        }
        binding.answerContainer.adapter = adapter
        binding.answerContainer.layoutManager = LinearLayoutManager(requireContext())

        // 질문 정보 및 답변 가져오기
        if (questionId != null) {
            fetchReply(questionId)
            fetchQuestionDetails(questionId)
        }

        val btnPickImage: ImageButton = view.findViewById(R.id.btnPickImage)

        btnPickImage.setOnClickListener {
            if (hasPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
                openGallery()
            } else {
                checkAndRequestPermissions()
            }
        }

        binding.sendBtn.setOnClickListener {
            if (questionId != null) {
                sendPostAnswer(questionId)
            }
            binding.imagePreview.visibility=View.GONE
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 파일 선택 허용
        imagePickerLauncher.launch(intent)
    }

    private fun checkAndRequestPermissions() {
        val permissions = when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }

        if (!hasPermissions(permissions)) {
            requestPermissionLauncher.launch(permissions)
        } else {
            openGallery()
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun getJwtToken(): String {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun getUsername(): String? {
        val sharedPref = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPref.getString("username", null)
    }

    private fun sendPostAnswer(questionId: Long) {
        val token = getJwtToken()
        val text = binding.etMessage.text.toString() // 서버 키가 text임

        // 내용을 확인하고 비어 있지 않도록 체크
        if (text.isBlank()) {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("text", text)
        }

        answerService.postAnswer("Bearer $token", questionId,jsonObject).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                if (response.isSuccessful) {
                    var answerId = response.body()!!

                    // 이미지 업로드는 포스트 ID를 받은 후 처리
                    selectedImageUris?.let { uris ->
                        uploadImages(answerId ?: return, uris)
                    }

                    binding.etMessage.apply {
                        setText("")  // 텍스트를 지웁니다.
                        hint = "답변 작성하기"  // 힌트를 설정합니다.
                    }
                } else {
                    Log.e("POST_REQUEST", "Error: ${response.code()}")
                    Toast.makeText(context, "Request failed with status code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                Log.e("POST_REQUEST", "Failed to send request", t)
                Toast.makeText(context, "Request failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchReply(questionId: Long) {
        val token = getJwtToken()

        answerService.getAnswers("Bearer $token", questionId).enqueue(object :
            Callback<List<Reply>> {
            override fun onResponse(call: Call<List<Reply>>, response: Response<List<Reply>>) {
                if (response.isSuccessful) {

                    response.body()?.let { replies ->
                        answerList.clear()
                        answerList.addAll(replies)
                        adapter.notifyDataSetChanged()


                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Reply>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(requireContext(), "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchQuestionDetails(questionId: Long) {
        val token = getJwtToken()

        answerService.getQuestionFromId("Bearer $token", questionId).enqueue(object : Callback<QuestionClass> {
            override fun onResponse(call: Call<QuestionClass>, response: Response<QuestionClass>) {
                if (response.isSuccessful) {
                    response.body()?.let { question ->
                        bindQuestionToViews(question)
                    }
                } else {
                    Toast.makeText(requireContext(), "불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<QuestionClass>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindQuestionToViews(question: QuestionClass) {
        // 질문 제목, 내용 설정
        binding.title.text = question.subject
        binding.questionText.text = question.text


        // 좋아요 수, 댓글 수, 스크랩 수 설정
        binding.likeNum.text = question.likes.toString()
        binding.commentNum.text = question.replyCount.toString()
        binding.scrapNum.text = question.scrapCount.toString()

        val username = question.username
        //질문자 프로필 설정
        fetchUpdateUserInfo(username)

        // RecyclerView 초기화 및 어댑터 설정
        if (!isRecyclerViewInitialized) {
            binding.recyclerViewImages.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = QuestionImageAdapter(question.images) // ImageData 리스트를 직접 전달
            }
            isRecyclerViewInitialized = true
            if(question.images.isNotEmpty()){
                binding.recyclerViewImages.visibility = View.VISIBLE
            }
        }

    }

    private fun fetchUpdateUserInfo(username: String) {
        val token = getJwtToken()

        // 프로필 정보 가져오기 요청
        answerService.getProfile("Bearer $token", username).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    // 성공적으로 사용자 프로필 정보를 받았을 때 처리
                    response.body()?.let { user ->
                        // 사용자 닉네임을 UI에 설정
                        binding.questionerName.text = user.nickname


                        // profileImage가 null인지 먼저 체크
                        val profileImage = user.profileImage
                        if (!profileImage.isNullOrEmpty()) {
                            val fullImageUrl = "http://15.165.213.186/$profileImage"
                            Glide.with(binding.root.context)
                                .load(fullImageUrl)
                                .into(binding.questionerProfile)
                        } else {
                            // 프로필 이미지가 없을 경우 기본 이미지 설정
                            binding.questionerProfile.setImageResource(R.drawable.group_172)
                        }
                    }
                } else {
                    // 요청이 실패했을 때 처리 (예: 에러 메시지 출력)
                    Log.e("UserProfile", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // 네트워크 오류나 다른 문제가 발생했을 때 처리
                Log.e("UserProfile", "Request failed", t)
            }
        })
    }



    private fun postLike(postId: Long) {
        val token = getJwtToken()

        answerService.likePost("Bearer $token", postId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    isLiked = true
                    saveLikeStatus(postId, true)
                    updateLikeButtonUI(isLiked)
                    Toast.makeText(requireContext(), "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "좋아요를 누르는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun postScrap(username: String, postId: Long) {
        val token = getJwtToken()

        answerService.scrapPost("Bearer $token", username, postId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    isScrapped = true
                    saveScrapStatus(postId, true)
                    updateScrapButtonUI(isScrapped)
                    Toast.makeText(requireContext(), "스크랩을 완료했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "스크랩을 완료하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateLikeButtonUI(isLiked: Boolean) {
        if (isLiked) {
            binding.likeButton.setImageResource(R.drawable.like_button2)  // 좋아요된 상태의 이미지
            binding.likeButton.isEnabled = false  // 좋아요 버튼 비활성화
        } else {
            binding.likeButton.setImageResource(R.drawable.like_button)  // 기본 좋아요 이미지
            binding.likeButton.isEnabled = true  // 좋아요 버튼 활성화
        }
    }

    private fun updateScrapButtonUI(isScrapped: Boolean) {
        if (isScrapped) {
            binding.scrapButton.setImageResource(R.drawable.scrap_button2)  // 스크랩된 상태의 이미지
            binding.scrapButton.isEnabled = false  // 스크랩 버튼 비활성화
        } else {
            binding.scrapButton.setImageResource(R.drawable.scrap_button)  // 기본 스크랩 이미지
            binding.scrapButton.isEnabled = true  // 스크랩 버튼 활성화
        }
    }

    private fun saveLikeStatus(postId: Long, isLiked: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("likes", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("post_$postId", isLiked)
            apply()
        }
    }

    private fun saveScrapStatus(postId: Long, isScrapped: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("scraps", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("post_$postId", isScrapped)
            apply()
        }
    }

    private fun getLikeStatus(postId: Long): Boolean {
        val sharedPref = requireContext().getSharedPreferences("likes", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("post_$postId", false)
    }

    private fun getScrapStatus(postId: Long): Boolean {
        val sharedPref = requireContext().getSharedPreferences("scraps", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("post_$postId", false)
    }

    private fun setupAutoRefresh() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val questionId = arguments?.getLong("QUESTION_ID")
                if (questionId != null) {
                    // 좋아요, 스크랩 상태 업데이트
                    fetchQuestionDetails(questionId)
                    // 댓글 목록 업데이트
                    fetchReply(questionId)
                }
                handler.postDelayed(this, refreshInterval)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(runnable)  // Fragment가 파괴될 때 Runnable 제거
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context?.contentResolver?.query(uri, projection, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
            it.close()
        }
        return path
    }

    private fun uploadImages(postId: Long, imageUris: List<Uri>) {
        val token = getJwtToken()
        val fileParts = imageUris.mapNotNull { uri ->
            val filePath = getRealPathFromURI(uri)
            filePath?.let {
                val file = File(it)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", file.name, requestFile)
            }
        }

        if (fileParts.isEmpty()) {
            Toast.makeText(context, "No valid images to upload", Toast.LENGTH_SHORT).show()
            return
        }

        answerService.uploadImages("Bearer $token", postId, fileParts).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "사진을 정상적으로 업로드하였습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("UPLOAD_IMAGES", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Image upload failed with status code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                Log.e("UPLOAD_IMAGES", "Failed to upload images", t)
                Toast.makeText(context, "Image upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 프래그먼트에서 키보드 숨기기
    private fun hideKeyboard() {
        val inputManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = requireActivity().currentFocus ?: view
        currentFocusedView?.let {
            inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

}
