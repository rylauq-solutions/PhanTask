package com.phantask.notification.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Comprehensive tests for EmailService
 * Tests email sending functionality with mocked JavaMailSender
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private String toEmail;
    private String username;
    private String tempPassword;

    @BeforeEach
    void setUp() {
        toEmail = "test@example.com";
        username = "testuser";
        tempPassword = "TempPass123";
    }

    // ==================== SEND ACCOUNT CREATION EMAIL Tests ====================

    @Test
    void sendAccountCreationEmail_WithValidData_ShouldSendEmail() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAccountCreationEmail_ShouldSetCorrectRecipient() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getTo());
        assertEquals(1, sentMessage.getTo().length);
        assertEquals(toEmail, sentMessage.getTo()[0]);
    }

    @Test
    void sendAccountCreationEmail_ShouldSetCorrectSubject() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("Your Phantask Account Has Been Created", sentMessage.getSubject());
    }

    @Test
    void sendAccountCreationEmail_ShouldIncludeUsernameInBody() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(username));
        assertTrue(sentMessage.getText().contains("Username: " + username));
    }

    @Test
    void sendAccountCreationEmail_ShouldIncludeTempPasswordInBody() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains(tempPassword));
        assertTrue(sentMessage.getText().contains("Temporary Password: " + tempPassword));
    }

    @Test
    void sendAccountCreationEmail_ShouldContainGreeting() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Hello"));
    }

    @Test
    void sendAccountCreationEmail_ShouldContainSuccessMessage() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Your account has been successfully created"));
    }

    @Test
    void sendAccountCreationEmail_ShouldContainPasswordChangeInstruction() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Please re-login and change your password"));
    }

    @Test
    void sendAccountCreationEmail_ShouldContainSignature() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Phantask Team"));
    }

    @Test
    void sendAccountCreationEmail_WithDifferentEmail_ShouldSendToCorrectAddress() {
        // Arrange
        String differentEmail = "different@example.com";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(differentEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(differentEmail, sentMessage.getTo()[0]);
    }

    @Test
    void sendAccountCreationEmail_WithDifferentUsername_ShouldIncludeInBody() {
        // Arrange
        String differentUsername = "johndoe";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, differentUsername, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains(differentUsername));
    }

    @Test
    void sendAccountCreationEmail_WithDifferentPassword_ShouldIncludeInBody() {
        // Arrange
        String differentPassword = "NewPass456";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, differentPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains(differentPassword));
    }

    @Test
    void sendAccountCreationEmail_WhenMailSenderThrowsException_ShouldPropagateException() {
        // Arrange
        doThrow(new RuntimeException("Mail server error"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            emailService.sendAccountCreationEmail(toEmail, username, tempPassword)
        );
    }

    @Test
    void sendAccountCreationEmail_WithMailException_ShouldPropagateException() {
        // Arrange
        doThrow(new MailException("SMTP error") {})
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailException.class, () ->
            emailService.sendAccountCreationEmail(toEmail, username, tempPassword)
        );
    }

    @Test
    void sendAccountCreationEmail_ShouldCallMailSenderOnce() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAccountCreationEmail_WithNullEmail_ShouldStillAttemptToSend() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(null, username, tempPassword);

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAccountCreationEmail_WithEmptyEmail_ShouldStillAttemptToSend() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail("", username, tempPassword);

        // Assert
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendAccountCreationEmail_WithSpecialCharactersInUsername_ShouldHandleCorrectly() {
        // Arrange
        String specialUsername = "user@123.com";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, specialUsername, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains(specialUsername));
    }

    @Test
    void sendAccountCreationEmail_WithSpecialCharactersInPassword_ShouldHandleCorrectly() {
        // Arrange
        String specialPassword = "P@ssw0rd!#$";
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, specialPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains(specialPassword));
    }

    @Test
    void sendAccountCreationEmail_ShouldFormatMessageCorrectly() {
        // Arrange
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(mailSender).send(messageCaptor.capture());

        // Act
        emailService.sendAccountCreationEmail(toEmail, username, tempPassword);

        // Assert
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage.getText());
        assertFalse(sentMessage.getText().isEmpty());
        // Ensure formatted string is used (contains both username and password)
        assertTrue(sentMessage.getText().contains(username) && 
                   sentMessage.getText().contains(tempPassword));
    }
}
