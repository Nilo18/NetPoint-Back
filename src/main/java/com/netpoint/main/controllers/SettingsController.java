package com.netpoint.main.controllers;

import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.requests.CashierAdditionRequest;
import com.netpoint.main.dto.responses.PageResponse;
import com.netpoint.main.dto.responses.UserModificationResponse;
import com.netpoint.main.models.User;
import com.netpoint.main.services.SettingsService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<PageResponse<UserDTO>> addCashier(
            @Valid @RequestBody CashierAdditionRequest cashier,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(this.settingsService.addCashier(cashier, pageable)));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<UserModificationResponse> deleteUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(this.settingsService.deleteUser(userId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @GetMapping("/search")
    public ResponseEntity<?> searchUser(@RequestParam String searchTerm) {
        try {
            UserDTO user = settingsService.searchUser(searchTerm);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
