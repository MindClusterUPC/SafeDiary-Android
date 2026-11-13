package com.mindcluster.safediary.shared.infrastructure.security

object AccountStorage {
    fun databaseName(accountId: Long): String {
        require(accountId >= 0)
        return "safediary-account-$accountId.db"
    }
    fun chatPreferencesName(accountId: Long): String {
        require(accountId >= 0)
        return "diarito-account-$accountId"
    }
}
