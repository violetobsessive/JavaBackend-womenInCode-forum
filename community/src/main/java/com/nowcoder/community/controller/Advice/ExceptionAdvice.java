package com.nowcoder.community.controller.Advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


// annotation的意思是只扫描带有@Controller注解的bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);

    // 这个方法统一处理所有异常; 大括号表示多个数据（处理多个异常）
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("server error" + e.getMessage());
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }
        // 判断请求是普通请求还是异步请求（返回html还是json）
        String xRequestedWith = request.getHeader("x=requested-with");

        // 如果是异步请求
        if("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/json; charSet=utf-8");
            // 获取输出流 - 输出string
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "server error"));
        }
        // 如果是普通请求
        else{
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

}
