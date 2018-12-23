package dev.hercat.com.blog.controller

import dev.hercat.com.blog.mapper.ArticleMapper
import dev.hercat.com.blog.mapper.CommentMapper
import dev.hercat.com.blog.mapper.UserMapper
import dev.hercat.com.blog.model.ArticleComment
import dev.hercat.com.blog.model.Message
import dev.hercat.com.blog.model.Pagination
import dev.hercat.com.blog.tool.generateDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController


@RestController
class ArticleCommentController(
        @Autowired private val articleMapper: ArticleMapper,
        @Autowired private val commentMapper: CommentMapper,
        @Autowired private val userMapper: UserMapper) {

    @RequestMapping(value = ["/p/comment", "/p/comment/"], method = [RequestMethod.POST])
    fun addComment(comment: ArticleComment): Message {
        val msg = Message()
        if (validateComment(comment)) {
            comment.createDate = generateDate()
            if (commentMapper.insertComment(comment) == 1) {
                msg.code = 200
                msg.map("articleComment", comment)
            } else {
                msg.code = 500
                msg.info = "新增评论失败"
            }
        } else {
            msg.code = 400
            msg.info = "评论信息不完整"
        }
        return msg
    }

    @RequestMapping(value = ["/p/{articleId}/comment", "/p/{articleId}/comment"], method = [RequestMethod.GET])
    fun getArticleComments(@PathVariable("articleId") articleId: String,
                           pagination: Pagination): Message {
        val msg = Message()
        val comments = commentMapper.selectCommentsByArticle(articleId, pagination)
        val count = commentMapper.countByArticle(articleId)
        msg.code = 200
        msg.map("comments", comments)
        msg.map("count", count)
        msg.map("pagination", pagination)
        return msg
    }

    @RequestMapping(value = ["/p/comment/{id}", "/p/comment/{id}/"], method = [RequestMethod.DELETE])
    fun deleteComment(@PathVariable("id") id: Int): Message {
        val msg = Message()
        if (commentMapper.deleteById(id) == 1) {
            msg.code = 200
        } else {
            msg.code = 500
            msg.info = "删除评论失败"
        }
        return msg
    }

    private fun validateComment(comment: ArticleComment): Boolean {
        return when {
            comment.article.id.isBlank() -> false
            articleMapper.selectArticleById(comment.article.id) == null -> false
            comment.from.email.isBlank() -> false
            userMapper.selectUserByEmail(comment.from.email) == null -> false
            comment.to.email.isBlank() -> false
            userMapper.selectUserByEmail(comment.to.email) == null -> false
            comment.content.isBlank() -> false
            else -> true
        }
    }
}