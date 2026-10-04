package com.mindcluster.safediary

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mindcluster.safediary.shared.presentation.navigation.AppNavigation
import com.mindcluster.safediary.shared.presentation.theme.SafeDiaryTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeDiaryTheme {
                AppNavigation()
            }
        }
    }
}