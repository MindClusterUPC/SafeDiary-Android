package com.mindcluster.safediary

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.mindcluster.safediary.shared.presentation.components.ProfileAvatarPicture
import org.junit.Rule
import org.junit.Test
import java.io.File

class AvatarRenderingTest {
    @get:Rule val compose = createComposeRule()
    @Test fun selectedImageIsDisplayedAndRemovingItRestoresInitials() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = File.createTempFile("qa-avatar-render-", ".png", context.cacheDir)
        try {
            val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.BLUE)
            source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            val photo = mutableStateOf<String?>(Uri.fromFile(source).toString())
            compose.setContent { ProfileAvatarPicture("QA", photo.value, Modifier.size(96.dp)) }
            compose.waitUntil(5000) { compose.onAllNodesWithContentDescription("Foto de perfil").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithContentDescription("Foto de perfil").assertIsDisplayed()
            compose.onNodeWithText("QA").assertDoesNotExist()
            compose.runOnIdle { photo.value = null }
            compose.onNodeWithText("QA").assertIsDisplayed()
            compose.onNodeWithContentDescription("Foto de perfil").assertDoesNotExist()
        } finally { source.delete() }
    }
}
