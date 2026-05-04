package com.netpoint.main.services;

import com.netpoint.main.dto.requests.LoginRequest;
import com.netpoint.main.dto.requests.VerifyOtpRequest;
import com.netpoint.main.dto.responses.AuthResponse;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.OtpEntry;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    // AuthService- ყველა dependency Mock-ებია
    // ანუ რეალური DB, იმეილი, JWT აარ მუსაობს მარტო AuthService-ის ლოგიკას ტესტავს

    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private OtpStore otpStore;
    @Mock private EmailService emailService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // ტყუილ იუზერს ქმნის
    private User makeUser(String role) {
        Company company = new Company();
        company.setId(1);

        User user = new User();
        user.setId(1);
        user.setEmail("test@netpoint.com");
        user.setPassword("hashed");
        user.setRole(role);
        user.setName("Test User");
        user.setCompanyId(company);
        return user;
    }



    @Test
    void login_cashier_returnsJwtDirectly() {
        User user = makeUser("CASHIER");
        LoginRequest req = new LoginRequest("test@netpoint.com", "raw", "CASHIER");

        // რო გამოიძახება აბრუნებს ტყუილ იუზერს
        when(userRepository.findByEmail("test@netpoint.com")).thenReturn(Optional.of(user));
        //სწორ პაროლზე თრუს აბრუნესბ
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);
        //რო გამოიძახება ტყუილ ჯვტ სტრინგს აბრნებს
        when(jwtService.generateToken(any(), any(), any(), any(), any())).thenReturn("jwt-123");

        AuthResponse response = authService.login(req);
        //ქეშიერი პირდაპირ იღებს აუთენტიკეიტედ სტატუსს
        assertEquals("authenticated", response.status());
        assertEquals("jwt-123", response.token());
        // ეს ამოწმებს რო მეთოდი არ გამოიძახა ქეშიერსე
        verify(emailService, never()).sendOtpEmail(any(), any());
    }

    @Test
    void login_admin_triggers2fa() {
        User user = makeUser("ADMIN");
        LoginRequest req = new LoginRequest("test@netpoint.com", "raw", "ADMIN");

        when(userRepository.findByEmail("test@netpoint.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);
        // otpStore.save() OTPს ინახავს და დროებით ტოკენს აბრუნებს
        when(otpStore.save(any(), any(), any())).thenReturn("temp-token-abc");

        AuthResponse response = authService.login(req);
        // ადმინი 2fa_required სტატუსს უნდა იღებდეს
        assertEquals("2fa_required", response.status());
        assertEquals("temp-token-abc", response.token());
        // verify() ამოწმებს რომ sendOtpEmail სწორ იმეილზე გამოიძახეს
        verify(emailService).sendOtpEmail(eq("test@netpoint.com"), anyString());
    }

    @Test
    void login_owner_triggers2fa() {
        User user = makeUser("OWNER");
        LoginRequest req = new LoginRequest("test@netpoint.com", "raw", "OWNER");

        when(userRepository.findByEmail("test@netpoint.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);
        when(otpStore.save(any(), any(), any())).thenReturn("temp-token-owner");

        AuthResponse response = authService.login(req);
        // Owner-იც 2FA-ს გადის
        assertEquals("2fa_required", response.status());
        verify(emailService).sendOtpEmail(eq("test@netpoint.com"), anyString());
    }

    @Test
    void login_userNotFound_throwsUserNotFoundException() {
        LoginRequest req = new LoginRequest("nobody@test.com", "raw", "CASHIER");
        // Optional.empty() ბაზაში იუზერი არ არსებობს
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(req));
    }

    @Test
    void login_wrongPassword_throwsInvalidPasswordException() {
        User user = makeUser("CASHIER");
        LoginRequest req = new LoginRequest("test@netpoint.com", "wrongpass", "CASHIER");

        when(userRepository.findByEmail("test@netpoint.com")).thenReturn(Optional.of(user));
        // passwordEncoder.matches() false აბრუნებს პაროლი არასწორია

        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> authService.login(req));
    }

    @Test
    void login_wrongRole_throwsInvalidRoleException() {
        User user = makeUser("CASHIER");
        // იუზერი ქეშიერი გლეხია მარა ადმინად ცდის შესვლას
        LoginRequest req = new LoginRequest("test@netpoint.com", "raw", "ADMIN");

        when(userRepository.findByEmail("test@netpoint.com")).thenReturn(Optional.of(user));
        // როლი როცა არ ემთხვევა ისვრის InvalidRoleException
        assertThrows(InvalidRoleException.class, () -> authService.login(req));
    }



    @Test
    void verifyOtp_validCode_returnsJwt() {
        OtpEntry entry = mock(OtpEntry.class);
        when(entry.getOtpCode()).thenReturn("123456");
        when(entry.isExpired()).thenReturn(false);
        when(entry.getUserId()).thenReturn("1");

        User user = makeUser("OWNER");
        VerifyOtpRequest req = new VerifyOtpRequest("temp-token", "123456");

        when(otpStore.get("temp-token")).thenReturn(entry);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any(), any(), any(), any())).thenReturn("final-jwt");

        AuthResponse response = authService.verifyOtp(req);

        assertEquals("authenticated", response.status());
        assertEquals("final-jwt", response.token());
        // ოტპ უნდა წაიშალოს წარმატებული ვერიფიკაციის მერე
        verify(otpStore).invalidate("temp-token");
    }

    @Test
    void verifyOtp_wrongCode_throwsInvalidOtpException() {
        OtpEntry entry = mock(OtpEntry.class);
        when(entry.getOtpCode()).thenReturn("123456");
        // იუზერმა "000000" შეიყვანა, სწორია "123456"
        VerifyOtpRequest req = new VerifyOtpRequest("temp-token", "000000");
        when(otpStore.get("temp-token")).thenReturn(entry);

        assertThrows(InvalidOtpException.class, () -> authService.verifyOtp(req));
        // ინვალიდაცია უნდა ქნას არასწორ გამოცნობაზე brue force-დან იცავს
        verify(otpStore).invalidate("temp-token");
    }

    @Test
    void verifyOtp_expiredCode_throwsOtpExpiredException() {
        OtpEntry entry = mock(OtpEntry.class);
        when(entry.getOtpCode()).thenReturn("123456");
        // კოდი ვადაგასულია - 5 წუთი გავიდა

        when(entry.isExpired()).thenReturn(true);

        VerifyOtpRequest req = new VerifyOtpRequest("temp-token", "123456");
        when(otpStore.get("temp-token")).thenReturn(entry);
        // კოდი სწორია მაგრამ ვადაგასული - OtpExpiredException უნდა ისროლოს
        assertThrows(OtpExpiredException.class, () -> authService.verifyOtp(req));
    }
}