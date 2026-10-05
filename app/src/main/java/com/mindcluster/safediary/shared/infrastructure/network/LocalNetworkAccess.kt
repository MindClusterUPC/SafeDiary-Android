package com.mindcluster.safediary.shared.infrastructure.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.mindcluster.safediary.BuildConfig

/**
 * Android 17+ requires a runtime permission to reach private network addresses.
 * Only debug builds need it, because they talk to the backend running on the developer machine.
 */
object LocalNetworkAccess {

    const val PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"

    private const val ANDROID_17 = 37

    fun needsRuntimeGrant(context: Context): Boolean {
        if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < ANDROID_17) return false
        return ContextCompat.checkSelfPermission(context, PERMISSION) != PackageManager.PERMISSION_GRANTED
    }
}
