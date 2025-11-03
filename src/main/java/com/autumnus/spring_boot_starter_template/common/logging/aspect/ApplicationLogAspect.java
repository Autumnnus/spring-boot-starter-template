package com.autumnus.spring_boot_starter_template.common.logging.aspect;

import com.autumnus.spring_boot_starter_template.common.logging.annotation.NoLog;
import com.autumnus.spring_boot_starter_template.common.logging.enums.LogLevel;
import com.autumnus.spring_boot_starter_template.common.logging.service.ApplicationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging service method executions
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationLogAspect {

    private final ApplicationLogService applicationLogService;

    @Around("execution(* com.autumnus.spring_boot_starter_template.modules..service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // Skip if method is annotated with @NoLog
        if (signature.getMethod().isAnnotationPresent(NoLog.class)) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();
        long startTime = System.currentTimeMillis();

        log.debug("Entering method: {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("Exiting method: {}.{} - Duration: {}ms", className, methodName, duration);

            // Log successful execution
            applicationLogService.createLog(
                    LogLevel.DEBUG,
                    String.format("Method %s.%s executed successfully in %dms", className, methodName, duration),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    null
            );

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("Error in method: {}.{} - Duration: {}ms", className, methodName, duration, e);

            // Log exception
            applicationLogService.createLog(
                    LogLevel.ERROR,
                    String.format("Method %s.%s failed after %dms: %s",
                            className, methodName, duration, e.getMessage()),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    e
            );

            throw e;
        }
    }

    @Around("execution(* com.autumnus.spring_boot_starter_template.modules..controller..*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        log.info("Controller method called: {}.{}", className, methodName);

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("Controller method failed: {}.{}", className, methodName, e);
            throw e;
        }
    }
}
