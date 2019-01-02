package dev.hercat.com.blog.mapper

import dev.hercat.com.blog.model.*
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Component

@Component
@Mapper
interface UserMapper {
    fun insertUser(@Param("user") user: User): Int

    fun selectUserByEmail(@Param("email") email: String): User?
}

@Component
@Mapper
interface ArticleMapper {

    fun insertArticle(@Param("article") article: Article): Int

    fun selectArticleById(@Param("id") id: String): Article?

    fun selectArticles(@Param("pagination") pagination: Pagination = Pagination()): List<Article>

    fun selectArticlesBySelection(@Param("article") article: Article,
                                  @Param("pagination") pagination: Pagination): List<Article>

    fun count(): Long

    fun countBySelection(@Param("article") article: Article): Long

    fun updateArticle(@Param("article") article: Article): Int
}

@Component
@Mapper
interface CommentMapper {
    fun insertComment(@Param("comment") comment: ArticleComment): Int

    fun selectCommentsByArticle(@Param("articleId") articleId: String,
                                @Param("pagination") pagination: Pagination = Pagination()): List<ArticleComment>

    fun countByArticle(@Param("articleId") articleId: String): Long

    fun deleteById(@Param("id") id: Int): Int
}

@Component
@Mapper
interface ArticleTypeMapper {
    fun insertType(@Param("type") type: ArticleType): Int

    fun getTypes(): List<ArticleType>

    fun count(): Long
}

@Component
@Mapper
interface ArticleViewsMapper {
    fun insert(@Param("view") view: ArticleViews): Int

    fun update(@Param("view") view: ArticleViews): Int

    fun selectArticleViewsByArticleId(@Param("articleId") articleId: String): ArticleViews?

    fun totalView(): Long
}

@Component
@Mapper
interface EvilsMapper {
    fun insert(@Param("evils") evils: Evils): Int

    fun getEvils(@Param("pagination") pagination: Pagination = Pagination()): List<Evils>

    fun count(): Long
}
