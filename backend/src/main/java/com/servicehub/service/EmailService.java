package com.servicehub.service;

import com.servicehub.dto.ServiceRequestResponse;
import com.servicehub.model.enums.RequestStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@Profile("!test")
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.ENGLISH);

  @Value("${app.sla.sla-breach-notification.enabled:false}")
  private boolean isEmailEnabled;
  @Value("${app.sla.sla-breach-notification.admin-email}")
  private String adminEmail;
  @Value("${app.base-url:http://localhost:8080}")
  private String baseUrl;

  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(EmailService.class);


  public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
  }

  /**
   * Sends an SLA breach notification email to the configured admin address.
   *
   * @param previousStatus the status held before the breach was detected
   * @param serviceRequest the full DTO of the breached service request
   */
  @Async
  public void sendSlaNotification(RequestStatus previousStatus, ServiceRequestResponse serviceRequest) {
    logger.info("Preparing SLA breach notification for request #{} to {}", serviceRequest.getId(), adminEmail);

    if (!isEmailEnabled || adminEmail == null || adminEmail.isBlank()) {
      logger.warn("Email service is disabled or admin email is not configured. " +
              "Skipping SLA notification for request #{}.", serviceRequest.getId());
      return;
    }

    try {

      String html    = buildSlaEmailHtml(previousStatus, serviceRequest);
      String subject = buildSubject(serviceRequest);

      MimeMessage     message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(adminEmail);
      helper.setSubject(subject);
      helper.setText(html, true);

      mailSender.send(message);

      logger.info("SLA breach notification sent for request #{} — subject: \"{}\"", serviceRequest.getId(), subject);
    } catch (MessagingException e) {
      logger.error("Failed to send SLA notification email for request #{}: {}", serviceRequest.getId(), e.getMessage(), e);    }
    catch (Exception e) {
      logger.error("Unexpected error while sending SLA notification for request #{}: {}", serviceRequest.getId(), e.getMessage(), e);
    }
  }

  /**
   * Builds and populates the Thymeleaf context, then renders the HTML body.
   * Every variable the template accesses is set here — never rely on nulls
   * falling through silently.
   */
  private String buildSlaEmailHtml(RequestStatus previousStatus,
                                   ServiceRequestResponse serviceRequest) {

    Context ctx = new Context(Locale.ENGLISH);

    // ── Core data ──
    ctx.setVariable("serviceRequest", serviceRequest);
    ctx.setVariable("previousStatus",  previousStatus);   // may be null — template guards this
    ctx.setVariable("adminEmail",       adminEmail);
    ctx.setVariable("baseUrl",          baseUrl);

    ctx.setVariable("notifiedAt", LocalDateTime.now().format(DISPLAY_FORMATTER));

    return templateEngine.process("sla-email-notification", ctx);
  }

  /**
   * Constructs the email subject line for an SLA breach notification, incorporating the request ID, priority, and a truncated title.
   * @param req the service request for which the SLA breach occurred
   * @return a formatted subject line like "[SLA BREACH] #123 · HIGH · Printer not working…"
   *         If the title is missing, it will show "(No title)".
   *         If the title is longer than 60 characters, it will be truncated with an ellipsis.
   */
  private String buildSubject(ServiceRequestResponse req) {
    String priority = req.getPriority() != null ? req.getPriority() : "UNKNOWN";

    String rawTitle = req.getTitle();
    String title;
    if (rawTitle == null || rawTitle.isBlank()) {
      title = "(No title)";
    } else if (rawTitle.length() > 60) {
      title = rawTitle.substring(0, 57).stripTrailing() + "…";
    } else {
      title = rawTitle;
    }

    return String.format("[SLA BREACH] #%s · %s · %s",
            req.getId() != null ? req.getId() : "?",
            priority,
            title);
  }
}

