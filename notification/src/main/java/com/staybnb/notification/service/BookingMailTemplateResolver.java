package com.staybnb.notification.service;

import com.staybnb.common.event.BookingEvent;
import com.staybnb.notification.dto.MailTarget;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMailTemplateResolver {

    public List<MailTarget> resolve(BookingEvent event) {
        String hostEmail = event.getHostEmail();
        String guestEmail = event.getGuestEmail();
        String roomTitle = event.getRoomTitle();
        String hostName = event.getHostName();
        String guestName = event.getGuestName();
        Integer numberOfGuests = event.getNumberOfGuests();
        String checkIn = event.getCheckIn();
        String checkOut = event.getCheckOut();
        Double bookingPrice = event.getBookingPrice();
        Long bookingId = event.getBookingId();

        switch (event.getBookingStatus()) {
            case REQUESTED -> {
                return List.of(
                        new MailTarget(hostEmail,
                                "[Staybnb] 새 예약 요청이 도착했습니다",
                                String.format("호스트 %s님,\n게스트 %s님이 '%s' 숙소에 %d명 예약을 요청했습니다.\n체크인: %s, 체크아웃: %s\n예약 가격: %.2f\n예약 ID: %d",
                                        hostName, guestName, roomTitle, numberOfGuests, checkIn, checkOut, bookingPrice, bookingId)),
                        new MailTarget(guestEmail,
                                "[Staybnb] 예약 요청이 접수되었습니다",
                                String.format("게스트 %s님,\n'%s' 숙소 예약 요청이 정상적으로 접수되었습니다.\n체크인: %s, 체크아웃: %s\n예약 가격: %.2f\n예약 ID: %d",
                                        guestName, roomTitle, checkIn, checkOut, bookingPrice, bookingId))
                );
            }
            case RESERVED -> {
                return List.of(
                        new MailTarget(hostEmail,
                                "[Staybnb] 예약이 확정되었습니다",
                                String.format("호스트 %s님,\n게스트 %s님이 '%s' 숙소 예약을 확정했습니다.\n체크인: %s, 체크아웃: %s\n예약 가격: %.2f\n예약 ID: %d",
                                        hostName, guestName, roomTitle, checkIn, checkOut, bookingPrice, bookingId)),
                        new MailTarget(guestEmail,
                                "[Staybnb] 예약이 확정되었습니다",
                                String.format("게스트 %s님,\n'%s' 숙소 예약이 호스트에 의해 확정되었습니다.\n체크인: %s, 체크아웃: %s\n예약 가격: %.2f\n예약 ID: %d",
                                        guestName, roomTitle, checkIn, checkOut, bookingPrice, bookingId))
                );
            }
            case REJECTED -> {
                return List.of(
                        new MailTarget(hostEmail,
                                "[Staybnb] 예약 요청이 거절되었습니다",
                                String.format("호스트 %s님,\n게스트 %s님의 예약 요청이 거절되었습니다.\n숙소: %s\n예약 ID: %d",
                                        hostName, guestName, roomTitle, bookingId)),
                        new MailTarget(guestEmail,
                                "[Staybnb] 예약 요청이 거절되었습니다",
                                String.format("게스트 %s님,\n'%s' 숙소 예약 요청이 호스트에 의해 거절되었습니다.\n예약 ID: %d",
                                        guestName, roomTitle, bookingId))
                );
            }
            case CANCELLED -> {
                return List.of(
                        new MailTarget(hostEmail,
                                "[Staybnb] 예약이 취소되었습니다",
                                String.format("호스트 %s님,\n게스트 %s님이 예약을 취소했습니다.\n숙소: %s\n예약 ID: %d",
                                        hostName, guestName, roomTitle, bookingId)),
                        new MailTarget(guestEmail,
                                "[Staybnb] 예약이 취소되었습니다",
                                String.format("게스트 %s님,\n'%s' 숙소 예약이 취소되었습니다.\n예약 ID: %d",
                                        guestName, roomTitle, bookingId))
                );
            }
            case ONGOING -> {
                return List.of(
                        new MailTarget(hostEmail,
                                "[Staybnb] 숙박 예정 알림 – 체크인일 안내",
                                String.format("호스트 %s님,\n게스트 %s님의 '%s' 숙소 예약이 곧 시작됩니다.\n체크인 예정일: %s, 체크아웃 예정일: %s\n예약 ID: %d",
                                        hostName, guestName, roomTitle, checkIn, checkOut, bookingId)),
                        new MailTarget(guestEmail,
                                "[Staybnb] 체크인 예정일 안내",
                                String.format("게스트 %s님,\n'%s' 숙소 예약이 곧 시작됩니다.\n체크인 예정일: %s, 체크아웃 예정일: %s\n예약 ID: %d",
                                        guestName, roomTitle, checkIn, checkOut, bookingId))
                );
            }
            case ENDED -> {
                return List.of(
                        new MailTarget(hostEmail,
                                "[Staybnb] 숙박 종료일 안내",
                                String.format("호스트 %s님,\n게스트 %s님의 '%s' 숙소 예약이 종료 예정입니다.\n체크인일: %s, 체크아웃일: %s\n예약 ID: %d",
                                        hostName, guestName, roomTitle, checkIn, checkOut, bookingId)),
                        new MailTarget(guestEmail,
                                "[Staybnb] 체크아웃 예정일 안내",
                                String.format("게스트 %s님,\n'%s' 숙소 예약이 종료 예정입니다.\n체크인일: %s, 체크아웃일: %s\n예약 ID: %d",
                                        guestName, roomTitle, checkIn, checkOut, bookingId))
                );
            }
            default -> throw new IllegalArgumentException("Unknown BookingStatus: " + event.getBookingStatus());
        }
    }
}