package ru.havlong.test

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenRequest
import ru.havlong.test.data.AuthConfiguration
import ru.havlong.test.data.AuthRepository

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val authConfig = AuthConfiguration(application)
    private val authService = AuthorizationService(getApplication())
    private val authRepository = AuthRepository(authConfig)

    private val loading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = loading

    private val toastChannel = Channel<Int>(Channel.BUFFERED)
    val toastFlow: Flow<Int>
        get() = toastChannel.receiveAsFlow()

    private val intentChannel = Channel<Intent>(Channel.BUFFERED)
    val intentFlow: Flow<Intent>
        get() = intentChannel.receiveAsFlow()

    private val navigateChannel = Channel<String>(Channel.BUFFERED)
    val navigateFlow: Flow<String>
        get() = navigateChannel.receiveAsFlow()

    override fun onCleared() {
        super.onCleared()
        authService.dispose()
    }

    fun onFail(exception: AuthorizationException) {
        toastChannel.trySendBlocking(R.string.auth_cancelled)
        Log.e("Authorization", exception.stackTraceToString())
    }

    fun onCode(tokenExchangeRequest: TokenRequest) {
        viewModelScope.launch {
            loading.value = true
            kotlin.runCatching {
                authRepository.performTokenRequest(authService, tokenExchangeRequest)
            }.onSuccess {
                loading.value = false
                navigateChannel.send(it)
            }.onFailure {
                loading.value = false
                toastChannel.send(R.string.auth_cancelled)
                Log.e("Access token", it.stackTraceToString())
            }
        }
    }

    fun openPage() {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        val openAuthPageIntent = authService.getAuthorizationRequestIntent(
            authRepository.getAuthRequest(),
            customTabsIntent
        )

        intentChannel.trySendBlocking(openAuthPageIntent)
    }
}