package com.example.answerandquestion.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toast(message: String){
    Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT).show()
}


fun Context.toast(message: String){
    Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
}