package com.mindcluster.safediary.assistantai.domain

import com.mindcluster.safediary.assistantai.domain.model.DiaritoPersonality
import org.junit.Assert.assertEquals
import org.junit.Test

class DiaritoPersonalityTest {

    @Test
    fun fromApiNameReturnsCorrectPersonalityForValidNames() {
        assertEquals(DiaritoPersonality.SOL, DiaritoPersonality.fromApiName("Sol"))
        assertEquals(DiaritoPersonality.LUMA, DiaritoPersonality.fromApiName("Luma"))
        assertEquals(DiaritoPersonality.KAI, DiaritoPersonality.fromApiName("Kai"))
        assertEquals(DiaritoPersonality.NARA, DiaritoPersonality.fromApiName("Nara"))
    }

    @Test
    fun fromApiNameHandlesLowercaseNames() {
        assertEquals(DiaritoPersonality.SOL, DiaritoPersonality.fromApiName("sol"))
        assertEquals(DiaritoPersonality.LUMA, DiaritoPersonality.fromApiName("luma"))
        assertEquals(DiaritoPersonality.KAI, DiaritoPersonality.fromApiName("kai"))
        assertEquals(DiaritoPersonality.NARA, DiaritoPersonality.fromApiName("nara"))
    }

    @Test
    fun fromApiNameReturnsSolWhenNull() {
        assertEquals(DiaritoPersonality.SOL, DiaritoPersonality.fromApiName(null))
    }

    @Test
    fun fromApiNameReturnsSolWhenUnknownOrEmpty() {
        assertEquals(DiaritoPersonality.SOL, DiaritoPersonality.fromApiName(""))
        assertEquals(DiaritoPersonality.SOL, DiaritoPersonality.fromApiName("unknown"))
        assertEquals(DiaritoPersonality.SOL, DiaritoPersonality.fromApiName("Zeus"))
    }

    @Test
    fun personalitiesHaveExpectedApiNames() {
        assertEquals("Sol", DiaritoPersonality.SOL.apiName)
        assertEquals("Luma", DiaritoPersonality.LUMA.apiName)
        assertEquals("Kai", DiaritoPersonality.KAI.apiName)
        assertEquals("Nara", DiaritoPersonality.NARA.apiName)
    }
}
