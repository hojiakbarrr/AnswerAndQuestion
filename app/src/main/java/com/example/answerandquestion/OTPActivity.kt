package com.example.answerandquestion

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.example.answerandquestion.databinding.ActivityOTPActivityBinding
import com.example.answerandquestion.databinding.ActivityVerificationBinding
import com.example.answerandquestion.utils.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private val binding: ActivityOTPActivityBinding by lazy {
        ActivityOTPActivityBinding.inflate(layoutInflater)
    }
    var auth: FirebaseAuth? = null
    var verificationId: String? = null
    var dialog: ProgressDialog? = null

    /*now add firebase*/

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Sending OTP...")
        dialog!!.setCancelable(false)
        dialog!!.show()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        val phoneNumber = intent.getStringExtra("phoneNumber")
        Log.d("Error", "$phoneNumber")
        binding.phoneLble.text = "Verify $phoneNumber"
        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Log.d("Error", "$p0")
                    toast(p0.toString())
                }

                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken,
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog!!.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding.otpView.requestFocus()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        binding.otpView.setOtpCompletionListener { otp ->
            val credental = PhoneAuthProvider.getCredential(verificationId!!, otp)
            auth!!.signInWithCredential(credental)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toast("Вы успешно перешли в Экран Настройки профиля")
                        val intent = Intent(this@OTPActivity, SetUpProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        toast("Failed in OTPactivity")
                        Log.d("Error", "${task.result}")
                    }
                }
        }
    }
}