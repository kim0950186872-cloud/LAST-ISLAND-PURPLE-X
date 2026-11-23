package com.purplex.analyzer

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var output: TextView
    private val purple = Color.rgb(178, 120, 255)
    private val dark = Color.rgb(15, 11, 24)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 18)
            setBackgroundColor(dark)
        }
        root.addView(TextView(this).apply {
            text = "LAST ISLAND PURPLE X"
            textSize = 23f
            setTextColor(purple)
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "PATCH FILE INSPECTOR  •  v0.2"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        })
        root.addView(Button(this).apply {
            text = "เลือก APK / OBB / .bytes / JSON"
            setOnClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(i, 100)
            }
        })
        root.addView(TextView(this).apply {
            text = "โหมดอ่านอย่างเดียว: ตรวจขนาด, SHA-256, โครงสร้าง ZIP, JSON และข้อความที่อ่านได้ในไฟล์ไบนารี ไม่แก้ไข ไม่ฉีดแพตช์ และไม่เชื่อมต่อกับเกม"
            textSize = 13f
            setTextColor(Color.rgb(215, 205, 230))
            setPadding(4, 14, 4, 14)
        })
        val scroll = ScrollView(this)
        output = TextView(this).apply {
            text = "ยังไม่ได้เลือกไฟล์\n\nเลือกไฟล์ที่คุณมีสิทธิ์เข้าถึงเพื่อเริ่มตรวจสอบ"
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(12, 12, 12, 24)
        }
        scroll.addView(output)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    @Deprecated("Deprecated API retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            output.text = "กำลังตรวจสอบไฟล์..."
            Thread {
                val report = inspect(uri)
                runOnUiThread { output.text = report }
            }.start()
        }
    }

    private fun inspect(uri: Uri): String = try {
        val name = displayName(uri)
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("เปิดไฟล์ไม่ได้")
        val sha = MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }

        val text = bytes.toString(Charsets.UTF_8)
        val isJson = name.endsWith(".json", true) || text.trimStart().startsWith("{")
        val jsonReport = if (isJson) {
            try {
                JSONObject(text)
                "JSON: โครงสร้าง JSON ถูกต้อง"
            } catch (e: Exception) {
                "JSON: อ่านเป็น JSON ไม่สำเร็จ หรือไฟล์ไม่ใช่ JSON แบบข้อความ"
            }
        } else "JSON: ไม่ได้ระบุว่าเป็นไฟล์ JSON"

        val printable = Regex("[ -~]{5,}")
            .findAll(text)
            .map { it.value.take(180) }
            .distinct()
            .take(80)
            .toList()

        val zipEntries = mutableListOf<String>()
        var zipValid = false
        try {
            ZipInputStream(bytes.inputStream()).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    zipValid = true
                    zipEntries.add(e.name + if (e.isDirectory) "/" else "")
                    if (zipEntries.size >= 300) break
                    zip.closeEntry()
                }
            }
        } catch (_: Exception) { }

        buildString {
            appendLine("PURPLE X — FILE REPORT")
            appendLine("================================")
            appendLine("ชื่อไฟล์: $name")
            appendLine("ขนาด: ${bytes.size} bytes")
            appendLine("SHA-256:")
            appendLine(sha)
            appendLine(jsonReport)
            appendLine()
            if (zipValid) {
                appendLine("ZIP/APK: พบรายการภายใน ${zipEntries.size} รายการ (สูงสุด 300)")
                zipEntries.forEach { appendLine("• $it") }
            } else {
                appendLine("ZIP/APK: ไม่พบ ZIP directory ที่อ่านได้")
            }
            appendLine()
            appendLine("ข้อความ ASCII ที่อ่านได้ (สูงสุด 80 รายการ):")
            if (printable.isEmpty()) appendLine("ไม่พบข้อความ ASCII ที่อ่านได้")
            printable.forEach { appendLine("• $it") }
            appendLine()
            appendLine("ข้อจำกัด: การพบชื่อคลาสหรือข้อความไม่ยืนยันว่าฟังก์ชันนั้นทำงานจริง")
            appendLine("รุ่นนี้ไม่ถอดรหัส/แก้ไข patch, ไม่ inject code และไม่แก้ไฟล์เกม")
        }
    } catch (e: Exception) {
        "ตรวจสอบไม่สำเร็จ: ${e.message}"
    }

    private fun displayName(uri: Uri): String {
        var name = "unknown"
        contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c ->
                if (c.moveToFirst()) {
                    val i = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (i >= 0) name = c.getString(i) ?: name
                }
            }
        return name
    }
}
