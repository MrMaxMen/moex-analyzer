package com.example

import com.example.entities.HistoryRecord
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.time.LocalDate

import java.util.concurrent.atomic.AtomicInteger

class DataService(
    val client: HttpClient,
) {

    var assetsNames = listOf<String>()

    suspend fun fetchAssetsNames() {
        assetsNames = parseTickers(getAssetsList(client))
    }


    suspend fun getFundsFullNames() =
        parseFunds(getAssetsList(client))


    suspend fun fetchAssetsHistory(
        assets: List<String> = assetsNames,
        since : LocalDate,
        parallelism: Int = 30,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): Map<String, Result<List<HistoryRecord>>> = coroutineScope {
        val limiter = Semaphore(parallelism)
        val done = AtomicInteger()
        assets.map { asset ->
            async {
                val result = limiter.withPermit { runCatching { getAssetHistoryFor(client,asset.trim().uppercase(), since) } }
                onProgress(done.incrementAndGet(), assets.size)
                asset to result
            }
        }.awaitAll().toMap()
    }

}