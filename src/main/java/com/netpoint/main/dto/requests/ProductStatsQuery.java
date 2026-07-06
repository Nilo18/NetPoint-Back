package com.netpoint.main.dto.requests;

import lombok.Data;

@Data
public class ProductStatsQuery {
    private String search = "";

    private String filterBy = "";
    private String filterFrom = "";
    private String filterTo = "";
}
