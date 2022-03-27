package com.example.answerandquestion

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.answerandquestion.databinding.ActivityOTPActivityBinding
import com.example.answerandquestion.databinding.ActivitySetUpProfileBinding
import com.example.answerandquestion.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class SetUpProfileActivity : AppCompatActivity() {

    private val binding: ActivitySetUpProfileBinding by lazy {
        ActivitySetUpProfileBinding.inflate(layoutInflater)
    }
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        dialog?.setMessage("Updating Profile")
        dialog?.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()


        binding.imageView.setOnClickListener {
            upLoadImageProfile()
        }
        binding.continueBtn02.setOnClickListener {
            val name: String = binding.nameBox.text.toString()
            if (name.isEmpty()) {
                binding.nameBox.setError("Please type a name")
            }
            dialog?.show()
            if (selectedImage != null) {
                val reference = storage!!.reference.child("Profile")
                    .child(auth!!.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val imageUrl = uri.toString()
                            val uid = auth!!.uid
                            val phone = auth!!.currentUser!!.phoneNumber
                            val name: String = binding.nameBox.text.toString()
                            val user = User(uid, name, phone, imageUrl)
                            database!!.reference.child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener {
                                    dialog?.dismiss()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                        }
                    } else {
                        val uid = auth!!.uid
                        val phone = auth!!.currentUser?.phoneNumber
                        val name: String = binding.nameBox.text.toString()
                        val user = User(uid, name, phone, "No Image")
                        database?.reference
                            ?.child("users")
                            ?.child(uid!!)
                            ?.setValue(user)
                            ?.addOnCanceledListener {
                                dialog?.dismiss()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }
        }
    }

    private fun upLoadImageProfile() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 45)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null){
            if (data.data != null){
                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference
                    .child("Profiles")
                    .child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener{ task->
                    if(task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener {uri->
                            val filePath = uri.toString()
                            val obj = HashMap<String, Any>()
                            obj["profileImage"] = filePath
                            FirebaseAuth.getInstance().uid?.let {
                                database?.reference?.child("users")?.child(it)
                                    ?.updateChildren(obj)?.addOnCompleteListener {  }
                            }
                        }
                    }
                    binding.imageView.setImageURI(data.data)
                    selectedImage = data.data

                }
            }
        }
    }
}