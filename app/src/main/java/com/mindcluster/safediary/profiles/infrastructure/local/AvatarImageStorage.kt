package com.mindcluster.safediary.profiles.infrastructure.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object AvatarImageStorage {
    const val MAX_BYTES = 5 * 1024 * 1024

    /** Copies selected media into app-private, account-owned storage. Call on an IO dispatcher. */
    fun importImage(context: Context, uri: Uri, accountId: Long): String {
        require(accountId > 0) { "Inicia sesión antes de elegir una foto." }
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                require(output.size() + count <= MAX_BYTES) { "La imagen supera los 5 MB." }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        } ?: error("No se pudo abrir la imagen.")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "El archivo no es una imagen válida." }
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        var bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(java.nio.ByteBuffer.wrap(bytes))) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val orientedSize = maxOf(info.size.width, info.size.height)
                if (orientedSize > 512) decoder.setTargetSize(
                    (info.size.width * (512.0 / orientedSize)).toInt().coerceAtLeast(1),
                    (info.size.height * (512.0 / orientedSize)).toInt().coerceAtLeast(1))
            }
        } else {
            val options = BitmapFactory.Options().apply { inSampleSize = 1 }
            while (largest / options.inSampleSize > 1024) options.inSampleSize *= 2
            val decoded = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
            val orientation = try { bytes.inputStream().use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) } } catch (_: java.io.IOException) { 1 }
            val matrix = Matrix().apply {
                when (orientation) {
                    2 -> setScale(-1f, 1f)
                    3 -> setRotate(180f)
                    4 -> setScale(1f, -1f)
                    5 -> { setRotate(90f); postScale(-1f, 1f) }
                    6 -> setRotate(90f)
                    7 -> { setRotate(270f); postScale(-1f, 1f) }
                    8 -> setRotate(270f)
                }
            }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true).also {
                if (it !== decoded) decoded.recycle()
            }
        }
        val size = maxOf(bitmap.width, bitmap.height)
        if (size > 512) {
            val scaled = Bitmap.createScaledBitmap(bitmap, (bitmap.width * (512.0 / size)).toInt().coerceAtLeast(1),
                (bitmap.height * (512.0 / size)).toInt().coerceAtLeast(1), true)
            if (scaled !== bitmap) bitmap.recycle()
            bitmap = scaled
        }
        val folder = File(context.filesDir, "avatars/account-$accountId").apply { check(mkdirs() || isDirectory) }
        val file = File(folder, "${UUID.randomUUID()}.jpg")
        try {
            file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) }
            return Uri.fromFile(file).toString()
        } catch (e: Exception) {
            file.delete()
            throw e
        } finally { bitmap.recycle() }
    }
}
