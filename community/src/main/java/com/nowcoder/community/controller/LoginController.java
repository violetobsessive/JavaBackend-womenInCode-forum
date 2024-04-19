package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    //浏览器返回html - html包含图片路径，浏览器再次访问server获得图片
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }


    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String getRegisterPage(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            //register success
            model.addAttribute("message", "Register successfully! " +
                    "Please log onto your email to activate the account.");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMessage",map.get("UsernameMessage"));
            model.addAttribute("passwordMessage",map.get("PasswordMessage"));
            model.addAttribute("emailMessage",map.get("EmailMessage"));

            return "/site/register";
        }
    }
    //敏感数据存在server端，在多个请求中要用；这次请求生成验证码存在server，登录时再用 - 跨请求，用cookie/session
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        //用得到的string生成图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 图片输出给浏览器
        response.setContentType("img/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png", os);
        } catch (IOException e) {
            logger.error("profile fail to load: " + e.getMessage());
        }
    }

//    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
//    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
//        int result = userService.activation(userId, code);
//        if (result == ACTIVATION_SUCCESS) {
//            model.addAttribute("msg", "Success");
//            model.addAttribute("target", "/login");
//        } else if (result == ACTIVATION_REPEAT) {
//            model.addAttribute("msg", "Already activated!");
//            model.addAttribute("target", "/index");
//        } else {
//            model.addAttribute("msg", "Failed, activation code incorrect!");
//            model.addAttribute("target", "/index");
//        }
//        return "/site/operate-result";
//    }

    //处理login页面用户输入的数据，post到数据库
    //session用来提取之前存入的验证码图片；ticket需要发给浏览器保存，用cookies保存。response用来创建cookies
    @RequestMapping(path = "/login", method = RequestMethod.POST)

    //user实体object会store在model里，放在constructor里的会store在 HttpServletRequest-request 里
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /*HttpSession session, */HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){

        //检查验证码 - 仅在表现层检查即可，不需要用service层
        // 检查验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
