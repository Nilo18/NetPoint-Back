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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    @Mock private AuditLogService auditLogService;
    @InjectMocks private SettingsService settingsService;

    @Test
    public void shouldAddCashier() {
        Company company = new Company();
        company.setId(1);
        company.setName("Test Company");
        company.setEmail("test@company.com");
        company.setIndustry("Tech");
        User actor = new User();
        actor.setId(99);
        CashierAdditionRequest request = new CashierAdditionRequest(
                "Bob", "bob@gmail.com", "cashier", "123456", 22);

        when(userRepository.existsByEmail("bob@gmail.com")).thenReturn(false);
        when(companyRepository.findById(22)).thenReturn(Optional.of(company));
        when(userRepository.findById(99)).thenReturn(Optional.of(actor));
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByCompany_Id(22, pageable)).thenReturn(new PageImpl<>(java.util.List.of()));
        Page<UserDTO> response = settingsService.addCashier(99, request, pageable);
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("invalidCashierProvider")
    public void shouldThrowOnInvalidCashier(CashierAdditionRequest request,
                    Class<? extends Exception> expectedException) {
        Pageable pageable = PageRequest.of(1, 10);
        assertThrows(expectedException, () -> settingsService.addCashier(99, request, pageable));
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
