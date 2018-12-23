package dev.hercat.com.blog.controller

import dev.hercat.com.blog.mapper.UserMapper
import dev.hercat.com.blog.model.Message
import dev.hercat.com.blog.model.User
import dev.hercat.com.blog.tool.sha
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.regex.Pattern

@RestController
class UserController(
        @Autowired val userMapper: UserMapper) {

    @RequestMapping(value = ["/blog/register", "/blog/register/"], method = [RequestMethod.POST])
    fun register(user: User): Message {
        val msg = Message()
        if (validateUser(user, msg)) {
            // encrypt passcode
            user.password = sha(user.password)
            // add into db
            if (userMapper.insertUser(user) == 1) {
                msg.code = 200
                msg.map("user", user.apply {
                    password = "*********"
                })
            }
        }
        return msg
    }

    @RequestMapping(value = ["/blog/login", "/blog/login/"], method = [RequestMethod.POST])
    fun login(user: User): Message {
        val msg = Message()
        val validate = user.email.isNotBlank() && user.password.isNotBlank()
        if (validate) {
            val storedUser = userMapper.selectUserByEmail(user.email)
            when {
                storedUser == null -> {
                    msg.code = 400
                    msg.info = "用户不存在"
                }
                sha(user.password) != storedUser.password -> {
                    msg.code = 400
                    msg.info = "密码错误"
                }
                else -> {
                    msg.code = 200
                    msg.map("user", storedUser.apply {
                        password = "********"
                    })
                }
            }
        } else {
            msg.code = 400
            msg.info = "登录信息不完整"
        }
        return msg
    }

    private fun validateUser(user: User, msg: Message): Boolean {
        val regex = Regex("""[a-zA-Z_0-9.-]+@([a-z0-9-]+.)+[a-z]{1,6}""")
        return when {
            user.email.isBlank() -> {
                msg.code = 400
                msg.info = "用户邮箱为空"
                false
            }
            user.nickname.isBlank() -> {
                msg.code = 400
                msg.info = "用户昵称为空"
                false
            }
            user.password.isBlank() -> {
                msg.code = 400
                msg.info = "密码为空"
                false
            }
            !Pattern.matches(regex.pattern, user.email) -> {
                msg.code = 400
                msg.info = "邮箱地址不合法"
                false
            }
            userMapper.selectUserByEmail(user.email) != null -> {
                msg.code = 400
                msg.info = "该邮箱已被注册"
                false
            }
            else -> true
        }
    }

}
