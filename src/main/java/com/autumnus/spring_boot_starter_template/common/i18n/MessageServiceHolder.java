package com.autumnus.spring_boot_starter_template.common.i18n;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class to provide static access to MessageService.
 * This is useful for classes that cannot have dependencies injected (e.g., enums, static methods).
 */
@Component
public class MessageServiceHolder {

    private static MessageService messageService;

    @Autowired
    public MessageServiceHolder(MessageService messageService) {
        MessageServiceHolder.messageService = messageService;
    }

    /**
     * Gets a message for the current locale.
     *
     * @param code the message code
     * @return the translated message
     */
    public static String getMessage(String code) {
        return messageService.getMessage(code);
    }

    /**
     * Gets a message for the current locale with parameters.
     *
     * @param code the message code
     * @param args the parameters to fill in the message
     * @return the translated message
     */
    public static String getMessage(String code, Object... args) {
        return messageService.getMessage(code, args);
    }
}
