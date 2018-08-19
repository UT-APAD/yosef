package com.example.myapp

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TokenSignInRequest {
    @SerializedName("token")
    @Expose
    var token : String? = null
}