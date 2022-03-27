package com.example.answerandquestion

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.answerandquestion.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : AppCompatActivity() {

    private val binding: ActivityVerificationBinding by lazy {
        ActivityVerificationBinding.inflate(layoutInflater)
    }
    var auth : FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        supportActionBar?.hide()
        binding.editNumber.requestFocus()
        binding.continueBtn.setOnClickListener {
            val intent = Intent(this, OTPActivity::class.java)
            intent.putExtra("phoneNumber", binding.editNumber.text.toString())
            startActivity(intent)

        }



    }

}