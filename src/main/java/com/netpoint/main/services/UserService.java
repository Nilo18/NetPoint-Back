package com.netpoint.main.services;

import com.netpoint.main.dto.UserDTO;
import com.netpoint.main.exceptions.UserNotFoundException;
import com.netpoint.main.models.User;
import com.netpoint.main.repositories.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDTO getUserInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getProfileImage());
    }
}
