package com.example.feastfast

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.feastfast.adapter.MessageAdapter
import com.example.feastfast.databinding.ActivityChatBinding
import com.example.feastfast.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var chatRef: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var isListenerAttached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: return
        chatRef = database.reference.child("chats").child(userId)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        val layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.chatRecyclerView.layoutManager = layoutManager
        messageAdapter = MessageAdapter(messages)
        binding.chatRecyclerView.adapter = messageAdapter

        messageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                binding.chatRecyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
            }
        })

        // Setup Send Button
        binding.sendMessageButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        // --- 2. ADD CLICK LISTENERS FOR SUGGESTIONS ---
        binding.suggestion1.setOnClickListener { sendMessage(binding.suggestion1.text.toString()) }
        binding.suggestion2.setOnClickListener { sendMessage(binding.suggestion2.text.toString()) }

        listenForMessages()
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // --- 3. HIDE SUGGESTIONS WHEN A MESSAGE IS SENT ---
        binding.suggestionLayout.visibility = View.GONE

        val userRef = database.reference.child("users").child(userId).child("name")
        userRef.get().addOnSuccessListener { dataSnapshot ->
            val senderName = dataSnapshot.getValue(String::class.java) ?: currentUser.displayName ?: "User"
            val timestamp = System.currentTimeMillis()
            val message = ChatMessage(
                senderId = userId,
                senderName = senderName,
                message = messageText,
                timestamp = timestamp,
                sentByAdmin = false
            )
            chatRef.push().setValue(message).addOnSuccessListener {
                binding.messageEditText.text.clear()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listenForMessages() {
        if (isListenerAttached) return
        isListenerAttached = true

        // --- 4. SHOW/HIDE SUGGESTIONS BASED ON CHAT HISTORY ---
        // Use ValueEventListener once to check if history is empty
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    binding.suggestionLayout.visibility = View.GONE
                } else {
                    binding.suggestionLayout.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Use ChildEventListener to listen for new messages
        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)
                message?.let {
                    // Hide suggestions as soon as the first message loads
                    binding.suggestionLayout.visibility = View.GONE
                    messageAdapter.addMessage(it)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to load messages.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
