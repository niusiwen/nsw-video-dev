package com.nsw;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.nsw.controller.interceptor.MiniInterceptor;
/**
 * 
 * @Description: 
 * @author nsw  
 * @date 2020年12月21日  
 * @version: V1.0
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/META-INF/resources/")
				.addResourceLocations("file:G:/nsw_video_dev/");
		
	}
	
	@Bean(initMethod="init")
	public ZKCuratorClient zkCuratorClient() {
		return new ZKCuratorClient();
	}

	//注册拦截器
	@Bean
	public MiniInterceptor miniInterceptor() {
		return new MiniInterceptor();
	}

	//添加拦截器
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(miniInterceptor()).addPathPatterns("/user/**")//addPathPatterns 添加要拦截的路径
						.addPathPatterns("/video/upload", "/video/uploadCover",
								         "/video/userLike", "/video/userUnLike",
								         "/video/saveComment")
												  .addPathPatterns("/bgm/**")
												  .excludePathPatterns("/user/queryPublisher");//排除掉不需要拦截的路径
		
		super.addInterceptors(registry);
		
	}
	
}
