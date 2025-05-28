package com.example.passwordgenerator.aspect;

import com.example.passwordgenerator.cache.PasswordCache;
import com.example.passwordgenerator.repository.PasswordRepository;
import com.example.passwordgenerator.service.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class LoggingAspectTest {

    @Test
    public void testLoggingAspectInitialization() {

        PasswordRepository passwordRepository = mock(PasswordRepository.class);
        PasswordCache passwordCache = new PasswordCache();
        PasswordService target = new PasswordService(passwordRepository, passwordCache);

        LoggingAspect aspect = new LoggingAspect();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        PasswordService proxy = factory.getProxy();

        if (proxy == null) {
            fail("Прокси не должен быть null!");
        }

        String password = proxy.generatePassword(8, 2);

        if (password == null) {
            fail("Пароль не должен быть null!");
        }
    }
}