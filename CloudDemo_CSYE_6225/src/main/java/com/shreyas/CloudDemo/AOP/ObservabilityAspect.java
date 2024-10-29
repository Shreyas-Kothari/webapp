package com.shreyas.CloudDemo.AOP;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ObservabilityAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(com.shreyas.CloudDemo.annotation.Observability) || @within(com.shreyas.CloudDemo.annotation.Observability)")
    public Object observeAPITimerCount(ProceedingJoinPoint joinPoint) throws Throwable {
        String apiPth = getAPIPath(joinPoint);
        try {
            // Execute the annotated method
            return joinPoint.proceed();
        } catch (Exception e) {
            meterRegistry.counter("api.call.errors" + apiPth, "application","csye6225","api", apiPth).increment();
            log.error("API call failed for {}", apiPth, e);
            throw e;
        } finally {
            // Increment counter for API call count
            meterRegistry.counter("api.call.count" + apiPth, "api", apiPth).increment();
            log.info("Counter metrics recorded for {}", apiPth);
        }
    }

    @Around("@annotation(com.shreyas.CloudDemo.annotation.Observability) || @within(com.shreyas.CloudDemo.annotation.Observability)")
    public Object observeAPITimer(ProceedingJoinPoint joinPoint) throws Throwable {
        String apiAPth = getAPIPath(joinPoint);
        // Start a timer for API call duration
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // Execute the annotated method
            return joinPoint.proceed();
        } finally {
            Timer timer = Timer.builder("api.call.duration" + apiAPth)
                    .description("API call duration in milliseconds")   
                    .tags("application","csye6225","api", apiAPth)
                    .register(meterRegistry);
            // Stop timer and record API call duration in milliseconds
            sample.stop(timer);
            log.info("Timer metrics recorded for {}", apiAPth);
        }
    }


    @Around("within(@org.springframework.stereotype.Repository *)")
    public Object observeDatabaseCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            return joinPoint.proceed();
        } finally {
            Timer timer = Timer.builder("database.query.duration." + methodName)
                    .description("Time taken for database queries in ms")
                    .tags("application","csye6225","repository", joinPoint.getSignature().getDeclaringTypeName(), "method", methodName)
                    .register(meterRegistry);
            sample.stop(timer);
            log.info("Database query duration recorded for {}", methodName);
        }
    }

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object observeServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            return joinPoint.proceed();
        } finally {
            String serviceName = joinPoint.getSignature().getDeclaringTypeName();
            Timer timer = Timer.builder(serviceName.contains("S3") ? "aws.s3.call.duration" : "service.call.duration")
                    .description("Time taken for service calls in ms")
                    .tags("application","csye6225","service", serviceName, "method", methodName)
                    .register(meterRegistry);
            sample.stop(timer);
            log.info("Service call duration recorded for {}", methodName);
        }
    }

    private String getAPIPath(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();

        RequestMapping classMapping = declaringClass.getAnnotation(RequestMapping.class);
        String classPath = classMapping != null ? classMapping.value()[0] : "";

        String methodPath = "";
        if (method.isAnnotationPresent(GetMapping.class)) {
            methodPath = "/" + HttpMethod.GET.name() + classPath + getMappingPath(method, GetMapping.class);
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            methodPath = "/" + HttpMethod.POST.name() + classPath + getMappingPath(method, PostMapping.class);
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            methodPath = "/" + HttpMethod.PUT.name() + classPath + getMappingPath(method, PutMapping.class);
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            methodPath = "/" + HttpMethod.DELETE.name() + classPath + getMappingPath(method, DeleteMapping.class);
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            methodPath = classPath + getMappingPath(method, RequestMapping.class);
        }
        return methodPath.replace("/", ".");
    }

    private String getMappingPath(Method method, Class<? extends Annotation> annotationType) {
        if (method.isAnnotationPresent(annotationType)) {
            Annotation annotation = method.getAnnotation(annotationType);
            try {
                Method valueMethod = annotationType.getDeclaredMethod("value");
                String[] values = (String[]) valueMethod.invoke(annotation);
                if (values.length > 0) {
                    return values[0];
                }
            } catch (Exception e) {
                // Handle or log the exception
                log.error("Error getting mapping path from {} annotation: {}", annotationType.getName(), e.getMessage(), e);
            }
        }
        return "";
    }
}

