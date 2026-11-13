package com.mindcluster.safediary.shared.presentation.components

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.net.Uri
import android.graphics.BitmapFactory
import kotlinx.coroutines.CancellationException
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.profiles.infrastructure.local.ProfilePreferences

@Composable
fun UserProfileAvatar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.semantics { contentDescription = "Perfil y configuración" }) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFD1E3D4))
            .border(1.dp, Color(0xFFCBD5E1), CircleShape), contentAlignment = Alignment.Center) {
            ProfileAvatarContent(Modifier.matchParentSize(), fontSize = 13.sp)
        }
    }
}

/** Displays the same saved picture (or initials) in the header, profile and selector. */
@Composable
fun ProfileAvatarContent(modifier: Modifier = Modifier, fontSize: TextUnit = 32.sp) {
    val context = LocalContext.current
    val preferences = remember(context) { ProfilePreferences(context) }
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(preferences) {
        val unsubscribe = preferences.observeChanges { revision++ }
        onDispose { unsubscribe() }
    }
    val avatar = remember(revision) { Triple(preferences.initials, preferences.avatarType, preferences.avatarValue) }
    ProfileAvatarPicture(avatar.first, avatar.third.takeIf { avatar.second == "CUSTOM" },
        modifier, fontSize, avatar.second == "PRESET")
}

@Composable
internal fun ProfileAvatarPicture(initials: String, imageUri: String?, modifier: Modifier = Modifier,
                                  fontSize: TextUnit = 32.sp, preset: Boolean = false) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, imageUri) {
        value = null
        if (!imageUri.isNullOrBlank()) {
            value = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(Uri.parse(imageUri))?.use {
                        val options = BitmapFactory.Options().apply { inSampleSize = 1 }
                        BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
                    }
                } catch (e: CancellationException) { throw e
                } catch (_: Exception) { null }
            }
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap!!, "Foto de perfil", Modifier.matchParentSize(),
            contentScale = ContentScale.Crop)
        else if (preset) Icon(Icons.Outlined.Spa,
            "Avatar ilustrado", Modifier.size(32.dp), tint = Color(0xFF006B5F))
        else Text(initials, fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A2F))
    }
}
