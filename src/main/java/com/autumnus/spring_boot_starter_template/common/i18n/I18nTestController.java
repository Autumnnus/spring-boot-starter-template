package com.autumnus.spring_boot_starter_template.common.i18n;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Test controller to demonstrate i18n functionality.
 * This controller shows how to use MessageService for internationalization.
 */
@RestController
@RequestMapping("/api/v1/i18n")
@RequiredArgsConstructor
@Tag(name = "I18n", description = "Internationalization test endpoints")
public class I18nTestController {

    private final MessageService messageService;

    @GetMapping("/test")
    @Operation(summary = "Test i18n message retrieval",
               description = "Returns a test message in the requested language. Use Accept-Language header to specify language (en, tr)")
    public ApiResponse<Map<String, String>> testMessage() {
        Map<String, String> messages = new HashMap<>();

        // Get messages using current locale (from Accept-Language header)
        messages.put("welcome", messageService.getMessage("app.welcome"));
        messages.put("current_locale", messageService.getCurrentLocale().toString());
        messages.put("login_success", messageService.getMessage("auth.login.success"));
        messages.put("user_created", messageService.getMessage("user.created"));

        return ApiResponse.success(messages);
    }

    @GetMapping("/test-all-locales")
    @Operation(summary = "Test all supported locales",
               description = "Returns the same message in all supported languages")
    public ApiResponse<Map<String, String>> testAllLocales() {
        Map<String, String> messages = new HashMap<>();

        // Get messages in different locales
        messages.put("en", messageService.getMessage("app.welcome", Locale.ENGLISH));
        messages.put("tr", messageService.getMessage("app.welcome", new Locale("tr")));

        return ApiResponse.success(messages);
    }

    @GetMapping("/test-parameters")
    @Operation(summary = "Test parameterized messages",
               description = "Returns a message with parameters")
    public ApiResponse<Map<String, String>> testParameters(
            @RequestParam(defaultValue = "Spring Boot") String appName) {
        Map<String, String> messages = new HashMap<>();

        // Get parameterized messages
        messages.put("welcome_with_param", messageService.getMessage("email.welcome.subject", appName));
        messages.put("min_length", messageService.getMessage("validation.min_length", 8));

        return ApiResponse.success(messages);
    }

    @GetMapping("/test-error")
    @Operation(summary = "Test error message localization",
               description = "Throws an error to demonstrate localized error messages")
    public ApiResponse<Void> testError() {
        throw new ResourceNotFoundException("resource.user.not_found");
    }
}
