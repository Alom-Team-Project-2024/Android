import com.example.alom_team_project.question_board.QuestionClass
import com.example.alom_team_project.question_board.QuestionPostFragment
import com.example.alom_team_project.question_board.Subject
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface QuestionPostService {
    // 포스트 생성
    @POST("question_post")
    fun postQuestion(
        @Header("Authorization") token: String, // Authorization 헤더에 토큰 추가
        @Body requestBody: JsonObject // JSON 형식의 바디 데이터
    ): Call<Long>

    // 이미지 업로드 및 URL 반환
    @Multipart
    @POST("question_post/{postId}/images")
    fun uploadImages(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Part file: List<MultipartBody.Part>
    ): Call<JsonArray> // 응답을 JSON으로 변경

    // 질문 목록 가져오기
    @GET("question_post")
    fun getQuestions(
        @Header("Authorization") token: String // Authorization 헤더에 토큰 추가
    ): Call<List<QuestionClass>>

    // 과목 목록 가져오기
    @GET("subjects")
    fun getSubjects(
        @Header("Authorization") token: String // Authorization 헤더에 토큰 추가
    ): Call<List<Subject>>
}
