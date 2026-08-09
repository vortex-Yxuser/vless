package com.example.szxvpn

object ConfigParser {
    fun isSupportedShareLink(value: String): Boolean =
        value.startsWith("vless://") || value.startsWith("vmess://")
}
