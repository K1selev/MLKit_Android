package com.example.mlkitconversation

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.TextMessage
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var conversationView: TextView
    private lateinit var txtInput: EditText
    private lateinit var btnGenerateReply: Button
    private lateinit var btnChangeConversation: Button

    private var outputText = ""
    private var conversation: ArrayList<TextMessage> = ArrayList()

    private val conversations = listOf(
        listOf(
            "Hi, good morning!" to null,
            "Oh, hey -- how are you?" to "Nizhoni",
            "Just got up, thinking of heading out for breakfast" to null,
            "Want to meet up?" to "Nizhoni",
            "Sure, what do you fancy?" to null,
            "Just coffee, or do you want to eat?" to "Nizhoni"
        ),
        listOf(
            "Hey, did you watch the game last night?" to null,
            "Yeah, it was amazing! That last goal was insane!" to "Alex",
            "Totally! I couldn't believe it went in!" to null,
            "We should watch the next match together." to "Alex",
            "Sounds great! Let's do it." to null
        ),
        listOf(
            "Hey, what are you up to?" to null,
            "Just working on my project. You?" to "Sam",
            "Trying to figure out what to eat for lunch." to null,
            "How about sushi?" to "Sam"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        conversationView = findViewById(R.id.conversationView)
        txtInput = findViewById(R.id.txtInput)
        btnGenerateReply = findViewById(R.id.btnGenerateReply)
        btnChangeConversation = findViewById(R.id.btnChangeConversation)

        loadRandomConversation()

        btnGenerateReply.setOnClickListener {
            generateSmartReply()
        }

        btnChangeConversation.setOnClickListener {
            loadRandomConversation()
        }
    }

    private fun loadRandomConversation() {
        val randomIndex = Random.nextInt(conversations.size)
        initializeConversation(conversations[randomIndex])
    }

    private fun initializeConversation(dialog: List<Pair<String, String?>>) {
        outputText = ""
        conversation.clear()

        for ((message, sender) in dialog) {
            if (sender == null) {
                addConversationItem(message)
            } else {
                addConversationItem(message, sender)
            }
        }

        conversationView.text = outputText
    }

    private fun addConversationItem(item: String) {
        outputText += "Me: $item\n"
        conversation.add(TextMessage.createForLocalUser(item, System.currentTimeMillis()))
    }

    private fun addConversationItem(item: String, who: String) {
        outputText += "$who: $item\n"
        conversation.add(TextMessage.createForRemoteUser(item, System.currentTimeMillis(), who))
    }

    private fun generateSmartReply() {
        val smartReplyGenerator = SmartReply.getClient()

        smartReplyGenerator.suggestReplies(conversation)
            .addOnSuccessListener { result ->
                if (result.suggestions.isNotEmpty()) {
                    val reply = result.suggestions[0].text
                    txtInput.setText(reply)
                } else {
                    txtInput.setText("No suggestions available")
                }
            }
            .addOnFailureListener { e ->
                txtInput.setText("Error generating reply: ${e.localizedMessage}")
            }
    }
}