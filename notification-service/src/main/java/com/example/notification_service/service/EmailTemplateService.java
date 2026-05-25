package com.example.notification_service.service;

import com.example.notification_service.entity.EmailTemplate;
import com.example.notification_service.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAll();
    }

    public EmailTemplate getTemplateById(Long id) {
        return emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email template not found with id: " + id));
    }

    public EmailTemplate getActiveTemplateByName(String templateName) {
        return emailTemplateRepository.findByTemplateNameAndIsActiveTrue(templateName)
                .orElseThrow(() -> new RuntimeException("Active email template not found: " + templateName));
    }

    @Transactional
    public EmailTemplate createTemplate(EmailTemplate template) {
        return emailTemplateRepository.save(template);
    }

    @Transactional
    public EmailTemplate updateTemplate(Long id, EmailTemplate templateDetails) {
        EmailTemplate template = getTemplateById(id);
        template.setTemplateName(templateDetails.getTemplateName());
        template.setSubject(templateDetails.getSubject());
        template.setBodyHtml(templateDetails.getBodyHtml());
        template.setBodyText(templateDetails.getBodyText());
        template.setVariables(templateDetails.getVariables());
        template.setIsActive(templateDetails.getIsActive());
        return emailTemplateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        emailTemplateRepository.delete(getTemplateById(id));
    }
}
