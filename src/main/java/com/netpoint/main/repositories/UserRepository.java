package com.netpoint.main.repositories;

import com.netpoint.main.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Basic database operations are now available!
}