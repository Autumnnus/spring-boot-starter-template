package com.autumnus.spring_boot_starter_template.common.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving internationalized messages.
 * Provides convenient methods for getting translated messages based on the current locale.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Gets a message for the current locale.
     *
     * @param code the message code
     * @return the translated message
     */
    public String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * Gets a message for the current locale with parameters.
     *
     * @param code the message code
     * @param args the parameters to fill in the message
     * @return the translated message
     */
    public String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, getCurrentLocale());
    }

    /**
     * Gets a message for a specific locale.
     *
     * @param code the message code
     * @param locale the locale to use
     * @return the translated message
     */
    public String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

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
     * Gets a message with a default value if the code is not found.
     *
     * @param code the message code
     * @param defaultMessage the default message if code is not found
     * @return the translated message or default message
     */
    public String getMessageOrDefault(String code, String defaultMessage) {
        return messageSource.getMessage(code, null, defaultMessage, getCurrentLocale());
    }

    /**
     * Gets a message with parameters and a default value if the code is not found.
     *
     * @param code the message code
     * @param args the parameters to fill in the message
     * @param defaultMessage the default message if code is not found
     * @return the translated message or default message
     */
    public String getMessageOrDefault(String code, Object[] args, String defaultMessage) {
        return messageSource.getMessage(code, args, defaultMessage, getCurrentLocale());
    }

    /**
     * Gets the current locale from the LocaleContextHolder.
     *
     * @return the current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}
