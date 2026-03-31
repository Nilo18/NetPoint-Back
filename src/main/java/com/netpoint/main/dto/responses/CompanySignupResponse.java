package com.netpoint.main.dto.responses;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// ვახომ რომ გვითხრა მაშინ რომ ბაზის მოდელის ლოგიკა ფრონტში რასაც აგზავნით მაგისგან გამოყავითო, მაგისთვისაა საჭირო
// ამ შემთხვევაში პაროლი დაიმალება
public record CompanySignupResponse(Integer id, @NotBlank String name,
                                    @NotBlank @Email String email, @NotBlank String industry) {
}
