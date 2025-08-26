package com.staybnb.common.validation.annotation;

import com.staybnb.common.validation.annotation.validator.PriceRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceRangeValidator.class)
public @interface ValidPriceRange {
    String message() default "가격 범위가 유효하지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
