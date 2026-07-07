package com.netpoint.main.dto.responses;

import com.netpoint.main.dto.TopProfitableItemDTO;
import com.netpoint.main.dto.MonthlyFinancialsDTO;

import java.util.List;

public record ProductChartsResponse(
        List<MonthlyFinancialsDTO> monthlyData,
        List<TopProfitableItemDTO> topSixProducts
) {
}
