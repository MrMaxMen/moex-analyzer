package com.example.config_and_utils

object ProgressBar {
    @Synchronized
    fun draw(done: Int, total: Int, length: Int = 40) {
        val filled = done * length / total
        System.err.print("\r[" + "=".repeat(filled)+">" + " ".repeat(length - filled) + "] $done/$total")
        if (done == total) System.err.println()
    }
}