package com.netpoint.main.dto.responses;

import com.netpoint.main.dto.UserDTO;

public record CashierAdditionResponse(Integer status, UserDTO user) {
}
