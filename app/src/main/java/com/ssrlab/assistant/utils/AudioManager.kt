package com.ssrlab.assistant.utils

import com.ssrlab.assistant.utils.vm.ChatViewModel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

class AudioManager(viewModel: ChatViewModel) {

    private lateinit var audioManager: AudioManager

    val volumeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {

                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                viewModel.setVolumeAvailability(currentVolume != 0)
            }
        }
    }

    fun controlVolume(value: Int, context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
    }

    fun setUpVolumeStateListener(viewModel: ChatViewModel, activity: Activity) {
        audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        viewModel.setVolumeAvailability(currentVolume != 0)
    }
}
