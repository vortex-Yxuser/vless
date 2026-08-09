package com.example.szxvpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

/**
 * VPN service bridge.
 *
 * The exact libbox API is version-sensitive. The workflow pins a sing-box revision
 * and generates/copies libbox.aar. This service deliberately keeps the Android
 * VpnService lifecycle separate from protocol parsing so a libbox API change does
 * not silently produce a fake "connected" state.
 */
class VpnServiceImpl : VpnService() {
    companion object {
        const val ACTION_CONNECT = "com.example.szxvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.szxvpn.DISCONNECT"
        const val EXTRA_CONFIG = "config"
        const val EXTRA_PROXY = "proxy"
        const val EXTRA_PAYLOAD = "payload"
        private const val CHANNEL = "szxvpn"
    }

    private var tun: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> startTunnel(intent)
            ACTION_DISCONNECT -> stopTunnel()
        }
        return START_NOT_STICKY
    }

    private fun startTunnel(intent: Intent) {
        createChannel()
        startForeground(1, notification("Starting sing-box"))
        val config = intent.getStringExtra(EXTRA_CONFIG).orEmpty()

        // Parse/validate link before creating the VPN. The actual sing-box engine
        // is supplied by libbox.aar built by GitHub Actions.
        if (!ConfigParser.isSupportedShareLink(config)) {
            stopTunnel()
            return
        }

        tun?.close()
        tun = Builder()
            .setSession("SZXVPN")
            .addAddress("172.19.0.1", 30)
            .addRoute("0.0.0.0", 0)
            .setBlocking(false)
            .establish()

        // IMPORTANT: this is intentionally not reported as a real protocol
        // connection until the pinned libbox runtime is invoked successfully.
        // The generated build contains libbox.aar; the next adapter method is
        // isolated here for the pinned sing-box API.
    }

    private fun stopTunnel() {
        try { tun?.close() } catch (_: Throwable) {}
        tun = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        try { tun?.close() } catch (_: Throwable) {}
        tun = null
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel(CHANNEL, "SZXVPN", NotificationManager.IMPORTANCE_LOW)
                )
        }
    }

    private fun notification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("SZXVPN")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setOngoing(true)
            .build()
}
