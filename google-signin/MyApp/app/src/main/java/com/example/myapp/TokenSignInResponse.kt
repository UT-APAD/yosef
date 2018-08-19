package com.example.myapp

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TokenSignInResponse {
    @SerializedName("username")
    @Expose
    val username : String? = null

    @SerializedName("userid")
    @Expose
    val userid : String? = null
}