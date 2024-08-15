package com.example.setong_alom

import QuestionPostService
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var submitButton: ImageButton
    private lateinit var imageView: ImageView

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

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                imageView.setImageURI(selectedImageUri)

                postId?.let { id ->
                    uploadImage(id)
                } ?: Toast.makeText(context, "Post ID is not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_question_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.titleEditText)
        contentEditText = view.findViewById(R.id.contentEditText)
        submitButton = view.findViewById(R.id.submitButton)
        imageView = view.findViewById(R.id.imageView)

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

    private fun sendPostRequest() {
        val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIyMDExMzc1Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXI3MDIiLCJpYXQiOjE3MjM3MTU4MDUsImV4cCI6MTcyMzcxNzYwNX0.42NmgXInVMSz_oBNk9AdmRghafPvgk61o74BouI04vM"
        val subject = titleEditText.text.toString()
        val text = contentEditText.text.toString() // 서버 키가 text임

        // 제목과 내용을 확인하고 비어 있지 않도록 체크
        if (subject.isBlank() || text.isBlank()) {
            Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            return
        }

        val jsonObject = JsonObject().apply {
            addProperty("subject", subject)
            addProperty("text", text)
        }

        service.postQuestion(token, jsonObject).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                if (response.isSuccessful) {
                    postId = response.body()
                    Toast.makeText(context, "Post submitted successfully. ID: $postId", Toast.LENGTH_SHORT).show()
                    // 이미지 업로드는 포스트 ID를 받은 후 처리
                    selectedImageUri?.let { uri ->
                        uploadImage(postId ?: return)
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImage(postId: Long) {
        selectedImageUri?.let { uri ->
            val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IjIyMDExMzc1Iiwicm9sZSI6IlVTRVIiLCJuaWNrbmFtZSI6InVzZXI3MDIiLCJpYXQiOjE3MjM3MTU4MDUsImV4cCI6MTcyMzcxNzYwNX0.42NmgXInVMSz_oBNk9AdmRghafPvgk61o74BouI04vM"

            val filePath = getRealPathFromURI(uri) ?: return
            val file = File(filePath)

            if (file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Use the correct service method for image upload
                service.uploadImage(token, postId, body).enqueue(object : Callback<JsonArray> {
                    override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()

                            // 서버로부터의 응답 데이터 확인
                            val imageUrls = response.body()?.map { it.asJsonObject.get("imageUrl").asString }
                            Log.d("UPLOAD_IMAGE", "Image URLs: $imageUrls")
                        } else {
                            Log.e("UPLOAD_IMAGE", "Error: ${response.code()} - ${response.message()}")
                            Toast.makeText(context, "Image upload failed with status code: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                        Log.e("UPLOAD_IMAGE", "Failed to upload image", t)
                        Toast.makeText(context, "Image upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            }
        }
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
