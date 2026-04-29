package com.netpoint.main.dto.responses;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> userList,
        int page,
        int size,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<T>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages()
        );
    }
}
