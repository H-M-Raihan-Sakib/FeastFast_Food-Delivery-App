package com.example.feastfast.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.feastfast.databinding.ItemMessageReceivedBinding
import com.example.feastfast.databinding.ItemMessageSentBinding
import com.example.feastfast.model.ChatMessage

class MessageAdapter(private val messages: MutableList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    // This function is the key: it tells the adapter which layout to use.
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.sentByAdmin) {
            VIEW_TYPE_RECEIVED // Admin message
        } else {
            VIEW_TYPE_SENT     // User message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.itemViewType == VIEW_TYPE_SENT) {
            (holder as SentViewHolder).bind(message)
        } else {
            (holder as ReceivedViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class SentViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.sentMessageText.text = message.message
        }
    }

    class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.receivedMessageText.text = message.message
            binding.senderNameText.text = "Support"
        }
    }
}
