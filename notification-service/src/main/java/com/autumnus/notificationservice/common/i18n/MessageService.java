package com.autumnus.notificationservice.common.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service for retrieving internationalized messages in notification service.
 * Provides methods for getting translated messages based on language tags.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Gets a message for a specific locale with parameters.
     *
     * @param code the message code
     * @param args the parameters to fill in the message
     * @param locale the locale to use
     * @return the translated message
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * Gets a message for a specific language tag with parameters.
     *
     * @param code the message code
     * @param args the parameters to fill in the message
     * @param languageTag the language tag (e.g., "en", "tr")
     * @return the translated message
     */
    public String getMessage(String code, Object[] args, String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag != null ? languageTag : "en");
        return getMessage(code, args, locale);
    }

    /**
     * Gets a message with a default value if the code is not found.
     *
     * @param code the message code
     * @param args the parameters to fill in the message
     * @param languageTag the language tag
     * @param defaultMessage the default message if code is not found
     * @return the translated message or default message
     */
    public String getMessageOrDefault(String code, Object[] args, String languageTag, String defaultMessage) {
        Locale locale = Locale.forLanguageTag(languageTag != null ? languageTag : "en");
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}
