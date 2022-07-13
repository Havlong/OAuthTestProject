package ru.havlong.test.data

import android.content.Context
import net.openid.appauth.ResponseTypeValues
import ru.havlong.test.R

class AuthConfiguration(context: Context) {
    val AUTH_URI = context.getString(R.string.authorize_url)
    val TOKEN_URI = context.getString(R.string.access_token_url)
    val RESPONSE_TYPE = ResponseTypeValues.CODE

    val CLIENT_ID = context.getString(R.string.client_id)
    val CLIENT_SECRET = context.getString(R.string.client_secret)
    val CALLBACK_URL = context.getString(R.string.redirect_uri)
}