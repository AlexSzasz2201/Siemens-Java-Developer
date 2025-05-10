package com.siemens.internship.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    // We use a reasonably strict email regex (johndoe@example.com)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" +
                    "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null or black will be caught by @NotBlank, so we check only the pattern matching
        return value != null && EMAIL_PATTERN.matcher(value).matches();
    }
}
