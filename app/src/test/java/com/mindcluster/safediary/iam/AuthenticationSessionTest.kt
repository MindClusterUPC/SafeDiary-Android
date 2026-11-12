package com.mindcluster.safediary.iam

import com.google.gson.Gson
import com.mindcluster.safediary.iam.infrastructure.remote.dto.SignInResponseDto
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionHolder
import com.mindcluster.safediary.shared.infrastructure.security.SessionTokenPolicy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthenticationSessionTest {
    @Before fun resetSession() {
        AuthSessionHolder.token = null
        AuthSessionHolder.acknowledgeSignInRequired()
    }

    @After fun cleanSession() = resetSession()

    @Test fun readsTheRealIamResponseWithoutInventingCredentials() {
        val response = Gson().fromJson(
            """{"accountId":42,"accessToken":"header.payload.signature","role":"PATIENT"}""",
            SignInResponseDto::class.java
        )
        assertEquals(42L, response.resolvedAccountId)
        assertEquals("header.payload.signature", response.resolvedToken)
        assertEquals("PATIENT", response.resolvedRole)
    }

    @Test fun acceptsLegacyFieldNamesOnlyWhenCredentialsArePresent() {
        val response = Gson().fromJson(
            """{"id":9,"token":"header.payload.signature"}""",
            SignInResponseDto::class.java
        )
        assertEquals(9L, response.resolvedAccountId)
        assertEquals("header.payload.signature", response.resolvedToken)
    }

    @Test fun successfulHttpResponseWithoutCredentialsCannotCreateADemoSession() {
        val response = Gson().fromJson("{}", SignInResponseDto::class.java)
        assertThrows(IllegalStateException::class.java) { response.resolvedToken }
        assertThrows(IllegalStateException::class.java) { response.resolvedAccountId }
    }

    @Test fun rejectsMockBlankAndMalformedTokens() {
        listOf(null, "", " ", "mock_jwt_token_safe_diary", "mock_jwt_token_old", "opaque", "one.two", "one..three").forEach {
            assertFalse(SessionTokenPolicy.isUsable(it))
        }
        val response = Gson().fromJson(
            """{"accountId":1,"token":"mock_jwt_token_safe_diary"}""",
            SignInResponseDto::class.java
        )
        assertThrows(IllegalStateException::class.java) { response.resolvedToken }
    }

    @Test fun rejectionRequestsSignInAndPreventsPreferencesFromRestoringThatSession() {
        AuthSessionHolder.token = "header.old.signature"
        AuthSessionHolder.rejectToken("header.old.signature")
        AuthSessionHolder.restoreToken("header.old.signature")
        assertNull(AuthSessionHolder.token)
        assertTrue(AuthSessionHolder.requiresSignIn.value)
    }

    @Test fun lateUnauthorizedResponseCannotSignOutANewSession() {
        AuthSessionHolder.token = "header.old.signature"
        AuthSessionHolder.token = "header.new.signature"
        AuthSessionHolder.rejectToken("header.old.signature")
        assertEquals("header.new.signature", AuthSessionHolder.token)
        assertFalse(AuthSessionHolder.requiresSignIn.value)
    }

    @Test fun newAuthenticationClearsTheExpiredSessionSignal() {
        AuthSessionHolder.rejectStoredSession()
        assertTrue(AuthSessionHolder.requiresSignIn.value)
        AuthSessionHolder.token = "header.new.signature"
        assertFalse(AuthSessionHolder.requiresSignIn.value)
    }

    @Test fun publicUnauthorizedResponseWithoutATokenCannotInvalidateANewSession() {
        AuthSessionHolder.token = "header.new.signature"
        AuthSessionHolder.rejectToken(null)
        assertEquals("header.new.signature", AuthSessionHolder.token)
        assertFalse(AuthSessionHolder.requiresSignIn.value)
    }
}
