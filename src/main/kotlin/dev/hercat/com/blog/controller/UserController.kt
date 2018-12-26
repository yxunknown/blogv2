package dev.hercat.com.blog.controller

import com.alibaba.fastjson.JSONObject
import dev.hercat.com.blog.mapper.UserMapper
import dev.hercat.com.blog.model.Message
import dev.hercat.com.blog.model.User
import dev.hercat.com.blog.tool.sha
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.regex.Pattern

const val GITHUB_CLIENT_ID = "fac48429e3d3e35aaec2"
const val GITHUB_CLIENT_SECRET = "ca34bd71fb1698ddc8d0e4369c729257fdbb433b"
const val GITHUB_ACCESS_TOKEN = "https://github.com/login/oauth/access_token"
const val GITHUB_USER_INFO = "https://api.github.com/user"

@RestController
class UserController(
        @Autowired private val userMapper: UserMapper,
        @Autowired private val restTemplate: RestTemplate) {

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

    @RequestMapping(value = ["/login/code", "/login/code/"], method = [RequestMethod.POST])
    fun auth(code: String): Message {
        val msg = Message()
        // validate code
        if (code.isBlank()) {
            msg.code = 400
            msg.info = "code参数为空"
        } else {
            // get access_token
            val tokenInfo = getGithubAccessToken(code)
            if (tokenInfo == null) {
                msg.code = 500
                msg.info = "请求Github token出错"
            } else {
                // tokenInfo is a string like access_token=token&scope=scope&token_type=type
                // parse token from token info
                val token = tokenInfo.substringBefore("&").substringAfter("=")
                // get user info from github
                val userInfo = getUserInfoFromGithub(token)
                if (userInfo == null) {
                    msg.code = 500
                    msg.info = "获取Github用户信息出错"
                } else {
                    // parse userInfo into user
                    // {
                    //     "login": "yxunknown",
                    //     "id": 22553946,
                    //     "node_id": "MDQ6VXNlcjIyNTUzOTQ2",
                    //     "avatar_url": "https://avatars3.githubusercontent.com/u/22553946?v=4",
                    //     "name": "Mevur",
                    //     "company": "Leway",
                    //     "blog": "cplzwr@live.cn",
                    //     "location": "Zunyi City, China",
                    //     "email": "cplzwr@live.cn",
                    //     "bio": "紫衣青衫打马归家，路遇伊人面似桃花。",
                    // }
                    val user = User(
                            email = userInfo.getString("email"),
                            nickname = userInfo.getString("login"),
                            password = userInfo.getString("githubuser"),
                            profile = userInfo.getString("avatar_url"))
                    // check if this user has been stored into database
                    val dbUser = userMapper.selectUserByEmail(user.email)
                    if (dbUser == null) {
                        // this user is first login into this system
                        // storage it into database
                        val registerMsg = register(user)
                        if (registerMsg.code == 200) {
                            msg.code = 200
                            msg.map("user", user.apply { password = "********" })
                        } else {
                            msg.code = 500
                            msg.info = "存储用户信息出错"
                        }
                    } else {
                        // write login info
                        msg.code = 200
                        msg.map("user", dbUser.apply { password = "********" })
                    }
                }
            }
        }
        return msg
    }

    private fun getGithubAccessToken(code: String): String? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val body = LinkedMultiValueMap<String, String>()
        body.add("code", code)
        body.add("client_id", GITHUB_CLIENT_ID)
        body.add("client_secret", GITHUB_CLIENT_SECRET)
        val httpEntity = HttpEntity<MultiValueMap<String, String>>(body, headers)
        return restTemplate.postForObject(GITHUB_ACCESS_TOKEN, httpEntity, String::class.java)
    }

    private fun getUserInfoFromGithub(token: String): JSONObject? {
        val url = "$GITHUB_USER_INFO?access_token=$token"
        return restTemplate.getForEntity(url, JSONObject::class.java).body
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
