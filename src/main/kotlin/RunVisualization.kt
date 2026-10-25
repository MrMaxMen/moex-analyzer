package com.example

import java.io.File

fun runVisualization(dataDir: File): Boolean {
    val home = File(Core::class.java.protectionDomain.codeSource.location.toURI())
        .parentFile.parentFile
    val script = File(home, "visualization/main.py")
    if (!script.exists()) {
        println("Visualization script not found: ${script.absolutePath}")
        return false
    }
    val python = listOf("python", "py", "python3").firstOrNull(::canRun)
        ?: run { println("Python not found. CSV files are saved, graphs skipped."); return false }

    val exit = ProcessBuilder(python, script.absolutePath, "--data-dir", dataDir.absolutePath)
        .inheritIO().start().waitFor()
    if (exit != 0) println("Visualization failed (exit code $exit). CSV files are saved.")
    return exit == 0
}

private fun canRun(cmd: String) = runCatching {
    ProcessBuilder(cmd, "--version").redirectErrorStream(true).start().waitFor() == 0
}.getOrDefault(false)