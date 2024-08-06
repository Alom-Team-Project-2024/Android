import com.example.setong_alom.QuestionClass
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface QuestionPostService {
    @POST("question_post") // 엔드포인트
    fun postQuestion(
        @Header("Authorization") token: String, // Authorization 헤더에 토큰 추가
        @Body requestBody: JsonObject // JSON 형식의 바디 데이터
    ): Call<Void>


    @GET("question_post") // 데이터를 가져오는 엔드포인트
    fun getQuestions(
        @Header("Authorization") token: String // Authorization 헤더에 토큰 추가
    ): Call<List<QuestionClass>>
}
