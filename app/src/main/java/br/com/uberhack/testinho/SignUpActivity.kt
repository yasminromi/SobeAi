package br.com.uberhack.testinho

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        button_sign_up.setOnClickListener { onSignUp() }
    }

    fun onSignUp() {

        if(!phone_edit.text.toString().equals("") && !name_edit.text.toString().equals("")) {
            SignUpApplication.setPhone(phone_edit.text.toString())
            SignUpApplication.setUserName(name_edit.text.toString())
        }

        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
        finish()
    }
}
