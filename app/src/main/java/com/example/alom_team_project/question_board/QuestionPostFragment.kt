package com.example.alom_team_project.question_board

import QuestionPostService
import android.Manifest
import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alom_team_project.R
import com.example.alom_team_project.RetrofitClient
import com.example.alom_team_project.databinding.FragmentAnswerBinding
import com.example.alom_team_project.databinding.FragmentQuestionPostBinding
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val PERMISSION_REQUEST_CODE = 100

class QuestionPostFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentQuestionPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageAdapter: ImageAdapter
    private var selectedImageUris: List<Uri>? = null

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var submitButton: ImageButton
    private lateinit var imageView: ImageView
    private lateinit var adapter: SubjectAdapter
    private lateinit var subjectList: ArrayList<Subject>
    private lateinit var questionService: QuestionPostService

    private val service = RetrofitClient.instance.create(QuestionPostService::class.java)
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var postId: Long? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.titleEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        submitButton = view.findViewById(R.id.submitButton)

        // List 초기화
        subjectList = arrayListOf()

        // questionService 초기화
        questionService = RetrofitClient.instance.create(QuestionPostService::class.java)

        // RecyclerView 어댑터 및 레이아웃 매니저 설정
        setupRecyclerView()

        // 데이터 가져오기
        fetchData()

        // 검색 텍스트 변화 감지
        binding.titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변화 전의 행동을 정의
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.subjectRecyclerView.visibility = View.VISIBLE
                // 텍스트가 변경될 때마다 어댑터의 필터링 메소드 호출
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변화 후의 행동을 정의
            }
        })

        // RecyclerView 초기화
        binding.recyclerViewImages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(emptyList())  // 초기에는 빈 리스트
        binding.recyclerViewImages.adapter = imageAdapter

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
                imageAdapter = ImageAdapter(imageUris)
                binding.recyclerViewImages.adapter = imageAdapter
            }
        }


        val btnBack : ImageButton = view.findViewById(R.id.back_button)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val btnPickImage: ImageButton = view.findViewById(R.id.btnPickImage)

        btnPickImage.setOnClickListener {
            if (hasPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
                openGallery()
            } else {
                checkAndRequestPermissions()
            }
        }

        submitButton.setOnClickListener {
            sendPostRequest()
        }
    }

    private fun getJwtToken(): String {
        val sharedPref = requireContext().getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPref.getString("jwt_token", "") ?: ""
    }

    private fun sendPostRequest() {
        val token = getJwtToken()
        val subject = titleEditText.text.toString()
        val text = contentEditText.text.toString() // 서버 키가 text임

        // 제목과 내용을 확인하고 비어 있지 않도록 체크
        if (subject.isBlank() || text.isBlank()) {
            Toast.makeText(context, "과목명과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("subject", subject)
            addProperty("text", text)
        }

        service.postQuestion("Bearer $token", jsonObject).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                if (response.isSuccessful) {
                    postId = response.body()

                    // 이미지 업로드는 포스트 ID를 받은 후 처리
                    selectedImageUris?.let { uris ->
                        uploadImages(postId ?: return, uris)
                    }

                    Toast.makeText(context, "글이 정상적으로 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 파일 선택 허용
        imagePickerLauncher.launch(intent)
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

        service.uploadImages("Bearer $token", postId, fileParts).enqueue(object : Callback<JsonArray> {
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

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!hasPermissions(permissions)) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // 권한이 허용된 경우
            } else {
                Toast.makeText(context, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter(subjectList)
        binding.subjectRecyclerView.adapter = adapter
        binding.subjectRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun fetchData() {
        val token = getJwtToken()
        Log.d("FETCH_DATA", "Fetching data with token: $token")

        questionService.getSubjects("Bearer $token").enqueue(object : Callback<List<Subject>> {
            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                if (response.isSuccessful) {
                    Log.d("FETCH_DATA", "Data fetched successfully")

                    response.body()?.let { subjects ->
//                        subjectList.clear()
//                        subjectList.addAll(subjects)
                        adapter.filter(binding.titleEditText.text.toString())  // 현재 검색어로 필터링

                        response.body()?.let { subjects ->
                            adapter.updateSubjectList(subjects)
                        }
                    }
                } else {
                    Log.e("FETCH_DATA", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Failed to fetch data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                Log.e("FETCH_DATA", "Failed to fetch data", t)
                Toast.makeText(context, "Failed to fetch data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuestionPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
