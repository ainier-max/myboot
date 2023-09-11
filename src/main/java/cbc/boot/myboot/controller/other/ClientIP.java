package cbc.boot.myboot.controller.other;

import cbc.boot.myboot.util.GetIPUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class ClientIP {
    @GetMapping("/cbc/getClientIP.cbc")
    public @ResponseBody
    List<Object> getIP(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Author:陈斌才----------");
        System.out.println("执行获取请求客户端IP操作!");
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        try {
            String realIP = GetIPUtil.getIpAddr(request);
            System.out.println("请求客户端IP地址：" + realIP);
            hashMap.put("state", "success");
            hashMap.put("ip", realIP);
            returnList.add(hashMap);
            return returnList;
        }catch (Exception e) {
            e.printStackTrace();
            String realIP = GetIPUtil.getIpAddr(request);
            System.out.println("请求客户端IP地址：" + realIP);
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
            returnList.add(hashMap);
            return returnList;
        }
    }

}
