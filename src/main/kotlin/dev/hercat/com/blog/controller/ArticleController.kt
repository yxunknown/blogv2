package dev.hercat.com.blog.controller

import dev.hercat.com.blog.mapper.ArticleMapper
import dev.hercat.com.blog.model.Article
import dev.hercat.com.blog.model.Message
import dev.hercat.com.blog.model.Pagination
import dev.hercat.com.blog.tool.generateArticleId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ArticleController(
        @Autowired private val articleMapper: ArticleMapper) {

    @RequestMapping(value = ["/p", "/p/"], method = [RequestMethod.POST])
    fun postArticle(article: Article): Message {
        val msg = Message()
        when {
            validateArticle(article) -> {
                article.id = generateArticleId()
                if (articleMapper.insertArticle(article) == 1) {
                    msg.code = 200
                    msg.map("article", article)
                } else {
                    msg.code = 500
                    msg.info = "存储文章出错"
                }
            }
            else -> {
                msg.code = 400
                msg.info = "文章信息不完整"
            }
        }
        return msg
    }

    @RequestMapping(value = ["/p/{articleId}", "/p/{articleId}/"], method = [RequestMethod.GET])
    fun getArticleById(@PathVariable("articleId") articleId: String): Message {
        val msg = Message()
        if (articleId.isBlank()) {
            msg.code = 400
            msg.info = "缺少文章编号参数"
        } else {
            val article = articleMapper.selectArticleById(articleId)
            if (article == null) {
                msg.code = 400
                msg.info = "文章不存在"
            } else {
                msg.code = 200
                msg.map("article", article)
            }
        }
        return msg
    }

    @RequestMapping(value = ["/p", "/p/"], method = [RequestMethod.GET])
    fun getArticles(pagination: Pagination): Message {
        val articles = articleMapper.selectArticles(pagination)
        val count = articleMapper.count()
        val msg = Message()
        msg.code = 200
        msg.map("articles", articles)
        msg.map("count", count)
        msg.map("pagination", pagination)
        return msg
    }

    @RequestMapping(value = ["/p/s", "/p/s/"], method = [RequestMethod.GET])
    fun getArticlesBySelection(article: Article, pagination: Pagination): Message {
        val msg = Message()
        if (validateArticle(article)) {
            val articles = articleMapper.selectArticlesBySelection(article, pagination)
            val count = articleMapper.countBySelection(article)
            msg.code = 200
            msg.map("articles", articles)
            msg.map("count", count)
            msg.map("pagination", pagination)
            msg.map("selection", article)
        } else {
            msg.code = 400
            msg.info = "缺少查询参数"
        }
        return msg
    }

    @RequestMapping(value = ["/p", "/p/"], method = [RequestMethod.PUT])
    fun updateArticle(article: Article): Message {
        val msg = Message()
        if (validateArticle(article) && article.id.isNotBlank()) {
            val newArticle = articleMapper.selectArticleById(article.id)
            when {
                newArticle == null -> {
                    msg.code = 400
                    msg.info = "文章不存在"
                }
                articleMapper.updateArticle(article) != 1 -> {
                    msg.code = 500
                    msg.info = "更新文章出错"
                }
                else -> {
                    msg.code = 200
                    msg.map("article", articleMapper.selectArticleById(article.id)!!)
                }
            }
        } else {
            msg.code = 400
            msg.info = "文章参数不完整"
        }
        return msg
    }

    private fun validateArticle(article: Article): Boolean {
        return when {
            article.title.isBlank() -> false
            article.createDate.isBlank() -> false
            article.type.id == -1 -> false
            article.content.isBlank() -> false
            else -> true
        }
    }
}