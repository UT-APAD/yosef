package com.example.myapp

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface HttpService {
    @POST("/tokensignin")
    fun tokensignin(@Query("username") username : String?, @Query("token") token : String?) : Call<TokenSignInResponse>
}