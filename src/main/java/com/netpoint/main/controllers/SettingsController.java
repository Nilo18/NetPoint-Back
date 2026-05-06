package com.netpoint.main.controllers;

import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.requests.CashierAdditionRequest;
import com.netpoint.main.dto.responses.CashierAdditionResponse;
import com.netpoint.main.dto.responses.PageResponse;
import com.netpoint.main.models.User;
import com.netpoint.main.services.SettingsService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/settings")
@Data
public class SettingsController {
    private final SettingsService settingsService;

    @GetMapping(path = "/company-users/{id}")
    public ResponseEntity<PageResponse<UserDTO>> getCompanyUsers(@PathVariable Long id,
    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                PageResponse.from(settingsService.fetchCompanyUsers(id, PageRequest.of(page, size)))
        );
    }

    @PostMapping(path = "/add-cashier")
    public ResponseEntity<CashierAdditionResponse> addCashier(
            @Valid @RequestBody CashierAdditionRequest cashier) {
        return ResponseEntity.ok(this.settingsService.addCashier(cashier));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        settingsService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}
