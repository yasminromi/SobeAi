package br.com.uberhack.testinho

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        button_sign_up.setOnClickListener { onSignUp() }
    }

    fun onSignUp() {

        if (!phone_edit.text.toString().equals("") && !name_edit.text.toString().equals("")) {
            SignUpApplication.setPhone(phone_edit.text.toString())
            SignUpApplication.setUserName(name_edit.text.toString())
        }

        startActivity(Intent(this@SignUpActivity, MainMapsActivity::class.java))
        finish()
    }
}
