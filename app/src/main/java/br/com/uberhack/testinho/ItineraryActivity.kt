package br.com.uberhack.testinho

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_itinerary.*

class ItineraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerary)
        closeButton.setOnClickListener(::goBack)
        forwardButton.setOnClickListener(::goBack)
    }

    fun goBack(v: View) {
        setResult(MapsActivity.RESULT_RETURN)
        finish()
    }
}
