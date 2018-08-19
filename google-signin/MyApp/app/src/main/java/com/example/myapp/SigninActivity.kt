package com.example.myapp

import android.accounts.Account
import android.content.Intent
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_signin.*


class SigninActivity : AppCompatActivity(), TokenSignInAsyncTaskInterface {

    private val TAG: String = "SigninActivity"
    private val RC_SIGN_IN: Int = 101

    lateinit var mGoogleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //val sign_in_button = findViewById<SignInButton>(R.id.sign_in_button)
        sign_in_button.setOnClickListener{
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val signInButtonChildCount = sign_in_button.childCount
        for (i in 0..signInButtonChildCount) {
            if (sign_in_button.getChildAt(i) is TextView) {
                (sign_in_button.getChildAt(i) as TextView).text = getString(R.string.signinbutton_textView)
            }
        }

        //val sign_out_button = findViewById<Button>(R.id.sign_out_button)
        sign_out_button.setOnClickListener {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this) {
                        updateUI(GoogleSignIn.getLastSignedInAccount(this))
                    }
            mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener(this) {
                        updateUI(GoogleSignIn.getLastSignedInAccount(this))
                    }
        }

        //val imageView = findViewById<ImageView>(R.id.imageView)
        Picasso.get()
                .load("http://i.imgur.com/DvpvklR.png")
                .into(imageView)
    }

    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account)
    }

    fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            sign_in_button.visibility = View.GONE

            name_textView.visibility = View.VISIBLE
            name_textView.text = account.displayName

            email_textView.visibility = View.VISIBLE
            email_textView.text = account.email

            sign_out_button.visibility = View.VISIBLE
        }
        else {
            sign_in_button.visibility = View.VISIBLE

            name_textView.visibility = View.GONE
            name_textView.text = ""

            email_textView.visibility = View.GONE
            email_textView.text = ""

            sign_out_button.visibility = View.GONE
        }
    }

    fun updateWebuser(result : Map<String, String?>) {
        val username = result.get("username")
        val userid = result.get("userid")

        if (username == null) {
            webuser_textView.visibility = View.GONE
        }
        else {
            webuser_textView.visibility = View.VISIBLE
        }

        webuser_textView.text = String.format("signed in as %s\n with id %s", username, userid)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken

            TokenSignInAsyncTask(this).execute(account)
            updateUI(account)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    override fun onComplete(result : Map<String, String?>) {
        updateWebuser(result)
    }

    class TokenSignInAsyncTask (val _callback : TokenSignInAsyncTaskInterface) : AsyncTask<GoogleSignInAccount, Void, Map<String, String?>>() {
        val TAG = "TokenSignInAsyncTask"
        val callback = _callback

        override fun doInBackground(vararg p0: GoogleSignInAccount?): Map<String, String?> {
            val httpService = RetrofitClient().getRetrofitInstance("https://apad-210618.appspot.com");
            val api = httpService.create(HttpService::class.java)

            val account = p0[0]

            val response = api.tokensignin(account?.email, account?.idToken).execute()
            val responseBody = response.body()

            val result = mapOf("username" to responseBody?.username, "userid" to responseBody?.userid)

            return result
        }

        override fun onPostExecute(result: Map<String, String?>) {
            callback.onComplete(result)
        }
    }
}

interface TokenSignInAsyncTaskInterface {
    fun onComplete(result: Map<String, String?>)
}
