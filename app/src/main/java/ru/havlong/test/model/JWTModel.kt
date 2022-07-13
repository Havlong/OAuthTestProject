package ru.havlong.test.model

import kotlinx.serialization.Serializable

@Serializable
data class JWTModel(val username: String, val nickname: String, val avatar: ImageDesc)

@Serializable
data class ImageDesc(val thumb: String, val full: String)
