package com.example.config_and_utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout

fun configureHttpClient() = HttpClient(CIO) {
    install(HttpTimeout) {
        requestTimeoutMillis = 90_000
        connectTimeoutMillis = 60_000
        socketTimeoutMillis = 90_000
    }
}