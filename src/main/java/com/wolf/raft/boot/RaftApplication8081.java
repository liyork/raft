package com.wolf.raft.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wolf.raft")
public class RaftApplication8081 {

	public static void main(String[] args) {

		SpringApplication.run(RaftApplication8081.class, args);

	}


	//todo 想替换掉原先的dispatcherServlet然后自己构造，并进行回调，不知可行否，但是属性都是privte需要反射
//	@Bean
//	public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
//
//		DispatcherServlet dispatcherServlet2 = new DispatcherServlet();
//		dispatcherServlet2.setDispatchOptionsRequest(dispatcherServlet.getop);
//		dispatcherServlet2.setDispatchTraceRequest(dispatcherServlet.getdis);
//		dispatcherServlet2.setThrowExceptionIfNoHandlerFound(
//				this.webMvcProperties.isThrowExceptionIfNoHandlerFound());
//
//
//		ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(
//				dispatcherServlet, "/foo");
//		registration.setName("customDispatcher");
//		return registration;
//	}
//

	//这是springboot原生的
//	@Bean(name = "dispatcherServlet")
//	public DispatcherServlet dispatcherServlet() {
//		DispatcherServlet dispatcherServlet = new DispatcherServlet();
//		dispatcherServlet.setDispatchOptionsRequest(
//				this.webMvcProperties.isDispatchOptionsRequest());
//		dispatcherServlet.setDispatchTraceRequest(
//				this.webMvcProperties.isDispatchTraceRequest());
//		dispatcherServlet.setThrowExceptionIfNoHandlerFound(
//				this.webMvcProperties.isThrowExceptionIfNoHandlerFound());
//		return dispatcherServlet;
//	}

}

