package com.ssrlab.assistant.rv

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.ssrlab.assistant.R
import com.ssrlab.assistant.db.*
import com.ssrlab.assistant.ui.chat.MainActivity
import com.ssrlab.assistant.utils.helpers.MediaPlayer.initializeMediaPlayer
import com.ssrlab.assistant.utils.helpers.MediaPlayer.pauseAudio
import com.ssrlab.assistant.utils.helpers.MediaPlayer.playAudio

@Suppress("KotlinConstantConditions")
class ChatAdapter(
    private val messageI: ArrayList<MessageInfoObject>,
    private val botMessage: ArrayList<BotMessage>,
    private val userMessage: ArrayList<UserMessage>,
    private val userVoiceMessage: ArrayList<UserVoiceMessage>,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val arrayOfButtons = ArrayList<ImageButton>()
    private val playingArray = ArrayList<Boolean>()

    inner class ChatViewHolder(item: View) : RecyclerView.ViewHolder(item)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        var messageView: View? = null
        when (viewType) {
            1 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_bot, parent, false)
            2 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user, parent, false)
            3 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_message_user_voice, parent, false)
            0 -> messageView = LayoutInflater.from(parent.context).inflate(R.layout.rv_empty_view, parent, false)
        }

        return ChatViewHolder(messageView!!)
    }

    override fun getItemCount(): Int = messageI.size + 3

    @Suppress("ControlFlowWithEmptyBody")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val view = holder.itemView

        if (position < messageI.size) {
            when (messageI[position].role) {
                1 -> {
                    val textMsg = view.findViewById<TextView>(R.id.rv_bot_msg)
                    val share = view.findViewById<ImageButton>(R.id.rv_bot_share)
                    val clipboard = view.findViewById<ImageButton>(R.id.rv_bot_clipboard)

                    textMsg.text = botMessage.find { it.id == messageI[position].id }?.text
                    share.setOnClickListener { Log.d("share", "${messageI[position].id} share") }
                    clipboard.setOnClickListener { Log.d("clipboard", "${messageI[position].id} clipboard") }
                }
                2 -> {
                    val textMsg = view.findViewById<TextView>(R.id.rv_user_msg)

                    textMsg.text = userMessage.find { it.id == messageI[position].id }?.text
                }
                3 -> {
                    val playButton = view.findViewById<ImageButton>(R.id.rv_user_voice_button)
                    val duration = view.findViewById<TextView>(R.id.rv_user_voice_duration)
                    val audio = userVoiceMessage.find { it.id == messageI[position].id }?.audio?.toUri()!!

                    arrayOfButtons.add(playButton)

                    initializeMediaPlayer(mainActivity, audio, duration)

                    playButton.setOnClickListener {
                        pauseAudio()
                        initializeMediaPlayer(mainActivity, audio, duration)
                        playAudio()
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < messageI.size) {
            when (messageI[position].role) {
                1 -> 1
                2 -> 2
                3 -> 3
                else -> 0
            }
        } else 0
    }
}