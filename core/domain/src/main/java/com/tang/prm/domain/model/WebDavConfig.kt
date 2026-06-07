package com.tang.prm.domain.model

data class WebDavConfig(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val remotePath: String = "/knots_backup/",
    val autoSyncOnLaunch: Boolean = false,
    val lastSyncTime: Long = 0,
    val lastSyncDirection: String = ""
)
