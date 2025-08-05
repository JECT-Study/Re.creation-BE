package org.ject.recreation.core.api.controller.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueQuestionOrderValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueQuestionOrder {
    String message() default "order 값들이 중복됩니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
