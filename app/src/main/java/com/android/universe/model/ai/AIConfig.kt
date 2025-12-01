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

  // Maximum event count per request
  const val MAX_EVENT_PER_REQUEST = 3

  // Minimum delay between passive AI generation triggers
  const val REQUEST_COOLDOWN: Long = 60_000L // x_000L = x seconds

  // Minimum allowed distance between two auto-generated events (km)
  const val MIN_EVENT_SPACING_KM = 0.5

  // Max allowed viewport radius (km) for triggering passive generation
  const val MAX_VIEWPORT_RADIUS_KM = 4.0
}
