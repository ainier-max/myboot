package cbc.boot.myboot.util;

import javax.servlet.http.HttpServletRequest;

public class GetIPUtil
{
  public static String getIpAddr(HttpServletRequest request)
  {
    String ip = request.getHeader("X-Real-IP");
    if (ip!=null && !ip.isEmpty() && (!"unknown".equalsIgnoreCase(ip))) {
      return ip;
    }
    ip = request.getHeader("X-Forwarded-For");
    if (ip!=null && !ip.isEmpty() && (!"unknown".equalsIgnoreCase(ip)))
    {
      int index = ip.indexOf(',');
      if (index != -1) {
        return ip.substring(0, index);
      }
      return ip;
    }

    return request.getRemoteAddr();
  }
}