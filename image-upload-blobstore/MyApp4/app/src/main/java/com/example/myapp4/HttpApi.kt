package com.example.myapp4

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface HttpApi {
    @GET("/getsignurl")
    fun getSignUrl() : Call<GetSignUrlResponse>

    @Multipart
    @POST
    fun signGuestbook(@Url uploadUrl : String?, @Part("content") content : RequestBody, @Part image : MultipartBody.Part) : Call<ResponseBody>
}

class GetSignUrlResponse {
    @SerializedName("upload_url")
    val uploadUrl : String = ""
}
