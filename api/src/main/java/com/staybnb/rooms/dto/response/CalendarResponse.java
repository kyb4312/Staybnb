package com.staybnb.rooms.dto.response;

import com.staybnb.rooms.dto.response.vo.DailyInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CalendarResponse {

    private Long roomId;
    private String currency;
    private List<DailyInfo> dailyInfos;
}
