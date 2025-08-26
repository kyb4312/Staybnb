package com.staybnb.common.validation.annotation.validator;

import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.common.validation.annotation.ValidPriceRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PriceRangeValidator implements ConstraintValidator<ValidPriceRange, SearchRoomRequest> {

    @Override
    public boolean isValid(SearchRoomRequest request, ConstraintValidatorContext context) {
        if(request.getPriceFrom() == null || request.getPriceTo() == null) {
            return true;
        }
        return request.getPriceFrom() <= request.getPriceTo();
    }
}
