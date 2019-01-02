package dev.hercat.com.blog.controller

import dev.hercat.com.blog.mapper.EvilsMapper
import dev.hercat.com.blog.model.Evils
import dev.hercat.com.blog.model.Message
import dev.hercat.com.blog.model.Pagination
import dev.hercat.com.blog.tool.generateDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class EvilsController(
        @Autowired private val evilsMapper: EvilsMapper) {

    @RequestMapping(value = ["/evils", "/evils/"], method = [RequestMethod.POST])
    fun addEvils(evils: Evils): Message {
        val msg = Message()
        val validate = evils.content.isNotBlank()
        if (validate) {
            evils.createDate = generateDate()
            if (evilsMapper.insert(evils) == 1) {
                msg.code = 200
                msg.map("evils", evils)
            } else {
                msg.code = 500
                msg.info = "存储失败"
            }
        } else {
            msg.code = 500
            msg.info = "缺少内容参数"
        }
        return msg
    }

    @RequestMapping(value = ["/evils", "/evils/"], method = [RequestMethod.GET])
    fun getEvils(pagination: Pagination): Message {
        val msg = Message()
        val evils = evilsMapper.getEvils(pagination)
        msg.code = 200
        msg.map("evils", evils)
        msg.map("pagination", pagination)
        return msg
    }

    @RequestMapping(value = ["/evils/count", "/evils/count/"], method = [RequestMethod.GET])
    fun count(): Message {
        val msg = Message()
        val count = evilsMapper.count()
        msg.code = 200
        msg.map("count", count)
        return msg
    }
}
