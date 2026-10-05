package com.mindcluster.safediary.assistantai.application.commands

data class EditMessageCommand(
    val messageRemoteId: Long,
    val newPrompt: String
)
