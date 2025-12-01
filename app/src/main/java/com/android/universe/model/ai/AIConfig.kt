package com.android.universe.model.ai

/**
 * Centralized configuration for all AI-driven behavior.
 *
 * This file consolidates:
 * - OpenAI model & generation parameters
 * - Passive AI generation heuristics
 * - Cooldowns & limits
 * - Spatial tuning constants
 *
 * All knobs affecting AI behavior should live here.
 */
object AIConfig {

  /** See [OpenAI pricing](https://platform.openai.com/docs/pricing?utm_source=chatgpt.com) */
  const val AI_MODEL = "gpt-4o"

  // Maximum number of tokens the model may produce
  const val MAX_COMPLETION_TOKENS = 1500

  // Max allowed radius within which events can be generated
  const val MAX_RADIUS_KM = 5
}
