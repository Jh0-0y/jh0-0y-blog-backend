plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.portfolio"
version = "0.0.1-SNAPSHOT"
description = "portfolio backend"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Data JPA: DB 연동 및 ORM
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	// Spring Security: 인증/인가
	implementation("org.springframework.boot:spring-boot-starter-security")
	// Bean Validation (javax.validation / Hibernate Validator)
	implementation("org.springframework.boot:spring-boot-starter-validation")
	// Spring Web MVC: REST API / 컨트롤러
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	// 테스트용 Spring Data JPA
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	// 테스트용 Spring Security
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	// 테스트용 Bean Validation
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	// 테스트용 Spring Web MVC
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	// Lombok: 코드 자동 생성 (getter/setter, 생성자 등)
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	// MariaDB 드라이버: runtime 시 DB 연결용
	runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
	// PostgreSQL 드라이버: runtime 시 DB 연결용 (사용 시 선택)
	runtimeOnly("org.postgresql:postgresql")
	// JUnit 런처: 테스트 실행 시 필요
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// jjwt API: 토큰 생성/검증용 공용 인터페이스
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	// jjwt 구현체: 실제 signing/verify 처리
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	// jjwt JSON 처리: Claim을 JSON으로 파싱/직렬화
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
