package org.ject.recreation.core.api.controller.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.ject.recreation.core.api.controller.validation.contract.HasOrder;
import org.ject.recreation.core.api.controller.validation.contract.HasOrderedItems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueQuestionOrderValidator implements ConstraintValidator<UniqueQuestionOrder, HasOrderedItems> {

    private String errorMessage;

    @Override
    public void initialize(UniqueQuestionOrder constraintAnnotation) {
        this.errorMessage = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(HasOrderedItems request, ConstraintValidatorContext context) {
        List<? extends HasOrder> questions = request.getOrderedItems();

        if (questions == null) return true;
        List<Integer> questionOrders = questions.stream()
                .map(HasOrder::getOrder)
                .toList();

        Set<Integer> uniqueQuestionOrders = new HashSet<>(questionOrders);
        boolean valid = uniqueQuestionOrders.size() == questionOrders.size();

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage)
                    .addPropertyNode("questions")
                    .addConstraintViolation();
        }

        return valid;
    }
}

