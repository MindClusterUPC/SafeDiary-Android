package com.mindcluster.safediary

import android.app.Application
import com.mindcluster.safediary.shared.infrastructure.locale.AppLocaleManager

class SafeDiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLocaleManager.ensureDefault(this)
    }
}
