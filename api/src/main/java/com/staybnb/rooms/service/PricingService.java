package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Pricing;
import com.staybnb.rooms.domain.Room;
import com.staybnb.rooms.dto.request.SearchPricingRequest;
import com.staybnb.rooms.dto.request.UpdatePricingRequest;
import com.staybnb.rooms.dto.response.PricingResponse;
import com.staybnb.rooms.repository.PricingRepository;
import com.staybnb.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRepository pricingRepository;
    private final RoomRepository roomRepository;

    /**
     * 숙박 총 가격 조회
     */
    public PricingResponse getPricing(Long roomId, SearchPricingRequest request) {
        // TODO: 요청값 유효성 검사 (존재하는 roomId 인지, 유효한 날짜 범위인지)

        int totalPrice = calcTotalPrice(roomId, request.getStartDate(), request.getEndDate().minusDays(1));

        return PricingResponse.builder()
                .roomId(roomId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalPrice((double) totalPrice) // TODO: request 통화 기준 환산
                .build();
    }

    /**
     * 선택 구간들 숙박 가격 변경
     */
    @Transactional
    public void updateSelectedDatesPricing(Long roomId, UpdatePricingRequest request) {
        // TODO: 요청값 유효성 검사 (존재하는 roomId 인지, 유효한 날짜 범위인지)
        Room room = roomRepository.findById(roomId).orElseThrow(); // TODO: Exception handling

        // 숙박 가격 변경
        request.getDateSelected().forEach(range ->
                updatePricing(room, range.getStartDate(), range.getEndDate(), request.getPricePerNight()));
    }

    /**
     * startDate ~ endDate 구간 숙박 가격 변경
     */
    private void updatePricing(Room room, LocalDate startDate, LocalDate endDate, int pricePerNight) {
        updateExistingPricing(room, startDate, endDate);
        pricingRepository.save(new Pricing(room, startDate, endDate, pricePerNight));
    }

    /**
     * 날짜가 겹치는 기존 pricing 데이터가 있을 경우,
     * 기존 데이터는 삭제하고 겹치지 않는 구간 데이터만 다시 저장
     */
    private void updateExistingPricing(Room room, LocalDate startDate, LocalDate endDate) {
        List<Pricing> conflictedPricingList = pricingRepository.findPricingsByDate(room.getId(), startDate, endDate);
        pricingRepository.deleteAll(conflictedPricingList);

        for (Pricing conflicted : conflictedPricingList) {
            if (conflicted.getStartDate().isBefore(startDate)) {
                pricingRepository.save(new Pricing(room, conflicted.getStartDate(), startDate.minusDays(1), conflicted.getPricePerNight()));
            }
            if (conflicted.getEndDate().isAfter(endDate)) {
                pricingRepository.save(new Pricing(room, endDate.plusDays(1), conflicted.getEndDate(), conflicted.getPricePerNight()));
            }
        }
    }

    /**
     * [startDate, endDate] 기간 총 숙박 가격 계산
     */
    private int calcTotalPrice(Long roomId, LocalDate startDate, LocalDate endDate) {
        int totalDays = countDays(startDate, endDate);
        int totalPrice = 0;

        List<Pricing> pricingList = pricingRepository.findPricingsByDate(roomId, startDate, endDate);

        for (Pricing pricing : pricingList) {
            int days = countDaysWithinRange(pricing, startDate, endDate);
            totalPrice += days * pricing.getPricePerNight();
            totalDays -= days;
        }

        totalPrice += totalDays * roomRepository.findById(roomId).get().getBasePrice();

        return totalPrice;
    }

    /**
     * [(startDate, endDate] 일수 카운트
     */
    private int countDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * pricing 날짜 범위 중 [startDate, endDate] 구간에 포함되는 일수 카운트
     */
    private int countDaysWithinRange(Pricing pricing, LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(
                pricing.getStartDate().isBefore(startDate) ? startDate : pricing.getStartDate(),
                pricing.getEndDate().isAfter(endDate) ? endDate : pricing.getEndDate()
        ) + 1;
    }
}
