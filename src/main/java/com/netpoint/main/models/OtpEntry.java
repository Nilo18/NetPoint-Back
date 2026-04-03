package com.netpoint.main.models;

import java.time.Instant;

//ეს თითოეული კონკრეტულ 2FA მცდელობისთვისაა
// იკავებს OTP დეტალებს ადმინისთვის ან ოუნერისთვის ვინც პაროლი და ლოგინი სწორედ გაიარა, მარა 2ფა არ გაუკეთებია ჯერ


public class OtpEntry {
    private final String userId;    // იმ მომხმარებლის ID ვისაც 2FA უნდა
    private final String phoneNumber; // ის ნომერია სადაც ოტპ იგზავნება
    private final String otpCode; //თვითონ ოტპ კოდი
    private final Instant expiresAt; //დრო რის მერეც უკვე ვადა გასდის
    private int attempts; //ითვლის ჩფლავებულ მცდელობების რაოდენობას

    public OtpEntry(String userId, String phoneNumber, String otpCode) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.otpCode = otpCode;
        //ეს პროსტა ისაა, რომ 5 წუთში გასდის ვადა იმ ოტპ კოდს
        this.expiresAt = Instant.now().plusSeconds(300);
        this.attempts = 0;
    }
    // თრუს აბრუნებს თ ოტპ სწროად გაიარა
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
    public String getUserId() { return userId; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getOtpCode() { return otpCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public int getAttempts() { return attempts; }
    //ყველა ჩაფლავებულზე ეს ითვლება
    public void incrementAttempts() { this.attempts++; }
}