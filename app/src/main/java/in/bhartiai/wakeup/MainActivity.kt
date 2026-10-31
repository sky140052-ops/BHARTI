package `in`.bhartiai.wakeup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.graphics.Typeface
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private var recognizer: SpeechRecognizer? = null
    private lateinit var status: TextView
    private lateinit var startButton: Button
    private var listening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply {
            text = "नमस्ते Boss!\nHey Bharti बोलने के लिए Start Listening दबाएँ।"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            typeface = Typeface.DEFAULT_BOLD
        }
        startButton = Button(this).apply { text = "Start listening" }
        val stopButton = Button(this).apply { text = "Stop listening" }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            addView(status)
            addView(startButton)
            addView(stopButton)
        }
        setContentView(layout)
        startButton.setOnClickListener { requestMicAndStart() }
        stopButton.setOnClickListener { stopListening() }
    }

    private fun requestMicAndStart() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
        } else startListening()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) startListening()
        else status.text = "माइक्रोफोन अनुमति नहीं मिली।"
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "इस फोन में Speech Recognition उपलब्ध नहीं है।"
            return
        }
        listening = true
        startButton.isEnabled = false
        status.text = "सुन रही हूँ… 'Hey Bharti' बोलिए।"
        listenOnce()
    }

    private fun listenOnce() {
        if (!listening) return
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status.text = "सुन रही हूँ… 'Hey Bharti' बोलिए।" }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { if (listening) listenOnce() }
            override fun onResults(results: Bundle?) {
                val heard = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty().joinToString(" ").lowercase(Locale.ROOT)
                if (heard.contains("hey bharti") || heard.contains("हे भारती")) {
                    listening = false
                    status.text = "Bharti मिल गई! ChatGPT खोल रही हूँ…"
                    openChatGPT()
                } else if (listening) listenOnce()
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val heard = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.joinToString(" ")?.lowercase(Locale.ROOT).orEmpty()
                if (heard.contains("hey bharti") || heard.contains("हे भारती")) {
                    listening = false
                    status.text = "Bharti मिल गई! ChatGPT खोल रही हूँ…"
                    openChatGPT()
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        recognizer?.startListening(intent)
    }

    private fun openChatGPT() {
        val launch = packageManager.getLaunchIntentForPackage("com.openai.chatgpt")
        if (launch != null) startActivity(launch)
        else Toast.makeText(this, "ChatGPT इंस्टॉल नहीं है।", Toast.LENGTH_LONG).show()
        startButton.isEnabled = true
    }

    private fun stopListening() {
        listening = false
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        if (::startButton.isInitialized) startButton.isEnabled = true
        if (::status.isInitialized) status.text = "Listening बंद है।"
    }

    override fun onDestroy() {
        stopListening()
        super.onDestroy()
    }
}