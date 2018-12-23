package dev.hercat.com.blog.controller

import dev.hercat.com.blog.mapper.ArticleTypeMapper
import dev.hercat.com.blog.model.ArticleType
import dev.hercat.com.blog.model.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class ArticleTypeController(
        @Autowired private val typeMapper: ArticleTypeMapper) {

    @RequestMapping(value = ["/p/type", "/p/type/"], method = [RequestMethod.POST])
    fun addType(type: ArticleType): Message {
        val msg = Message()
        if (type.type.isNotBlank()) {
            if (typeMapper.insertType(type) == 1) {
                msg.code = 200
                msg.map("type", type)
            } else {
                msg.code = 500
                msg.info = "新增分类失败"
            }
        } else {
            msg.code = 400
            msg.info = "分类为空"
        }
        return msg
    }

    @RequestMapping(value = ["/p/type", "/p/type/"], method = [RequestMethod.GET])
    fun getTypes(): Message {
        val msg = Message()
        val types = typeMapper.getTypes()
        msg.code = 200
        msg.map("types", types)
        return msg
    }
}