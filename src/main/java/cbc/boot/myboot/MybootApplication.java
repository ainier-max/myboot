package cbc.boot.myboot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;


@SpringBootApplication
//@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ImportResource(value = { "classpath:spring/applicationContext-dao.xml" })
public class MybootApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		System.out.println("cbc-程序开始启动");
		SpringApplication.run(MybootApplication.class, args);
		System.out.println("cbc-程序启动成功");
	}
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(MybootApplication.class);
	}
}
