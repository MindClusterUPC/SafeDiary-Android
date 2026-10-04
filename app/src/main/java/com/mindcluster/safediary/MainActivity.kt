package com.mindcluster.safediary

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mindcluster.safediary.shared.infrastructure.network.LocalNetworkAccess
import com.mindcluster.safediary.shared.presentation.navigation.AppNavigation
import com.mindcluster.safediary.shared.presentation.theme.SafeDiaryTheme

class MainActivity : AppCompatActivity() {

    private val localNetworkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* chat falls back to mock if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (LocalNetworkAccess.needsRuntimeGrant(this)) {
            localNetworkPermission.launch(LocalNetworkAccess.PERMISSION)
        }
        setContent {
            SafeDiaryTheme {
                AppNavigation()
            }
        }
    }
}
