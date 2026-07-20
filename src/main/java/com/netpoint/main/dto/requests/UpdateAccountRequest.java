package com.netpoint.main.dto.requests;



import com.netpoint.main.dto.UpdateUserInfoDTO;
import com.netpoint.main.dto.UserDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    @Valid
    UpdateUserInfoDTO newInfo;
//    @Size(min = 8, message = "Password must be at least 8 characters")
//    private String newPassword;
    @Valid VerifyOtpRequest verificationInfo;
    private boolean removeImage;
//    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
//    private String name;
//
//    @Email(message = "Invalid email format")
//    private String email;
//
//    @Size(min = 6, message = "Password must be at least 6 characters")
//    private String password;
}