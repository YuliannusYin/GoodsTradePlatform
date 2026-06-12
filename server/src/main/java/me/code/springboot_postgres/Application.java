/**
 * @file Application.java
 * @description 电商项目 Spring Boot 应用程序入口类，负责启动应用并配置 JPA 审计和仓库扫描
 * @input 命令行启动参数
 * @output 运行中的 Spring Boot 应用实例
 */
package me.code.springboot_postgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 电商项目主应用类
 * 职责：作为 Spring Boot 应用的入口，启用 JPA 审计功能和仓库自动扫描
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"me.code.springboot_postgres.repositories"})
public class Application {
    /**
     * 应用程序主入口方法
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
