package com.example.myapp4

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    val LOAD_IMAGE = 101
    lateinit var imageURI : Uri
    var content : String? = null

    val websiteUrl = "https://apad-210618.appspot.com"

    val retrofitApi = RetrofitInstance().getRetrofitInstance(websiteUrl)
            .create(HttpApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectImage_button.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), LOAD_IMAGE)
        }

        submit_button.setOnClickListener {
            content = content_editText.text.toString()

            val inputStream = contentResolver.openInputStream(imageURI)

            val imageFile = File(imageURI.path)
            val imageRequestBody : RequestBody = RequestBody.create(MediaType.parse("image/*"), inputStream.readBytes(1024))
            val imagePart : MultipartBody.Part = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
            val contentRequestBody : RequestBody = RequestBody.create(MediaType.parse("text/plain"), content)

            val req = retrofitApi.getSignUrl()
            req.enqueue(object : Callback<GetSignUrlResponse> {
                override fun onFailure(call: Call<GetSignUrlResponse>?, t: Throwable?) {
                    Log.d("MainActivity", "Failure HTTP Request")
                }

                override fun onResponse(call: Call<GetSignUrlResponse>?, response: Response<GetSignUrlResponse>?) {
                    val uploadUrl = response?.body()?.uploadUrl?.replace(String.format("%s/", websiteUrl), "")
                    Log.d(TAG, String.format("uploadUrl: %s", uploadUrl))
                    val req = retrofitApi.signGuestbook(uploadUrl, contentRequestBody, imagePart)
                    req.enqueue(object : Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                            Log.d("MainActivity", "Failure HTTP Request")
                        }

                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                            Log.d("MainActivity", "Success HTTP Request")
                        }
                    })
                }

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            imageURI = data?.data!!
        }
    }
}
