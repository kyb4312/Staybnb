package com.staybnb.rooms.service;

import com.staybnb.rooms.domain.Pricing;
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
     * 선택 구간 숙박 가격 변경
     */
    @Transactional
    public void updateSelectedDatesPricing(Long roomId, UpdatePricingRequest request) {
        // TODO: 요청값 유효성 검사 (존재하는 roomId 인지, 유효한 날짜 범위인지)

        // 숙박 가격 변경
        request.getDateSelected().forEach(range ->
                updatePricing(roomId, range.getStartDate(), range.getEndDate(), request.getPricePerNight()));
    }

    /**
     * startDate ~ endDate 구간 숙박 가격 변경
     */
    private void updatePricing(Long roomId, LocalDate startDate, LocalDate endDate, int pricePerNight) {
        // 날짜가 겹치는 기존 pricing 데이터 업데이트
        updateExistingPricing(roomId, startDate, endDate);

        // 요청에 대한 새로운 pricing 데이터 저장
        pricingRepository.save(Pricing.builder()
                .room(roomRepository.findById(roomId).get())
                .startDate(startDate)
                .endDate(endDate)
                .pricePerNight(pricePerNight)
                .build());
    }

    /**
     * 날짜가 겹치는 기존 pricing 데이터가 있을 경우,
     * 겹치지 않는 구간 데이터만 다시 저장하고 기존 데이터는 삭제
     */
    private void updateExistingPricing(Long roomId, LocalDate startDate, LocalDate endDate) {
        // 날짜가 겹치는 pricing 데이터 조회
        List<Pricing> pricingList = pricingRepository.findPricingsByDate(roomId, startDate, endDate);

        for (Pricing pricing : pricingList) {
            // startDate 보다 이전 날짜 구간이 있는 경우
            Pricing pricingBeforeStartDate = getPricingBeforeStartDate(pricing, startDate);
            if (pricingBeforeStartDate != null) {
                pricingRepository.save(pricingBeforeStartDate);
            }
            // endDate 보다 이후 날짜 구간이 있는 경우
            Pricing pricingAfterEndDate = getPricingAfterEndDate(pricing, endDate);
            if (pricingAfterEndDate != null) {
                pricingRepository.save(pricingAfterEndDate);
            }
            // 기존 데이터 삭제
            pricingRepository.delete(pricing);
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

    /**
     * startDate 보다 이전 구간 pricing 추출
     */
    private Pricing getPricingBeforeStartDate(Pricing pricing, LocalDate startDate) {
        if (pricing.getStartDate().isBefore(startDate)) {
            return Pricing.builder()
                    .room(pricing.getRoom())
                    .startDate(pricing.getStartDate())
                    .endDate(startDate.minusDays(1))
                    .pricePerNight(pricing.getPricePerNight())
                    .build();
        }
        return null;
    }

    /**
     * endDate 보다 이후 구간 pricing 추출
     */
    private Pricing getPricingAfterEndDate(Pricing pricing, LocalDate endDate) {
        if (pricing.getEndDate().isAfter(endDate)) {
            return Pricing.builder()
                    .room(pricing.getRoom())
                    .startDate(endDate.plusDays(1))
                    .endDate(pricing.getEndDate())
                    .pricePerNight(pricing.getPricePerNight())
                    .build();
        }
        return null;
    }
}
