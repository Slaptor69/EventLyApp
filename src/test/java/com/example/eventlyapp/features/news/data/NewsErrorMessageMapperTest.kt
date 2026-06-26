package com.example.eventlyapp.features.news.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NewsErrorMessageMapperTest {
    @Test
    fun missingApiKeyMessage_isUserReadable() {
        assertEquals(
            "Новости временно недоступны: в приложении не настроен ключ NYT API.",
            NewsErrorMessageMapper.missingApiKeyMessage()
        )
    }

    @Test
    fun toUserMessage_mapsUnauthorizedHttpToApiKeyMessage() {
        val message = NewsErrorMessageMapper.toUserMessage(httpException(401))

        assertEquals(
            "NYT отклонил ключ API. Проверь, что ключ активен и привязан к Top Stories API.",
            message
        )
    }

    @Test
    fun toUserMessage_mapsRateLimitToRetryLaterMessage() {
        val message = NewsErrorMessageMapper.toUserMessage(httpException(429))

        assertEquals(
            "NYT временно ограничил количество запросов. Попробуй обновить новости позже.",
            message
        )
    }

    @Test
    fun toUserMessage_mapsUnknownHostCauseToNoInternetMessage() {
        val message = NewsErrorMessageMapper.toUserMessage(
            IllegalStateException("wrapped", UnknownHostException())
        )

        assertEquals(
            "Нет подключения к интернету. Проверь сеть и попробуй ещё раз.",
            message
        )
    }

    @Test
    fun toUserMessage_mapsTimeoutBeforeGenericIoMessage() {
        val message = NewsErrorMessageMapper.toUserMessage(SocketTimeoutException())

        assertEquals(
            "NYT слишком долго отвечает. Попробуй обновить новости ещё раз.",
            message
        )
    }

    @Test
    fun toUserMessage_mapsGenericIoToConnectionMessage() {
        val message = NewsErrorMessageMapper.toUserMessage(IOException())

        assertEquals(
            "Не удалось подключиться к NYT. Проверь интернет и попробуй снова.",
            message
        )
    }

    private fun httpException(code: Int): HttpException {
        val responseBody = "error".toResponseBody("text/plain".toMediaType())
        return HttpException(Response.error<Unit>(code, responseBody))
    }
}
