package dev.hercat.com.blog.tool

import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

val chars = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")

fun sha(source: String): String {
    val inputData = source.toByteArray(charset = Charset.forName("UTF-8"))
    val msgDigest = MessageDigest.getInstance("SHA")
    val sha = BigInteger(msgDigest.digest(inputData)).abs()
    return sha.toString(32)
}


@Synchronized
fun generateArticleId(): String {
    val instant = Instant.now()
    val timestamp = Timestamp.from(instant).toString()
    val random = Random(instant.toEpochMilli())
    val regex = Regex("""[- .:]""")
    val sb = StringBuilder(timestamp.replace(regex, "").substring(2))
    for (index in 6 until 12) {
        var char = chars[Integer.valueOf(sb[index].toString())]
        if (random.nextInt(2) == 1) {
            char = char.toUpperCase()
        }
        sb.replace(index, index + 1, char)
    }
    if (sb.length < 15) {
        sb.append("0")
    }
    return sb.toString()
}

@Synchronized
fun generateDate(): String {
    val date = Date()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    return dateFormatter.format(date)
}
