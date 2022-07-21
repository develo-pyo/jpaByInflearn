package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// h2 설치경로 ~/bin h2.sh 실행 후 서버 띄워야 JPA 관련 에러 발생하지 않음
@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	//지연로딩 (LAZY) 사용시 엔티티를 조회하는 시점에 프록시 객체를 jackson 라이브러리가 json으로 생성할지 모름.
	//Hibernate5Module 을 Bean 으로 등록하여 해결 가능. => Entity 를 직접 return 하는 것은 지양하기때문에 방법만 알아두자.
	// Gradle : com.fasterxml.jackson.datatype:jackson-datatype-hibernate5
	@Bean
	Hibernate5Module hibernate5Module(){
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		// LAZY 멤버변수를 강제로 조회하는 설정. 해당설정을 주지않으면 LAZY 사용하는 멤버변수 NULL로 나옴
		// hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}

}
