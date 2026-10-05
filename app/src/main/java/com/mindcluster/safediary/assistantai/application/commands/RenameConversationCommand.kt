package com.mindcluster.safediary.assistantai.application.commands

data class RenameConversationCommand(
    val remoteId: String,
    val newTitle: String
)
