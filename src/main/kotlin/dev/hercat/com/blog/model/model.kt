package dev.hercat.com.blog.model

data class Pagination(
        var start: Long = 0L,
        var limit: Long = 20L)

class Message {
    var code: Int = 0
    var info: String = ""
    val data: MutableMap<String, Any> = mutableMapOf()

    fun map(key: String, value: Any) {
        this.data[key] = value
    }
}

data class User(
        var email: String = "",
        var passwod: String = "",
        var nickname: String = "",
        var profile: String = "")

data class Article(
        var id: String = "",
        var title: String = "",
        var createDate: String = "",
        var type: ArticleType = ArticleType(),
        var content: String = "")

data class ArticleType(
        var id: Int = -1,
        var type: String = "")

data class ArticleViews(
        var id: Int = 0,
        var article: Article = Article(),
        var count: Long = 0L)

data class ArticleComment(
        var id: Int = 0,
        var from: User = User(),
        var to: User = User(),
        var article: Article = Article(),
        var createDate: String = "",
        var content: String = "")

