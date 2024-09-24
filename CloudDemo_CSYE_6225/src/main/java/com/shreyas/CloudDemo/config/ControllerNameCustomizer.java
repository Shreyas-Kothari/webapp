package com.shreyas.CloudDemo.config;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.List;


@Component
public class ControllerNameCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String customName = controllerName.replaceAll("([a-z])([A-Z]+)", "$1 $2")
                .replace("Controller", "").trim();

        List<String> tags = new ArrayList<>();
        tags.add(customName);
        operation.setTags(tags);

        return operation;
    }
}