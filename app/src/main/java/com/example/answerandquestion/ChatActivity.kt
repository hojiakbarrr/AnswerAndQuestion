package com.example.answerandquestion

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.answerandquestion.adapter.MessageAdapter
import com.example.answerandquestion.databinding.ActivityChatBinding
import com.example.answerandquestion.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    var adapter: MessageAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        /**
         *
         */
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding.name.text = name
        Glide.with(this).load(profile).placeholder(R.drawable.placeholder)
            .into(binding.profile01)
        binding.imageView.setOnClickListener { finish() }
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue(String::class.java)
                    if (status == "offline") {
                        binding.status.visibility = View.GONE
                    } else {
                        binding.status.setText(status)
                        binding.status.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        /**
         *
         */
        adapter = MessageAdapter(this, messages, senderRoom!!, receiverRoom!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        /*
         */
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot in snapshot.children) {
                        val message: Message? = snapshot.getValue(Message::class.java)
                        message!!.messageId = snapshot.key
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding.send.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString().trim()
            val date = Date()
            val message = Message(messageTxt, senderUid, date.time)

            binding.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .child("message")
                .child(randomKey!!)
                .setValue(message)
                .addOnSuccessListener { }


        }
        binding.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)

            val handler = Handler()
            binding.messageBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    database!!.reference.child("Presence")
                        .child(senderUid!!)
                        .setValue("typing...")
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed(userStoppedTyping, 1000)
                }

                var userStoppedTyping = Runnable {
                    database!!.reference.child("Presence")
                        .child(senderUid!!)
                        .setValue("Online")
                }
            })
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
    }

    override fun onResume() {
        super.onResume()
        val currenrId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currenrId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currenrId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currenrId!!)
            .setValue("offline")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                if (data.data != null) {
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    var reference = storage?.reference?.child("chats")
                        ?.child(calendar.timeInMillis.toString() + "")
                    dialog?.show()
                    reference?.putFile(selectedImage!!)
                        ?.addOnCompleteListener { task ->
                            dialog?.dismiss()
                            if (task.isSuccessful) {
                                reference.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageTxt: String = binding.messageBox.text.toString()
                                    val date = Date()
                                    val message = Message(messageTxt, senderUid, date.time)
                                    message.message = "photo"
                                    message.imageUrl = filePath
                                    binding.messageBox.setText("")
                                    val randomKey = database!!.reference.push().key
                                    val lastMsObj = HashMap<String, Any>()
                                    lastMsObj["lastMsg"] = message.message!!
                                    lastMsObj["lastMsgTime"] = date.time
                                    database!!.reference.child("chats")
                                        .updateChildren(lastMsObj)
                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .updateChildren(lastMsObj)
                                    database!!.reference.child("chats")
                                        .child(senderRoom!!)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database!!.reference.child("chats")
                                                .child(receiverRoom!!)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener { }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }
}