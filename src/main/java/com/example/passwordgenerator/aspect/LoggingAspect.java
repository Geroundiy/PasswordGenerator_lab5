package com.example.passwordgenerator.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.passwordgenerator.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Вызван метод: " + joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "execution(* com.example.passwordgenerator.controller.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("Метод " + joinPoint.getSignature().getName() + " завершился с результатом: " + result);
    }

    @AfterThrowing(pointcut = "execution(* com.example.passwordgenerator.controller.*.*(..))", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        logger.error("Ошибка в методе " + joinPoint.getSignature().getName() + ": " + error.getMessage());
    }
}