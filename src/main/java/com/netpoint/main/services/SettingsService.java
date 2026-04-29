package com.netpoint.main.services;

import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Data
public class SettingsService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public Page<UserDTO> fetchCompanyUsers(Long id, Pageable pageable) {
        if (!companyRepository.existsById(id)) {
            throw new CompanyNotFoundException("Couldn't find company by id");
        }

        return userRepository.findByCompanyId_Id(id, pageable)
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()));
    }
}
