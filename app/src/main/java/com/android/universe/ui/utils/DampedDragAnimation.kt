package com.android.universe.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Original source:
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/master/catalog/src/main/java/com/kyant/backdrop/catalog/utils/DampedDragAnimation.kt
 * Date taken: 2025-11-13
 *
 * Description: This file was originally created by Kyant0 Minor modifications were made for
 * integration into UniVERSE
 */

/**
 * A state holder class that manages the logic and animation for a damped, interactive drag gesture.
 *
 * This class handles multiple asynchronous animations simultaneously:
 * 1. The main drag [value]
 * 2. The visual scaling ([scaleX], [scaleY]) used for press/release feedback
 * 3. An internal [velocity] calculation to simulate inertia.
 *
 * It provides a [modifier] that should be attached to the Composable to enable drag detection.
 *
 * @param animationScope The [kotlinx.coroutines.CoroutineScope] used to run all animations.
 *   Typically, this is `rememberCoroutineScope()`.
 * @param initialValue The starting float value of the drag (e.g., 0f).
 * @param valueRange The allowed range for the main drag value. Dragging will be clamped to this
 *   range.
 * @param visibilityThreshold The threshold used for the value animation spring, determining when
 *   the animation is considered finished.
 * @param initialScale The resting scale value for the animated content (e.g., 1.0f).
 * @param pressedScale The scale value when the content is actively being pressed or dragged (e.g.,
 *   0.9f).
 * @param onDragStarted Lambda invoked when a drag gesture begins, providing the initial touch
 *   position.
 * @param onDragStopped Lambda invoked when the drag gesture ends (either by lift-off or
 *   cancellation).
 * @param onDrag Lambda invoked during the drag gesture, providing the size of the Composable and
 *   the change in drag amount.
 */
class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
) {

  private val valueAnimationSpec = spring(1f, 1000f, visibilityThreshold)
  private val velocityAnimationSpec = spring(0.5f, 300f, visibilityThreshold * 10f)
  private val pressProgressAnimationSpec = spring(1f, 1000f, 0.001f)
  private val scaleXAnimationSpec = spring(0.6f, 250f, 0.001f)
  private val scaleYAnimationSpec = spring(0.7f, 250f, 0.001f)

  private val valueAnimation = Animatable(initialValue, visibilityThreshold)
  private val velocityAnimation = Animatable(0f, 5f)
  private val pressProgressAnimation = Animatable(0f, 0.001f)
  private val scaleXAnimation = Animatable(initialScale, 0.001f)
  private val scaleYAnimation = Animatable(initialScale, 0.001f)

  private val mutatorMutex = MutatorMutex()

  private val velocityTracker = VelocityTracker()

  /** The current animated value of the drag position, clamped within [valueRange]. */
  val value: Float
    get() = valueAnimation.value

  /** The current progress of the drag value as a ratio between 0.0f and 1.0f. */
  val progress: Float
    get() = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)

  /** The target value the main drag animation is currently moving towards. */
  val targetValue: Float
    get() = valueAnimation.targetValue

  /** The current animation progress (0.0f to 1.0f) for the press/release state. */
  val pressProgress: Float
    get() = pressProgressAnimation.value

  /** The current animated scale factor applied to the X-axis (for visual feedback). */
  val scaleX: Float
    get() = scaleXAnimation.value

  /** The current animated scale factor applied to the Y-axis (for visual feedback). */
  val scaleY: Float
    get() = scaleYAnimation.value

  /** The current animated velocity calculated from the drag input. */
  val velocity: Float
    get() = velocityAnimation.value

  /**
   * The [androidx.compose.ui.Modifier] that should be attached to the Composable that needs to
   * respond to the damped drag. This handles all pointer input events (start, end, cancel, drag)
   * and triggers the corresponding animation and lifecycle functions.
   */
  val modifier: Modifier =
      Modifier.Companion.pointerInput(Unit) {
        inspectDragGestures(
            onDragStart = { down ->
              onDragStarted(down.position)
              press()
            },
            onDragEnd = {
              onDragStopped()
              release()
            },
            onDragCancel = {
              onDragStopped()
              release()
            }) { _, dragAmount ->
              onDrag(size, dragAmount)
            }
      }

  /**
   * Starts the "press" animation cycle, instantly resetting the velocity tracker and animating
   * [pressProgress], [scaleX], and [scaleY] towards their 'pressed' targets.
   */
  fun press() {
    velocityTracker.resetTracking()
    animationScope.launch {
      launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
      launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
      launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
    }
  }

  /**
   * Starts the "release" animation cycle, animating [pressProgress], [scaleX], and [scaleY] back to
   * their 'initial' resting values.
   *
   * It also includes a mechanism to wait for the main [value] to settle near its target before
   * continuing the release of the visual elements, ensuring smooth damping.
   */
  fun release() {
    animationScope.launch {
      awaitFrame()
      if (value != targetValue) {
        val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
        snapshotFlow { valueAnimation.value }
            .filter { abs(it - valueAnimation.targetValue) < threshold }
            .first()
      }
      launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
      launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
      launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
    }
  }

  /**
   * Updates the current [value] and simultaneously animates towards it using the defined
   * [valueAnimationSpec]. This is typically called from the `onDrag` handler during an active drag
   * gesture.
   *
   * @param value The new drag value to move towards, which will be clamped to [valueRange].
   */
  fun updateValue(value: Float) {
    val targetValue = value.coerceIn(valueRange)
    animationScope.launch {
      launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) { updateVelocity() } }
    }
  }

  /**
   * Animates the main drag value to a final target. This is used to programmatically move the
   * animation, typically in response to a fling or snap-to-position logic.
   *
   * It ensures that other concurrent animations are cancelled using [mutatorMutex] and also
   * temporarily runs the [press] and [release] cycles for visual feedback.
   *
   * @param value The final value to animate to, clamped to [valueRange].
   */
  fun animateToValue(value: Float) {
    animationScope.launch {
      mutatorMutex.mutate {
        press()
        val targetValue = value.coerceIn(valueRange)
        launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) }
        if (velocity != 0f) {
          launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
        }
        release()
      }
    }
  }

  /**
   * Internal function to update the internal [velocityTracker] and animate the [velocity] property.
   * This is crucial for calculating fling/inertia effects.
   */
  private fun updateVelocity() {
    velocityTracker.addPosition(System.currentTimeMillis(), Offset(value, 0f))
    val targetVelocity =
        velocityTracker.calculateVelocity().x / (valueRange.endInclusive - valueRange.start)
    animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
  }
}
