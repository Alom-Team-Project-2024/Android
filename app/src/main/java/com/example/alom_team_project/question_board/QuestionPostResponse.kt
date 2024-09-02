import com.google.gson.annotations.SerializedName

data class QuestionPostResponse(
    @SerializedName("subject") val subject: String,
    @SerializedName("text") val text: String,
    @SerializedName("writer") val writer: String,
    @SerializedName("likes") val likes: Int,
    @SerializedName("clips") val clips: Int,
    @SerializedName("replyCount") val replyCount: Int,
    @SerializedName("replies") val replies: List<Reply>,
    @SerializedName("images") val images: List<ImageData>
)

data class ImageData(
    val imageUrl: String
)

data class User(
    val nickname: String,
    val profileImage: String
)

data class Reply(
    val id : Long,
    val title: String,
    val text: String,
    val writer: String,
    val username: String,
    val likes: Int,
    val images: List<ImageData>,
    val createdAt: String
)

data class Subject(
    val subject : String
)
