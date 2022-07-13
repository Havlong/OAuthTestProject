package ru.havlong.test.data

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.openid.appauth.*
import kotlin.coroutines.suspendCoroutine

class AuthRepository(private val authConfig: AuthConfiguration) {
    private val serviceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse(authConfig.AUTH_URI),
        Uri.parse(authConfig.TOKEN_URI)
    )

    private val clientAuthentication: ClientAuthentication
        get() = ClientSecretPost(authConfig.CLIENT_SECRET)

    fun getAuthRequest(): AuthorizationRequest {
        val redirectUri = authConfig.CALLBACK_URL.toUri()

        return AuthorizationRequest.Builder(
            serviceConfiguration,
            authConfig.CLIENT_ID,
            authConfig.RESPONSE_TYPE,
            redirectUri
        ).build()
    }


    suspend fun performTokenRequest(
        authService: AuthorizationService,
        tokenRequest: TokenRequest
    ): String = withContext(Dispatchers.IO) {
        suspendCoroutine { coroutine ->
            authService.performTokenRequest(
                tokenRequest,
                clientAuthentication
            ) { response, exception ->
                when {
                    response != null -> coroutine.resumeWith(Result.success(response.accessToken.orEmpty()))
                    exception != null -> coroutine.resumeWith(Result.failure(exception))
                }
            }
        }
    }
}
