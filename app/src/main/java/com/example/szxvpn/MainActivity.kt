package com.example.szxvpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)

        findViewById<Button>(R.id.connect).setOnClickListener {
            val prepare = VpnService.prepare(this)
            if (prepare != null) startActivityForResult(prepare, 100) else startVpn()
        }
        findViewById<Button>(R.id.disconnect).setOnClickListener {
            startService(Intent(this, VpnServiceImpl::class.java).setAction(VpnServiceImpl.ACTION_DISCONNECT))
        }
    }

    @Deprecated("legacy activity result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) startVpn()
        else if (requestCode == 100) status.text = "VPN permission denied"
    }

    private fun startVpn() {
        val cfg = findViewById<EditText>(R.id.config).text.toString().trim()
        val proxy = findViewById<EditText>(R.id.proxy).text.toString().trim()
        val payload = findViewById<EditText>(R.id.payload).text.toString()

        if (!cfg.startsWith("vless://") && !cfg.startsWith("vmess://")) {
            status.text = "Enter a VLESS or VMess link"
            return
        }

        val i = Intent(this, VpnServiceImpl::class.java)
            .setAction(VpnServiceImpl.ACTION_CONNECT)
            .putExtra(VpnServiceImpl.EXTRA_CONFIG, cfg)
            .putExtra(VpnServiceImpl.EXTRA_PROXY, proxy)
            .putExtra(VpnServiceImpl.EXTRA_PAYLOAD, payload)

        ContextCompat.startForegroundService(this, i)
        status.text = "STARTING"
    }
}
