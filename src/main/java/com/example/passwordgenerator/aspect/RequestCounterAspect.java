package com.example.passwordgenerator.aspect;

import com.example.passwordgenerator.counter.RequestCounterInterface;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Aspect
@Component
public class RequestCounterAspect {

    private final RequestCounterInterface requestCounter;

    public RequestCounterAspect(RequestCounterInterface requestCounter) {
        this.requestCounter = requestCounter;
    }

    @Before(
            "execution(* com.example.passwordgenerator.service.PasswordService.generatePassword(..)) || " +
                    "execution(* com.example.passwordgenerator.service.PasswordService.generatePasswordsBulk(..))"
    )
    public void countPasswordGeneration(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();

        if ("generatePassword".equals(methodName)) {
            requestCounter.increment();
        } else if ("generatePasswordsBulk".equals(methodName)) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof List<?> requests) {
                requests.stream()
                        .filter(Objects::nonNull)
                        .forEach(r -> requestCounter.increment());
            }
        }
    }
}
