package com.netpoint.main.dto.responses;

import com.netpoint.main.dto.CompanyDTO;
import com.netpoint.main.dto.UserDTO;

public record CompanyUserPayload(
        CompanyDTO company,
        UserDTO user
) {
}
