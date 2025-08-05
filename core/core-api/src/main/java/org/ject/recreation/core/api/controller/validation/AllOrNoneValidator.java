package org.ject.recreation.core.api.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.ject.recreation.core.api.controller.validation.contract.HasCursorFields;

public class AllOrNoneValidator implements ConstraintValidator<AllOrNone, HasCursorFields> {

    @Override
    public boolean isValid(HasCursorFields fields, ConstraintValidatorContext context) {
        boolean allNull = fields.getCursorFields().stream()
                .allMatch(field -> field == null);

        boolean allNotNull = fields.getCursorFields().stream()
                .noneMatch(field -> field == null);

        return allNull || allNotNull;
    }
}

