package com.netpoint.main.services;

import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.requests.CashierAdditionRequest;
//import com.netpoint.main.dto.responses.CashierAdditionResponse;
import com.netpoint.main.dto.responses.UserModificationResponse;
import com.netpoint.main.exceptions.*;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Data
@Log
public class SettingsService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public Page<UserDTO> fetchCompanyUsers(Long id, Pageable pageable) {
        if (!companyRepository.existsById(id)) {
            throw new CompanyNotFoundException("Couldn't find company by id");
        }

        return userRepository.findByCompanyId_Id(id, pageable)
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()));
    }

    public Page<UserDTO> addCashier(CashierAdditionRequest cashier, Pageable pageable) {
        String role = cashier.role().trim().toLowerCase();

        if (!role.equals("cashier")) {
            throw new UnallowedRoleException("Only cashiers are allowed to be registered with a pin");
        }

        if (userRepository.existsByEmail(cashier.email())) {
            throw new EmailAlreadyExistsException("A user with this email already exists");
        }

        if (!cashier.pin().matches("\\d{6}")) {
            throw new InvalidPinException("Pin must be exactly 6 digits");
        }

        // ****
        // ADD A JWT AUTHORIZATION HERE LATER ON TO MAKE SURE THAT AN OWNER FROM
        // ANOTHER COMPANY DOESN'T ADD A CASHIER IN SOMEONE ELSE'S COMPANY
        // ****

        Company company = this.companyRepository.findById(Long.valueOf(cashier.companyId()))
                .orElseThrow(() -> new CompanyNotFoundException("Company with the given id was not found"));

        User user = new User();
        user.setName(cashier.name());
        user.setEmail(cashier.email());
        user.setRole(cashier.role());
        user.setCompanyId(company);
        user.setPin(passwordEncoder.encode(cashier.pin()));

        log.info("Saving " + user + " to the database...");

        this.userRepository.save(user);

        return userRepository.findByCompanyId_Id(Long.valueOf(cashier.companyId()), pageable)
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()));
    }

    public UserModificationResponse deleteUser(Integer userId) {
        // თუ იუზერი არ არსებობს, exception
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Suggested user was not found"));

        // Owner-ის წაშლა არ შეიძლება
        if (user.getRole().equals("OWNER")) {
            throw new InvalidRoleException("Cannot delete the owner account");
        }

        this.userRepository.delete(user);

        log.info("User deleted: " + userId);
        return new UserModificationResponse(
                200,
                new UserDTO(
                        user.getId(), user.getName(),
                        user.getEmail(), user.getRole()
                )
        );
    }
}
