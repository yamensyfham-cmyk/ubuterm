package com.ubuterm

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.ubuterm.terminal.PtyBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ptyHandle = 0L
    private val output = TextView(this)
    private val input = EditText(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        output.setTypeface(Typeface.MONOSPACE)
        output.setTextColor(Color.LTGRAY)
        output.textSize = 14f
        input.setTypeface(Typeface.MONOSPACE)
        input.hint = "type command, Enter to run"
        val root = FrameLayout(this)
        val outFrame = FrameLayout(this)
        outFrame.addView(output, FrameLayout.LayoutParams(-1, 0, 1f))
        root.addView(outFrame, FrameLayout.LayoutParams(-1, 0, 1f))
        root.addView(input, FrameLayout.LayoutParams(-1, 180))
        setContentView(root)
        startPty()
        input.setOnEditorActionListener { _, _, _ ->
            val line = input.text.toString()
            input.setText("")
            if (line.isNotEmpty()) {
                scope.launch(Dispatchers.IO) { PtyBridge.writeToPty(ptyHandle, (line + "\n").toByteArray()) }
            }
            true
        }
    }

    private fun startPty() {
        scope.launch(Dispatchers.IO) {
            ptyHandle = PtyBridge.openPty(80, 24)
            val buffer = ByteArray(4096)
            while (ptyHandle != 0L) {
                val n = PtyBridge.readFromPty(ptyHandle, buffer)
                if (n <= 0) break
                val chunk = String(buffer, 0, n)
                runOnUiThread { output.append(chunk) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ptyHandle != 0L) PtyBridge.closePty(ptyHandle)
        ptyHandle = 0L
    }
}
