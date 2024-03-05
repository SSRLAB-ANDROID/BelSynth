package com.ssrlab.assistant.client.chat

import android.content.Context
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.R
import com.ssrlab.assistant.db.objects.messages.Message
import com.ssrlab.assistant.utils.REQUEST_TIME_OUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ChatMessagesClient(private val context: Context) {

    private var chatClient = OkHttpClient.Builder()
        .connectTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .writeTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(REQUEST_TIME_OUT, TimeUnit.SECONDS)
        .build()

    private val fireAuth = FirebaseAuth.getInstance()

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun loadMessages(chatId: String, onSuccess: (ArrayList<Message>) -> Unit, onFailure: (String) -> Unit) {
        checkUid({ uid ->
            val request = Request.Builder()
                .url("https://ml1.ssrlab.by/chat-api/message/$chatId")
                .addHeader("x-user-id", uid)
                .build()

            chatClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()

                    try {
                        val jArray = JSONArray(responseBody)
                        if (jArray.length() != 0) {
                            val messageArray = ArrayList<Message>()

                            for (i in 0 until jArray.length()) {
                                val jObject = jArray.getJSONObject(i)
                                val userAudioLink = jObject.getString("message_voice_url") ?: "null"
                                val botAudioLink = jObject.getString("response_voice_url") ?: "null"

                                val requestMessage = Message(
                                    jObject.getString("message_text"),
                                    "user",
                                    userAudioLink
                                )

                                messageArray.add(requestMessage)

                                val responseMessage = Message(
                                    jObject.getString("response_text"),
                                    "bot",
                                    botAudioLink
                                )

                                messageArray.add(responseMessage)
                            }

                            while (messageArray.size != (jArray.length() * 2)) scope.launch { delay(100) }
                            onSuccess(messageArray)
                        } else {
                            val errorMessage = ContextCompat.getString(context, R.string.array_is_empty)
                            onFailure(errorMessage)
                        }
                    } catch (e: JSONException) {
                        onFailure(e.message.toString())
                    }
                }
            })
        }, {
            val errorMessage = ContextCompat.getString(context, R.string.null_uid)
            onFailure(errorMessage)
        })
    }

    fun sendAudio(audioFile: File, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val inputPath = audioFile.absolutePath
        val outputPath = "${context.cacheDir}/temp/temp_converted.mp3"

        convertToMp3(inputPath, outputPath) { result ->
            if (result) {
                val file = File(outputPath, "temp_converted.mp3")

                val fileBody = file.asRequestBody("application/octet-stream".toMediaType())
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("audio_file", audioFile.name, fileBody)
                    .addFormDataPart("format", "m4a")
                    .build()

                val request = Request.Builder()
                    .url("https://ml1.ssrlab.by/api/upload_audio")
                    .post(body)
                    .build()

                chatClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onFailure(e.message.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string() ?: "null"

                        if (responseBody != "null")
                            onSuccess(responseBody)
                    }
                })
            } else {
                val errorMessage = ContextCompat.getString(context, R.string.error_uploading_audio)
                onFailure(errorMessage)
            }
        }
    }

    fun sendMessage(
        chatId: String,
        text: String?,
        audioLink: String?,
        voiceType: String,
        role: String,
        onSuccess: (Message) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val isText = audioLink == null

        if (isText) sendTextMessage(chatId, text!!, voiceType, role, onSuccess, onFailure)
        else sendVoiceMessage(chatId, audioLink!!, voiceType, role, onSuccess, onFailure)
    }

    private fun sendTextMessage(
        chatId: String,
        text: String,
        voiceType: String,
        role: String,
        onSuccess: (Message) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json".toMediaType()
        val body = "{\"text\":\"$text\",\"voice_type\":\"${voiceType.lowercase()}\",\"chat_id\":\"$chatId\",\"role\":\"${role.lowercase()}\",\"with_voice\":true,\"search_in_internet\":true,\"format\":\"mp3\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://ml1.ssrlab.by/api/android/text")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        chatClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val jObject = responseBody?.let { JSONObject(it) }

                    val message = Message(
                        jObject!!.getString("text"),
                        "bot",
                        jObject.getString("audio")
                    )

                    onSuccess(message)
                } catch (e: JSONException) {
                    onFailure(e.message.toString())
                }
            }
        })
    }

    private fun sendVoiceMessage(
        chatId: String,
        audioLink: String,
        voiceType: String,
        role: String,
        onSuccess: (Message) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val mediaType = "application/json".toMediaType()
        val body = "{\"chat_id\":\"$chatId\",\"voice_type\":\"${voiceType.lowercase()}\",\"role\":\"${role.lowercase()}\",\"input_audio_url\":$audioLink,\"format\":\"mp3\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://ml1.ssrlab.by/api/android/voice-new")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        chatClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val jObject = responseBody?.let { JSONObject(it) }

                    val message = Message(
                        jObject!!.getString("text"),
                        "bot",
                        jObject.getString("audio")
                    )

                    onSuccess(message)
                } catch (e: JSONException) {
                    onFailure(e.message.toString())
                }
            }
        })
    }

    private fun checkUid(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val uid = fireAuth.currentUser?.uid ?: "null"

        if (uid != "null") onSuccess(uid)
        else onFailure()
    }

    private fun convertToMp3(inputPath: String, outputPath: String, onResult: (Boolean) -> Unit) {
        val command = arrayOf(
            "-i", inputPath, "-y",
            "-vn", //disable video
            "-ar", "44100", //frequency
            "-ac", "2", //channels
            "-b:a", "32k", //bitrate
            outputPath
        )

        FFmpeg.executeAsync(command) { _, returnCode ->
            if (returnCode == RETURN_CODE_SUCCESS) onResult(true)
            else onResult(false)
        }
    }
}