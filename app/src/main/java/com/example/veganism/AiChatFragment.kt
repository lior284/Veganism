package com.example.veganism

import android.os.Bundle
import android.text.Spanned
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.veganism.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AiChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AiChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyC3vpC1ctDtAcS3DLSTz3LgXJfVz_wJbR8"
    )

    private lateinit var rvChatHistory: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: ImageView

    // 1. The Data Source
    private val messageList = ArrayList<ChatMessage>()
    // 2. The Adapter
    private lateinit var chatAdapter: ChatAdapter

    // This starts a 'chat session' that tracks history automatically
    private val chat = model.startChat(history = listOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ai_chat, container, false)

        rvChatHistory = view.findViewById(R.id.rvChatHistory)
        etMessageInput = view.findViewById(R.id.etMessageInput)
        btnSend = view.findViewById(R.id.ivSend)

        setupRecyclerView()
        setupListeners()

        return view
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        rvChatHistory.adapter = chatAdapter
        rvChatHistory.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true // Starts list from bottom like a real chat
        }
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            val userText = etMessageInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                // 1. Add User Message
                addMessageToChat(userText, true)

                // 2. Clear input
                etMessageInput.text.clear()

                // 3. Generate Computer Response (Simulated)
                generateComputerResponse(userText)
            }
        }
    }

    private fun addMessageToChat(message: CharSequence, isUser: Boolean) {
        messageList.add(ChatMessage(message, isUser))
        // Notify adapter regarding the new item
        chatAdapter.notifyItemInserted(messageList.size - 1)
        // Scroll to the bottom
        rvChatHistory.scrollToPosition(messageList.size - 1)
    }

    private fun generateComputerResponse(userInput: String) {
        lifecycleScope.launch {
            try {
                // sendMessage sends the text AND the previous history
                val response = chat.sendMessage(userInput)
                val rawText = response.text ?: "No response from model."

                val formatedText = formatMarkdown(rawText)

                // Add the AI's response to the UI
                addMessageToChat(formatedText, false)
            } catch (e: Exception) {
                addMessageToChat("Error: ${e.message}", false)
            }
        }
    }

    private fun formatMarkdown(text: String): Spanned {
        var processedText = text
            // 1. Handle Markdown Links: [Title](URL) -> <a href="URL">Title</a>
            .replace(Regex("\\[(.*?)\\]\\((.*?)\\)"), "<a href=\"$2\">$1</a>")

            // 2. Handle Bold: **text** -> <b>text</b>
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")

            // 3. Handle Italics: *text* -> <i>text</i>
            .replace(Regex("(?<!\\*)\\*(?!\\*)(.*?)\\*"), "<i>$1</i>")

            // 4. Handle Bullet Points
            .replace(Regex("(?m)^[\\*\\-]\\s+"), "•&nbsp;")

            // 5. Handle Newlines
            .replace("\n", "<br>")

        Log.d("AI Chat Check", "Processed Text: $processedText")

        return HtmlCompat.fromHtml(processedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AIChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AiChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}