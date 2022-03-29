package com.example.answerandquestion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.answerandquestion.R
import com.example.answerandquestion.databinding.DeleteLayoutBinding
import com.example.answerandquestion.databinding.ReceiveMsgBinding
import com.example.answerandquestion.databinding.SendMsgBinding
import com.example.answerandquestion.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter (
    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    lateinit var messages: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    var senderRoom: String
    var receiverRoom: String


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: Message = messages[position]
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE)
                viewHolder.binding.message.setVisibility(View.GONE)
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message

            viewHolder.itemView.setOnLongClickListener {
                val view: View =
                    LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                binding.everyone.setOnClickListener(View.OnClickListener {
                    message.message = "This message is removed."
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    dialog.dismiss()
                })
                binding.delete.setOnClickListener(View.OnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                })
                binding.cancel.setOnClickListener(View.OnClickListener { dialog.dismiss() })
                dialog.show()
                false
            }
        } else {
            val viewHolder = holder as ReceiverViewHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE)
                viewHolder.binding.message.setVisibility(View.GONE)
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.setText(message.message)
            viewHolder.itemView.setOnLongClickListener {
                val view: View =
                    LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.getRoot())
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed."
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener(View.OnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                })
                binding.cancel.setOnClickListener(View.OnClickListener { dialog.dismiss() })
                dialog.show()
                false

            }

        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding

        init {
            binding = SendMsgBinding.bind(itemView)
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMsgBinding

        init {
            binding = ReceiveMsgBinding.bind(itemView)
        }
    }

    init {
        if (messages != null) {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }
}