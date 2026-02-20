package org.nikitakapustkin.application.ports.in;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DifferentAccountsValidator.class)
@Documented
public @interface DifferentAccounts {
  String message() default "fromAccountId and toAccountId must be different";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
