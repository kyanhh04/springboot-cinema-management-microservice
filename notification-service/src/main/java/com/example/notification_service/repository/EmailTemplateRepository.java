package com.example.notification_service.repository;

import com.example.notification_service.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByTemplateName(String templateName);
    Optional<EmailTemplate> findByTemplateNameAndIsActiveTrue(String templateName);
}
