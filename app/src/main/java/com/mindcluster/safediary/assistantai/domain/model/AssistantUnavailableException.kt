package com.mindcluster.safediary.assistantai.domain.model

class AssistantUnavailableException(cause: Throwable? = null) :
    Exception("The AI assistant is temporarily unavailable", cause)
