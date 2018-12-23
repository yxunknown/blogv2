package dev.hercat.com.blog.controller

import dev.hercat.com.blog.mapper.ArticleMapper
import dev.hercat.com.blog.mapper.ArticleViewsMapper
import dev.hercat.com.blog.model.Article
import dev.hercat.com.blog.model.ArticleViews
import dev.hercat.com.blog.model.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ArticleViewsController(
        @Autowired private val articleMapper: ArticleMapper,
        @Autowired private val articleViewsMapper: ArticleViewsMapper) {

    @Synchronized
    @RequestMapping(value = ["/p/{articleId}/views", "/p/{articleId}/views/"], method = [RequestMethod.POST])
    fun addArticleViewsCount(@PathVariable("articleId") articleId: String): Message {
        val msg = Message()
        val article = articleMapper.selectArticleById(articleId)
        when {
            articleId.isBlank() -> {
                msg.code = 400
                msg.info = "文章编号参数为空"
            }
            article == null -> {
                msg.code = 400
                msg.info = "文章不存在"
            }
            else -> {
                var articleViews = articleViewsMapper.selectArticleViewsByArticleId(articleId)
                if (articleViews == null) {
                    articleViews = ArticleViews(0, article, 1)
                    if (articleViewsMapper.insert(articleViews) == 1) {
                        msg.code = 200
                        msg.map("views", articleViews)
                    } else {
                        msg.code = 500
                        msg.info = "更新文章阅读数量失败"
                    }
                } else {
                    articleViews.count++
                    if (articleViewsMapper.update(articleViews) == 1) {
                        msg.code = 200
                        msg.map("views", articleViews)
                    } else {
                        msg.code = 500
                        msg.info = "更新文章阅读数量失败"
                    }
                }
            }
        }
        return msg
    }

    @RequestMapping(value = ["/p/{articleId}/views", "/p/{articleId}/views/"], method = [RequestMethod.GET])
    fun getArticleViews(@PathVariable("articleId") articleId: String): Message {
        val msg = Message()
        if (articleId.isNotBlank()) {
            val articleViews = articleViewsMapper.selectArticleViewsByArticleId(articleId)
            msg.code = 200
            msg.map("views", articleViews ?: "")
        } else {
            msg.code = 400
            msg.info = "文章文章编号参数"
        }
        return msg
    }
}