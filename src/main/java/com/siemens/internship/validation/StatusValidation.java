package com.siemens.internship.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StatusValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StatusValidation {
    String message() default "Invalid status! Allowed values: Processed, Not_Processed!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
