package com.staybnb.common.validation.annotation.validator;

import com.staybnb.rooms.dto.request.SearchRoomRequest;
import com.staybnb.common.validation.annotation.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, SearchRoomRequest> {

    @Override
    public boolean isValid(SearchRoomRequest request, ConstraintValidatorContext context) {
        if(request.getStartDate() == null && request.getEndDate() == null){
            return true;
        }
        if(request.getStartDate() == null || request.getEndDate() == null){
            return false;
        }

        // 유효 조건: LocalDate.now() <= startDate < EndDate < (LocalDate.now() + 365days)
        return request.getStartDate().isAfter(LocalDate.now().minusDays(1))
                && request.getStartDate().isBefore(request.getEndDate())
                && ChronoUnit.DAYS.between(LocalDate.now(), request.getEndDate()) < 365;
    }
}
