package com.netpoint.main.dto.requests;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class SalesQuery {
    private int page = 0;
    private int size = 10;
    private String search = "";
    private String sortBy = "";
    private String sortDirection = "";
    private String filterBy = "";
    private String filterFrom = "";
    private String filterTo = "";
}
