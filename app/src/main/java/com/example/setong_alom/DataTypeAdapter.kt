
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Date Type Adapter
class DateTypeAdapter : com.google.gson.TypeAdapter<Date>() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    override fun write(out: com.google.gson.stream.JsonWriter?, value: Date?) {
        out?.value(dateFormat.format(value))
    }

    override fun read(`in`: com.google.gson.stream.JsonReader): Date? {
        return try {
            dateFormat.parse(`in`.nextString())
        } catch (e: Exception) {
            null
        }
    }
}
