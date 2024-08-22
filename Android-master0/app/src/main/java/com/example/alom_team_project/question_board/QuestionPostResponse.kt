import com.google.gson.annotations.SerializedName

data class QuestionPostResponse(
    @SerializedName("subject") val subject: String,
    @SerializedName("text") val text: String,
    @SerializedName("writer") val writer: String,
    @SerializedName("likes") val likes: Int,
    @SerializedName("clips") val clips: Int,
    @SerializedName("replyCount") val replyCount: Int,
    @SerializedName("replies") val replies: List<Reply>,
    @SerializedName("images") val images: List<Image>
)

data class Reply(
    @SerializedName("title") val title: String,
    @SerializedName("text") val text: String
)

data class Image(
    @SerializedName("imageUrl") val imageUrl: String
)