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
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uber.sdk.android.core.Deeplink
import com.uber.sdk.android.core.auth.AccessTokenManager
import com.uber.sdk.android.core.utils.Preconditions.checkNotNull
import com.uber.sdk.android.core.utils.Preconditions.checkState
import com.uber.sdk.android.rides.RideParameters
import com.uber.sdk.android.rides.RideRequestButton
import com.uber.sdk.android.rides.RideRequestButtonCallback
import com.uber.sdk.core.client.ServerTokenSession
import com.uber.sdk.core.client.SessionConfiguration
import com.uber.sdk.rides.client.error.ApiError
import kotlinx.android.synthetic.main.activity_ride_request.*

/**
 * Activity that demonstrates how to use a [RideRequestButton].
 */
class RideRequestActivity : AppCompatActivity(), RideRequestButtonCallback {
    private lateinit var configuration: SessionConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_request)

        configuration = SessionConfiguration.Builder()
            .setRedirectUri(REDIRECT_URI)
            .setClientId(CLIENT_ID)
            .setServerToken(SERVER_TOKEN)
            .build()

        validateConfiguration(configuration)
        val session = ServerTokenSession(configuration)

        val rideParametersForProduct = RideParameters.Builder()
            .setProductId(UBERX_PRODUCT_ID)
            .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
            .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
            .build()

        // This button demonstrates deep-linking to the Uber app (default button behavior).

        uber_button_black.setRideParameters(rideParametersForProduct)
        uber_button_black.setSession(session)
        uber_button_black.setCallback(this)
        uber_button_black.loadRideInformation()

        val rideParametersCheapestProduct = RideParameters.Builder()
            .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
            .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
            .build()

        uber_button_white.setRideParameters(rideParametersForProduct)
        uber_button_white.setSession(session)
        uber_button_white.setDeeplinkFallback(Deeplink.Fallback.MOBILE_WEB)
        uber_button_white.loadRideInformation()
    }

    override fun onRideInformationLoaded() {
        Toast.makeText(this, "Estimates have been refreshed", Toast.LENGTH_LONG).show()
    }

    override fun onError(apiError: ApiError) {
        Toast.makeText(this, apiError.clientErrors[0].title, Toast.LENGTH_LONG).show()
    }

    override fun onError(throwable: Throwable) {
        Log.e("RideRequestActivity", "Error obtaining Metadata", throwable)
        Toast.makeText(this, "Connection error", Toast.LENGTH_LONG).show()
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

        val accessTokenStorage = AccessTokenManager(this)


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
        } else if (id == R.id.action_refresh_meta_data) {
            uber_button_black.loadRideInformation()
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
        checkNotNull(configuration.serverToken, String.format(nullError, "Server Token"))
        checkState(
            configuration.clientId != "insert_your_client_id_here",
            String.format(sampleError, "Client ID")
        )
        checkState(
            configuration.redirectUri != "insert_your_redirect_uri_here",
            String.format(sampleError, "Redirect URI")
        )
        checkState(
            configuration.redirectUri != "insert_your_server_token_here",
            String.format(sampleError, "Server Token")
        )
    }

    companion object {

        private val DROPOFF_ADDR = "One Embarcadero Center, San Francisco"
        private val DROPOFF_LAT = 37.795079
        private val DROPOFF_LONG = -122.397805
        private val DROPOFF_NICK = "Embarcadero"
        private val ERROR_LOG_TAG = "UberSDK-RideRequestActivity"
        private val PICKUP_ADDR = "1455 Market Street, San Francisco"
        private val PICKUP_LAT = 37.775304
        private val PICKUP_LONG = -122.417522
        private val PICKUP_NICK = "Uber HQ"
        private val UBERX_PRODUCT_ID = "a1111c8c-c720-46c3-8534-2fcdd730040d"
        private val WIDGET_REQUEST_CODE = 1234

        private val CLIENT_ID = BuildConfig.CLIENT_ID
        private val REDIRECT_URI = BuildConfig.REDIRECT_URI
        private val SERVER_TOKEN = BuildConfig.SERVER_TOKEN
    }
}
