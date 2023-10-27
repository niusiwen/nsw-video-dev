package com.nsw.controller.interceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.nsw.utils.JsonUtils;
import com.nsw.utils.NswJSONResult;
import com.nsw.utils.RedisOperator;
/**
 * 拦截器
 * @author nsw
 *
 */
public class MiniInterceptor implements HandlerInterceptor {

	@Autowired
	public RedisOperator redis;
	public static final String USER_REDIS_SESSION = "USER-REDIS-SESSION";
	
	/**
	 * 拦截请求，在controller调用之前
	 * 返回false请求被拦截，返回true请求被放行
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		String userId = request.getHeader("headerUserId");
		String userToken = request.getHeader("headerUserToken");
		if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {		
			String uniqueToken = redis.get(USER_REDIS_SESSION + ":" + userId);
			if(StringUtils.isEmpty(uniqueToken) && StringUtils.isBlank(uniqueToken)) {
				System.out.println("请登陆...");
				returnErrorResponse(response, new NswJSONResult().errorTokenMsg("请登陆..."));
				return false;
			} else {
				if(!uniqueToken.equals(userToken)) {
					System.out.println("账号被挤出...");
					returnErrorResponse(response, new NswJSONResult().errorTokenMsg("账号被挤出..."));
					return false;
				}
			}
			
		} else {
			System.out.println("请登陆...");
			returnErrorResponse(response, new NswJSONResult().errorTokenMsg("请登陆..."));
			return false;
		}
		
		return true;
	}
	
	/**
	 * 将拦截的错误提示信息返回到客户端
	 * @param response
	 * @param result
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public void returnErrorResponse(HttpServletResponse response, NswJSONResult result) 
			throws IOException, UnsupportedEncodingException {
		OutputStream out=null;
		try{
		    response.setCharacterEncoding("utf-8");
		    response.setContentType("text/json");
		    out = response.getOutputStream();
		    out.write(JsonUtils.objectToJson(result).getBytes("utf-8"));
		    out.flush();
		} finally{
		    if(out!=null){
		        out.close();
		    }
		}
	}

	/**
	 * 请求controller之后，渲染视图之前
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 请求controller之后， 视图渲染之后
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
