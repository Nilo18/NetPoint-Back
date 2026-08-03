package com.netpoint.main.dto.requests;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SalesStatsQuery {
    private String search = "";
    private String filterBy = "";
    private String filterFrom = "";
    private String filterTo = "";
}
