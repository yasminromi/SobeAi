/*
 * Copyright (c) 2016 Uber Technologies, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package br.com.uberhack.testinho

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.uber.sdk.android.core.auth.AccessTokenManager
import com.uber.sdk.android.core.auth.AuthenticationError
import com.uber.sdk.android.core.auth.LoginCallback
import com.uber.sdk.android.core.auth.LoginManager
import com.uber.sdk.android.core.utils.Preconditions.checkNotNull
import com.uber.sdk.android.core.utils.Preconditions.checkState
import com.uber.sdk.core.auth.AccessToken
import com.uber.sdk.core.auth.AccessTokenStorage
import com.uber.sdk.core.auth.Scope
import com.uber.sdk.core.client.SessionConfiguration
import com.uber.sdk.rides.client.UberRidesApi
import com.uber.sdk.rides.client.error.ErrorParser
import com.uber.sdk.rides.client.model.UserProfile
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * Activity that demonstrates how to use a [LoginManager].
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var accessTokenStorage: AccessTokenStorage
    private lateinit var loginManager: LoginManager
    private lateinit var configuration: SessionConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        configuration = SessionConfiguration.Builder()
            .setClientId(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS))
            .build()

        validateConfiguration(configuration)

        accessTokenStorage = AccessTokenManager(this)

        //Create a button with a custom request code
        uber_button_white.setCallback(SampleLoginCallback())
            .setSessionConfiguration(configuration)

        //Create a button using a custom AccessTokenStorage
        //Custom Scopes are set using XML for this button as well in R.layout.activity_ride_request

        uber_button_black.setAccessTokenStorage(accessTokenStorage)
            .setCallback(SampleLoginCallback())
            .setSessionConfiguration(configuration).requestCode = LOGIN_BUTTON_CUSTOM_REQUEST_CODE

        //Use a custom button with an onClickListener to call the LoginManager directly
        loginManager = LoginManager(
            accessTokenStorage,
            SampleLoginCallback(),
            configuration,
            CUSTOM_BUTTON_REQUEST_CODE
        )

        custom_uber_button.setOnClickListener { loginManager.login(this@LoginActivity) }
    }

    override fun onResume() {
        super.onResume()
        if (loginManager.isAuthenticated) {
            loadProfileInfo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(
            LOG_TAG, String.format(
                "onActivityResult requestCode:[%s] resultCode [%s]",
                requestCode, resultCode
            )
        )

        //Allow each a chance to catch it.
        uber_button_white.onActivityResult(requestCode, resultCode, data)
        uber_button_black.onActivityResult(requestCode, resultCode, data)

        loginManager.onActivityResult(this, requestCode, resultCode, data)
    }

    private inner class SampleLoginCallback : LoginCallback {

        override fun onLoginCancel() {
            Toast.makeText(this@LoginActivity, R.string.user_cancels_message, Toast.LENGTH_LONG).show()
        }

        override fun onLoginError(error: AuthenticationError) {
            Toast.makeText(
                this@LoginActivity,
                getString(R.string.login_error_message, error.name), Toast.LENGTH_LONG
            ).show()
        }

        override fun onLoginSuccess(accessToken: AccessToken) {
            loadProfileInfo()
        }

        override fun onAuthorizationCodeReceived(authorizationCode: String) {
            Toast.makeText(
                this@LoginActivity, getString(R.string.authorization_code_message, authorizationCode),
                Toast.LENGTH_LONG
            )   .show()
        }
    }

    private fun loadProfileInfo() {
        val session = loginManager.session
        val service = UberRidesApi.with(session).build().createService()

        service.userProfile
            .enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@LoginActivity,
                            getString(R.string.greeting, response.body().firstName),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val error = ErrorParser.parseError(response)
                        Toast.makeText(this@LoginActivity, error!!.clientErrors[0].title, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {

                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        accessTokenStorage = AccessTokenManager(this)


        if (id == R.id.action_clear) {
            accessTokenStorage.removeAccessToken()
            Toast.makeText(this, "AccessToken cleared", Toast.LENGTH_SHORT).show()
            return true
        } else if (id == R.id.action_copy) {
            val accessToken = accessTokenStorage.accessToken

            val message = if (accessToken == null) "No AccessToken stored" else "AccessToken copied to clipboard"
            if (accessToken != null) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("UberSampleAccessToken", accessToken.token)
                clipboard.primaryClip = clip
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * Validates the local variables needed by the Uber SDK used in the sample project
     *
     * @param configuration
     */
    private fun validateConfiguration(configuration: SessionConfiguration) {
        val nullError = "%s must not be null"
        val sampleError = "Please update your %s in the gradle.properties of the project before " +
                "using the Uber SDK Sample app. For a more secure storage location, " +
                "please investigate storing in your user home gradle.properties "

        checkNotNull(configuration, String.format(nullError, "SessionConfiguration"))
        checkNotNull(configuration.clientId, String.format(nullError, "Client ID"))
        checkNotNull(configuration.redirectUri, String.format(nullError, "Redirect URI"))
        checkState(
            configuration.clientId != "insert_your_client_id_here",
            String.format(sampleError, "Client ID")
        )
        checkState(
            configuration.redirectUri != "insert_your_redirect_uri_here",
            String.format(sampleError, "Redirect URI")
        )
    }

    companion object {
        //Please update CLIENT_ID and REDIRECT_URI below with your app's values.

        const val CLIENT_ID = BuildConfig.CLIENT_ID
        const val REDIRECT_URI = BuildConfig.REDIRECT_URI

        private const val LOG_TAG = "LoginActivity"

        private const val LOGIN_BUTTON_CUSTOM_REQUEST_CODE = 1112
        private const val CUSTOM_BUTTON_REQUEST_CODE = 1113
    }
}
