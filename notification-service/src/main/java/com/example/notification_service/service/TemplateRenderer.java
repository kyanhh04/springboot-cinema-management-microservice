package com.example.notification_service.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TemplateRenderer {

    public String render(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }

        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            rendered = rendered.replace("{{" + entry.getKey() + "}}", value);
        }
        return rendered;
    }
}
