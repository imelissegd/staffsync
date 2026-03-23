package com.mgd.employee_mgmt.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Convenience wrapper around Spring's MessageSource.
 * All services and controllers use this to resolve messages from messages.properties.
 */
@Component
public class MessageUtil {

    private final MessageSource messageSource;

    @Autowired
    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolve a message key with no arguments.
     */
    public String get(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    /**
     * Resolve a message key with positional {0}, {1}, ... placeholders.
     */
    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault());
    }
}
