package com.netpoint.main.dto.responses;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// ვახომ რომ გვითხრა მაშინ რომ ბაზის მოდელის ლოგიკა ფრონტში რასაც აგზავნით მაგისგან გამოყავითო, მაგისთვისაა საჭირო
// ამ შემთხვევაში პაროლი დაიმალება
public record CompanySignupResponse(
      @NotBlank Integer status,
      @NotBlank String access_token
) {}
