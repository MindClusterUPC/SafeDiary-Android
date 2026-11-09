package com.mindcluster.safediary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.mindcluster.safediary.profiles.infrastructure.local.AvatarImageStorage
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AvatarImageStorageTest {
    @Test fun importedPhotoSurvivesSourceRemovalAndHasAccountOwnedStorage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val account = 9_000_000_000_000L + System.currentTimeMillis()
        val source = File.createTempFile("qa-avatar-", ".png", context.cacheDir)
        val bitmap = Bitmap.createBitmap(1200, 800, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.BLUE)
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        var saved: File? = null
        try {
            saved = File(Uri.parse(AvatarImageStorage.importImage(context, Uri.fromFile(source), account)).path!!)
            assertTrue(source.delete())
            assertTrue(saved.exists())
            assertEquals(File(context.filesDir, "avatars/account-$account").canonicalPath, saved.parentFile!!.canonicalPath)
            val decoded = BitmapFactory.decodeFile(saved.path)
            assertNotNull(decoded)
            assertTrue(maxOf(decoded.width, decoded.height) <= 512)
            decoded.recycle()
        } finally { source.delete(); saved?.delete(); saved?.parentFile?.delete() }
    }

    @Test fun invalidAndOversizedFilesAreRejectedBeforeSaving() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = File.createTempFile("qa-avatar-invalid-", ".bin", context.cacheDir)
        try {
            source.writeText("This is not a photo")
            assertThrows(IllegalArgumentException::class.java) {
                AvatarImageStorage.importImage(context, Uri.fromFile(source), 9_000_000_000_001L)
            }
            java.io.RandomAccessFile(source, "rw").use { it.setLength(AvatarImageStorage.MAX_BYTES.toLong() + 1) }
            assertThrows(IllegalArgumentException::class.java) {
                AvatarImageStorage.importImage(context, Uri.fromFile(source), 9_000_000_000_001L)
            }
        } finally { source.delete() }
    }
}
