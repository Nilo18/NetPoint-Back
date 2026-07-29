package com.netpoint.main.repositories;

import com.netpoint.main.models.Company;
import com.netpoint.main.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByName(String name);
    Optional<User> findByNameOrEmail(String name, String email);
    // In UserRepository:
    Page<User> findByCompany_Id(Integer id, Pageable pageable);
    Optional<User> findByIdAndCompany_Id(Integer id, Integer companyId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndCompany_IdAndRole(String email, Integer companyId, String role);
    boolean existsByEmailAndCompany_Id(String email, Integer companyId);
    User findByCompany(Company company);
    @Query("select u from User u where u.company.id = :companyId and (lower(u.name) like lower(concat('%', :searchTerm, '%')) or lower(u.email) like lower(concat('%', :searchTerm, '%')))")
    List<User> searchByNameOrEmailWithinCompany(@Param("searchTerm") String searchTerm, @Param("companyId") Integer companyId);
    void deleteByCompany_Id(Integer companyId);
    long countByCompany_Id(Integer companyId);
    long countByCompany_IdAndRoleIgnoreCase(Integer companyId, String role);
    Optional<User> findByIdAndCompanyId(Integer id, Integer companyId);
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND u.role = 'OWNER'")
    Optional<User> findCompanyOwner(@Param("companyId") Integer companyId);
    Optional<User> findByRoleAndCompany_Id(String role, Integer companyId);
}
