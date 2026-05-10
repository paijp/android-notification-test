package jp.pa_i.android.notificationtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import jp.pa_i.android.notificationtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchToken()
        } else {
            binding.tvToken.text = "通知権限が拒否されました"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCopy.setOnClickListener {
            val token = binding.tvToken.text.toString()
            if (token.isNotEmpty() && !token.startsWith("取得")) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("FCM Token", token))
                Toast.makeText(this, "トークンをコピーしました", Toast.LENGTH_SHORT).show()
            }
        }

        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED -> fetchToken()
                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            fetchToken()
        }
    }

    private fun fetchToken() {
        binding.tvToken.text = "取得中..."
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.tvToken.text = task.result
            } else {
                binding.tvToken.text = "トークン取得失敗: \${task.exception?.message}"
            }
        }
    }
}
