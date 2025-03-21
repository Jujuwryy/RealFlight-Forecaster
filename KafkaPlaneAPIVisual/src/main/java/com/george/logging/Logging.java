package com.george.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class Logging{

    private static final Logger logger = LoggerFactory.getLogger(Logging.class);

    // Log method execution for services and consumers only
    @Around("execution(* com.george.service.*.*(..)) || execution(* com.george.consumer.*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        logger.debug("Entering method: {} with arguments: {}", methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.debug("Exiting method: {} with result size: {} (took {} ms)", 
                methodName, 
                result instanceof List ? ((List<?>) result).size() : "N/A", 
                executionTime);
            return result;
        } catch (Throwable throwable) {
            logger.error("Exception in method: {} - {}", methodName, throwable.getMessage(), throwable);
            throw throwable;
        }
    }
}