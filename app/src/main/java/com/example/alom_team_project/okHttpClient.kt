import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestOptions
import com.example.alom_team_project.R
import okhttp3.OkHttpClient
import java.io.InputStream

// OkHttpClient 생성
private fun createOkHttpClient(context: Context): OkHttpClient {
    val token = getJwtToken(context) // JWT 토큰 가져오기

    return OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .build()
}

// JWT 토큰을 Context를 사용하여 가져오기
private fun getJwtToken(context: Context): String {
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    return sharedPref.getString("jwt_token", "") ?: ""
}

// Glide 설정
fun setupGlide(context: Context) {
    val okHttpClient = createOkHttpClient(context)

    Glide.get(context).registry.replace(
        GlideUrl::class.java,
        InputStream::class.java,
        OkHttpUrlLoader.Factory(okHttpClient)
    )
}

