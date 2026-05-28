package com.example.notification_service;

import com.example.common.exception.GlobalExceptionHandler;
import com.example.common.security.JwtAuthenticationDetails;
import com.example.notification_service.controller.NotificationController;
import com.example.notification_service.entity.EmailTemplate;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.entity.NotificationPreference;
import com.example.notification_service.service.EmailTemplateService;
import com.example.notification_service.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationApiContractTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailTemplateService emailTemplateService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new NotificationController(notificationService, emailTemplateService)
        ).setControllerAdvice(new GlobalExceptionHandler()).build();
        authenticateAs(1L, "admin", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void notificationEndpointsRespectContract() throws Exception {
        Notification notification = notification();
        NotificationPreference preference = new NotificationPreference();
        preference.setId(2L);
        preference.setUserId(7L);
        preference.setEmailNotificationsEnabled(true);

        when(notificationService.getAllNotifications()).thenReturn(List.of(notification));
        when(notificationService.getNotificationById(1L)).thenReturn(notification);
        when(notificationService.getFailedNotifications()).thenReturn(List.of(notification));
        when(notificationService.resendNotification(1L)).thenReturn(notification);
        when(notificationService.sendTestEmail(any())).thenReturn(notification);
        when(notificationService.getNotificationsByUserId(7L)).thenReturn(List.of(notification));
        when(notificationService.getPreferenceByUserId(7L)).thenReturn(preference);
        when(notificationService.updatePreference(eq(7L), any())).thenReturn(preference);

        mockMvc.perform(get("/api/notifications")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/api/notifications/1")).andExpect(status().isOk()).andExpect(jsonPath("$.recipient").value("user@example.com"));
        mockMvc.perform(get("/api/notifications/failed")).andExpect(status().isOk());
        mockMvc.perform(post("/api/notifications/1/resend")).andExpect(status().isOk());
        mockMvc.perform(post("/api/notifications/test-email").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"user@example.com\",\"subject\":\"Test\",\"message\":\"Hello\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/notifications/users/7")).andExpect(status().isOk());
        mockMvc.perform(get("/api/notifications/preferences/users/7")).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(7));
        mockMvc.perform(put("/api/notifications/preferences/users/7").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailNotificationsEnabled\":true,\"bookingCreatedEmailEnabled\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void userScopedEndpointsRejectOtherUsers() throws Exception {
        authenticateAs(99L, "jane", "USER");

        when(notificationService.getNotificationById(1L)).thenReturn(notification());

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
        mockMvc.perform(get("/api/notifications/users/7"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/notifications/preferences/users/7"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/notifications/preferences/users/7").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailNotificationsEnabled\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userScopedEndpointsAllowOwner() throws Exception {
        authenticateAs(7L, "john", "USER");

        Notification notification = notification();
        NotificationPreference preference = new NotificationPreference();
        preference.setId(2L);
        preference.setUserId(7L);
        preference.setEmailNotificationsEnabled(true);

        when(notificationService.getNotificationById(1L)).thenReturn(notification);
        when(notificationService.getNotificationsByUserId(7L)).thenReturn(List.of(notification));
        when(notificationService.getPreferenceByUserId(7L)).thenReturn(preference);
        when(notificationService.updatePreference(eq(7L), any())).thenReturn(preference);

        mockMvc.perform(get("/api/notifications/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/notifications/users/7")).andExpect(status().isOk());
        mockMvc.perform(get("/api/notifications/preferences/users/7")).andExpect(status().isOk());
        mockMvc.perform(put("/api/notifications/preferences/users/7").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailNotificationsEnabled\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void emailTemplateEndpointsRespectContract() throws Exception {
        EmailTemplate template = new EmailTemplate();
        template.setId(10L);
        template.setTemplateName("BOOKING_CREATED");
        template.setSubject("Booking {{bookingReference}}");
        template.setBodyHtml("<p>Hello</p>");
        template.setIsActive(true);

        when(emailTemplateService.getAllTemplates()).thenReturn(List.of(template));
        when(emailTemplateService.createTemplate(any())).thenReturn(template);
        when(emailTemplateService.updateTemplate(eq(10L), any())).thenReturn(template);
        doNothing().when(emailTemplateService).deleteTemplate(10L);

        mockMvc.perform(get("/api/notifications/templates")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].templateName").value("BOOKING_CREATED"));
        mockMvc.perform(post("/api/notifications/templates").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"templateName":"BOOKING_CREATED","subject":"Booking {{bookingReference}}","bodyHtml":"<p>Hello</p>"}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/notifications/templates/10").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"templateName":"BOOKING_CREATED","subject":"Booking {{bookingReference}}","bodyHtml":"<p>Hello</p>"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/notifications/templates/10")).andExpect(status().isNoContent());
    }

    private Notification notification() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUserId(7L);
        notification.setType(Notification.NotificationType.EMAIL);
        notification.setTitle("Booking Created");
        notification.setSubject("Booking Created");
        notification.setMessage("Created");
        notification.setRecipient("user@example.com");
        notification.setStatus(Notification.NotificationStatus.SENT);
        return notification;
    }

    private void authenticateAs(Long userId, String username, String role) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        authentication.setDetails(new JwtAuthenticationDetails(userId, role, new MockHttpServletRequest()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
