package com.technet7.microsvc.email.client;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface EmailService {
    @POST("emails/register")
    Call<EmailResponse> registerEmail(@Body EmailRequest request);
}