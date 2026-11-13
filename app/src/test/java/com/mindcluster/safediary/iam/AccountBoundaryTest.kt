package com.mindcluster.safediary.iam

import com.mindcluster.safediary.shared.infrastructure.network.SessionBoundaryInterceptor
import com.mindcluster.safediary.shared.infrastructure.security.AccountStorage
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionHolder
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.lang.reflect.Proxy

class AccountBoundaryTest {
    @After fun clearToken() { AuthSessionHolder.token = null }

    @Test fun accountsAndLegacyDataHaveDifferentStorageNames() {
        assertNotEquals(AccountStorage.databaseName(1), AccountStorage.databaseName(2))
        assertNotEquals(AccountStorage.databaseName(0), AccountStorage.databaseName(1))
        assertNotEquals("safediary.db", AccountStorage.databaseName(1))
        assertNotEquals(AccountStorage.chatPreferencesName(1), AccountStorage.chatPreferencesName(2))
        assertThrows(IllegalArgumentException::class.java) { AccountStorage.databaseName(-1) }
    }

    private fun chain(proceed: (Request) -> Unit): Interceptor.Chain {
        val request = Request.Builder().url("http://localhost/api/v1/assistant/chat").build()
        return Proxy.newProxyInstance(Interceptor.Chain::class.java.classLoader, arrayOf(Interceptor.Chain::class.java)) { _, method, args ->
            when (method.name) {
                "request" -> request
                "proceed" -> { val sent = args!![0] as Request; proceed(sent); Response.Builder().request(sent).protocol(Protocol.HTTP_1_1).code(200).message("OK").body("{}".toResponseBody()).build() }
                else -> throw UnsupportedOperationException(method.name)
            }
        } as Interceptor.Chain
    }

    @Test fun aQueuedRequestCannotBeSentAsTheNextAccount() {
        AuthSessionHolder.token = "header.B.signature"
        var requests = 0
        assertThrows(IOException::class.java) {
            SessionBoundaryInterceptor("header.A.signature").intercept(chain { requests++ })
        }
        assertEquals(0, requests)
    }

    @Test fun aLateResponseIsDiscardedAfterAnAccountChange() {
        AuthSessionHolder.token = "header.A.signature"
        assertThrows(IOException::class.java) {
            SessionBoundaryInterceptor("header.A.signature").intercept(chain { AuthSessionHolder.token = "header.B.signature" })
        }
    }

    @Test fun requestsFromTheCurrentSessionCanComplete() {
        AuthSessionHolder.token = "header.A.signature"
        SessionBoundaryInterceptor("header.A.signature").intercept(chain {
            assertEquals("Bearer header.A.signature", it.header("Authorization"))
        }).use { assertEquals(200, it.code) }
    }
}
