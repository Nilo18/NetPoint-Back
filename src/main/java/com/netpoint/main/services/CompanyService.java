package com.netpoint.main.services;

import com.netpoint.main.dto.CompanyDTO;
import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.dto.responses.CompanyUserPayload;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.models.Company;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserService userService;

    public CompanyDTO getCompanyInfo(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        return new CompanyDTO(
                company.getId(),
                company.getLogo(),
                company.getEmail(),
                company.getName(),
                company.getIndustry()
        );
    }

    public CompanyUserPayload getCompanyUserPayload(Integer companyId, Integer userId) {
        CompanyDTO company = getCompanyInfo(companyId);
        UserDTO user = userService.getUserInfo(userId);

        return new CompanyUserPayload(company, user);
    }
}
