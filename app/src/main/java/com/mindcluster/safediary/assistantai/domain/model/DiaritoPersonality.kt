package com.mindcluster.safediary.assistantai.domain.model

enum class DiaritoPersonality(val apiName: String) {
    SOL("Sol"),
    LUMA("Luma"),
    KAI("Kai"),
    NARA("Nara");

    companion object {
        fun fromApiName(name: String?): DiaritoPersonality {
            if (name == null) return SOL
            return entries.firstOrNull { it.apiName.equals(name, ignoreCase = true) } ?: SOL
        }
    }
}
