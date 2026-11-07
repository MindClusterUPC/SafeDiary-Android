package com.mindcluster.safediary.profiles.application.commands

import com.mindcluster.safediary.profiles.domain.model.AvatarConfig
import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository

data class UpdateAvatarCommand(
    val patientId: Long,
    val avatarConfig: AvatarConfig
)

class UpdateAvatarHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(command: UpdateAvatarCommand): Result<PatientProfileAggregate> = runCatching {
        repository.updateAvatar(command.patientId, command.avatarConfig)
    }
}
