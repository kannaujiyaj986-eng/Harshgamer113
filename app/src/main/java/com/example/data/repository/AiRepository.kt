package com.example.data.repository

import kotlinx.coroutines.delay

/**
 * Abstraction for Harshgamer113 AI Studio generation.
 * Live Gemini inference requires Firebase AI Logic; when Firebase is not enabled,
 * [PlaceholderAiRepository] supplies clearly labeled sample creator responses.
 */
interface AiRepository {
    val isLiveGeminiEnabled: Boolean
    suspend fun generateStudioIdeas(prompt: String, category: String): Result<String>
}

class PlaceholderAiRepository : AiRepository {
    override val isLiveGeminiEnabled: Boolean = false

    override suspend fun generateStudioIdeas(prompt: String, category: String): Result<String> {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a prompt or pick a quick preset."))
        }
        delay(350)
        val sampleBody = when (category) {
            "Video Titles" -> buildString {
                appendLine("[Sample Placeholder Response — Enable Firebase AI Logic for Live Gemini]")
                appendLine()
                appendLine("Topic: \"$trimmed\"")
                appendLine("1. \"ZERO RECOIL 4-FINGER GYRO! New Season God-Mode Settings | Harshgamer113\"")
                appendLine("2. \"1v4 CLUTCH at Final Zone! Why Everyone Copies This Sensitivity\"")
                appendLine("3. \"I Tested Pro Custom Scrims at 120 FPS — Insane Comeback!\"")
            }
            "Training Drill" -> buildString {
                appendLine("[Sample Placeholder Response — Enable Firebase AI Logic for Live Gemini]")
                appendLine()
                appendLine("Focus: \"$trimmed\"")
                appendLine("• Warmup (5 min): 40m spray transfer between 3 targets without attachments.")
                appendLine("• Close Range (8 min): Crouch-jiggle hipfire tracking + pre-aim head height.")
                appendLine("• Match Ready (5 min): 1 TDM Warehouse match focusing on quick peek timing.")
            }
            "Thumbnail Hook" -> buildString {
                appendLine("[Sample Placeholder Response — Enable Firebase AI Logic for Live Gemini]")
                appendLine()
                appendLine("Concept: \"$trimmed\"")
                appendLine("• Top Badge: \"NEW CODE 2026\"")
                appendLine("• Bold Text (2 words max): \"LASER AIM!\"")
                appendLine("• Visual: Cyan rim light + Red-dot crosshair zoom on target.")
            }
            else -> buildString {
                appendLine("[Sample Placeholder Response — Enable Firebase AI Logic for Live Gemini]")
                appendLine()
                appendLine("Prompt: \"$trimmed\"")
                appendLine("• Drop Strategy: Secure high-ground compound first and designate 1 scout.")
                appendLine("• Loadout Pick: Primary AR (3x Gyro) + Close-range SMG/Shotgun.")
                appendLine("• Content Hook: Highlight the key rotation callout in the first 8 seconds.")
            }
        }
        return Result.success(sampleBody.trim())
    }
}
