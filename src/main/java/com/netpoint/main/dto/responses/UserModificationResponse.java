package com.netpoint.main.dto.responses;

import com.netpoint.main.dto.UserDTO;

public record UserModificationResponse(Integer status, UserDTO user) {
}
