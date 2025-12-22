package com.example.veganism

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSent: TextView = view.findViewById(R.id.tvSent)
        val tvReceived: TextView = view.findViewById(R.id.tvReceived)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = messages[position]

        if (chat.isSentByUser) {
            // Show Sent bubble, hide Received bubble
            holder.tvSent.text = chat.message
            holder.tvSent.visibility = View.VISIBLE
            holder.tvReceived.visibility = View.GONE
        } else {
            // Show Received bubble, hide Sent bubble
            holder.tvReceived.text = chat.message
            holder.tvReceived.visibility = View.VISIBLE
            holder.tvSent.visibility = View.GONE
            holder.tvReceived.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        }
    }

    override fun getItemCount() = messages.size
}