package com.mindcluster.safediary.profiles.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

sealed interface AvatarConfig : ValueObject {
    data class Initials(val initials: String = "EV") : AvatarConfig
    data class CustomImage(val imageUri: String) : AvatarConfig
    data class PresetAvatar(val presetId: String, val name: String) : AvatarConfig

    companion object {
        val DEFAULT_PRESETS = listOf(
            PresetAvatar("preset_serene", "Serene"),
            PresetAvatar("preset_botanical", "Botanical"),
            PresetAvatar("preset_abstract", "Abstract")
        )
    }
}
