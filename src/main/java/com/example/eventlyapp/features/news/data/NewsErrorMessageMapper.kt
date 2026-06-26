package com.example.eventlyapp.features.news.data

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NewsErrorMessageMapper {
    fun missingApiKeyMessage(): String {
        return "Новости временно недоступны: в приложении не настроен ключ NYT API."
    }

    fun toUserMessage(throwable: Throwable): String {
        val httpException = throwable.findCause<HttpException>()
        if (httpException != null) {
            return when (httpException.code()) {
                400, 401, 403 -> {
                    "NYT отклонил ключ API. Проверь, что ключ активен и привязан к Top Stories API."
                }
                429 -> "NYT временно ограничил количество запросов. Попробуй обновить новости позже."
                in 500..599 -> "Сервис NYT временно недоступен. Попробуй обновить новости позже."
                else -> "Не удалось загрузить новости. Попробуй обновить ленту позже."
            }
        }

        return when {
            throwable.findCause<UnknownHostException>() != null -> {
                "Нет подключения к интернету. Проверь сеть и попробуй ещё раз."
            }
            throwable.findCause<SocketTimeoutException>() != null -> {
                "NYT слишком долго отвечает. Попробуй обновить новости ещё раз."
            }
            throwable.findCause<IOException>() != null -> {
                "Не удалось подключиться к NYT. Проверь интернет и попробуй снова."
            }
            else -> "Не удалось загрузить новости. Попробуй обновить ленту позже."
        }
    }
}

private inline fun <reified T : Throwable> Throwable.findCause(): T? {
    var current: Throwable? = this
    while (current != null) {
        if (current is T) {
            return current
        }
        current = current.cause
    }
    return null
}
