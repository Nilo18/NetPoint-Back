package com.netpoint.main.services;

import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.requests.CashierAdditionRequest;
import com.netpoint.main.dto.responses.UserModificationResponse;
import com.netpoint.main.exceptions.EmailAlreadyExistsException;
import com.netpoint.main.exceptions.InvalidPinException;
import com.netpoint.main.exceptions.UnallowedRoleException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettingsServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @InjectMocks private SettingsService settingsService;

    @Test
    public void shouldAddCashier() {
        Company company = new Company(
                22, "Company1", "company@gmail.com",
                "password123", "Retail",
                new ArrayList<>()
            );
        CashierAdditionRequest request = new CashierAdditionRequest(
                "Bob", "bob@gmail.com", "cashier", "123456", 22);

        when(userRepository.existsByEmail("bob@gmail.com")).thenReturn(false);
        when(companyRepository.findById(22L)).thenReturn(Optional.of(company));
        when(passwordEncoder.encode("123456")).thenReturn("encoded123456");
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserDTO> response = settingsService.addCashier(request, pageable);
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("invalidCashierProvider")
    public void shouldThrowOnInvalidCashier(CashierAdditionRequest request,
                    @PageableDefault(page = 1, size = 10) Pageable pageable,
                    Class<? extends Exception> expectedException) {
        assertThrows(expectedException, () -> settingsService.addCashier(request, pageable));
    }

    static Stream<Arguments> invalidCashierProvider() {
        return Stream.of(
            Arguments.of(new CashierAdditionRequest("Bob", "bob@gmail.com",
            "admin", "123456", 22), UnallowedRoleException.class),
            Arguments.of(new CashierAdditionRequest("Alice", "alice@gmail.com",
            "cashier", "asddqwwqewq", 22), InvalidPinException.class)
        );
    }
}
