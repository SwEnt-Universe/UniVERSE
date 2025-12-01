package com.android.universe.model.ai.orchestration

import android.R.attr.radius
import com.android.universe.model.ai.AIConfig.MAX_EVENT_PER_REQUEST
import com.android.universe.model.ai.AIConfig.MAX_VIEWPORT_RADIUS_KM
import com.android.universe.model.ai.AIConfig.MIN_EVENT_SPACING_KM
import com.android.universe.model.ai.AIConfig.REQUEST_COOLDOWN
import org.junit.Assert.*
import org.junit.Test

class PassiveAIGenPolicyTest {

  private val policy = PassiveAIGenPolicy()

  // Helper for readability
  private fun eval(radiusKm: Double, numEvents: Int, last: Long, now: Long): Decision =
      policy.evaluate(
          radiusKm = radiusKm, numEvents = numEvents, lastGenTimestamp = last, now = now)

  // -------------------------------------------------------------------------
  // 1. COOLDOWN
  // -------------------------------------------------------------------------
  @Test
  fun `rejects when cooldown is still active`() {
    val now = 10_000L
    val last = now - (REQUEST_COOLDOWN - 1)

    val result = eval(radiusKm = 1.0, numEvents = 0, last = last, now = now)

    assertTrue(result is Decision.Reject)
  }

  // -------------------------------------------------------------------------
  // 2. VIEWPORT TOO BIG
  // -------------------------------------------------------------------------
  @Test
  fun `rejects when radius exceeds maximum allowed`() {
    val result =
        eval(
            radiusKm = MAX_VIEWPORT_RADIUS_KM + 5,
            numEvents = 0,
            last = 0,
            now = REQUEST_COOLDOWN + 1)

    assertTrue(result is Decision.Reject)
  }

  // -------------------------------------------------------------------------
  // 3. ENOUGH EVENTS ALREADY
  // -------------------------------------------------------------------------
  @Test
  fun `rejects when numEvents meets computed threshold`() {
    // threshold ≈ (R / d)^2
    val radius = MIN_EVENT_SPACING_KM
    val threshold = 1 // roughly (1/1)^2

    val result =
        eval(radiusKm = radius, numEvents = threshold, last = 0, now = REQUEST_COOLDOWN + 1)

    assertTrue(result is Decision.Reject)
  }

  // -------------------------------------------------------------------------
  // 4. ACCEPT — normal case
  // -------------------------------------------------------------------------
  @Test
  fun `accepts when deficit positive and below cap`() {
    val radius = MIN_EVENT_SPACING_KM * 3 // threshold ≈ 9

    val result = eval(radiusKm = radius, numEvents = 0, last = 0, now = REQUEST_COOLDOWN + 1)

    assertTrue(result is Decision.Accept)
    val accept = result as Decision.Accept

    assertTrue(accept.eventsToGenerate in 1..MAX_EVENT_PER_REQUEST)
  }

  // -------------------------------------------------------------------------
  // 5. ACCEPT — capped by MAX_EVENT_PER_REQUEST
  // -------------------------------------------------------------------------
  @Test
  fun `accepts and caps number of events to MAX_EVENT_PER_REQUEST`() {
    val result =
        eval(radiusKm = MAX_VIEWPORT_RADIUS_KM, numEvents = 0, last = 0, now = REQUEST_COOLDOWN + 1)

    assertTrue(result is Decision.Accept)
    val accept = result as Decision.Accept

    assertEquals(MAX_EVENT_PER_REQUEST, accept.eventsToGenerate)
  }
}
