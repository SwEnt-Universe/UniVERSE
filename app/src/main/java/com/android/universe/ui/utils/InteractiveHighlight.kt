package com.android.universe.ui.utils

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastCoerceIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Original source:
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/master/catalog/src/main/java/com/kyant/backdrop/catalog/utils/InteractiveHighlight.kt
 * Date taken: 2025-11-13
 *
 * Description: This file was originally created by Kyant0 Minor modifications were made for
 * integration into UniVERSE
 */

/**
 * A class that encapsulates the logic for an interactive press-and-drag highlight effect.
 *
 * It manages the animation state and provides two modifiers:
 * 1. [modifier]: The drawing modifier that renders the highlight.
 * 2. [gestureModifier]: The pointer input modifier that detects gestures to drive the effect.
 *
 * These should be applied to the same composable.
 *
 * @param animationScope The [kotlinx.coroutines.CoroutineScope] used to launch animations for press, release, and
 *   drag.
 * @param position A lambda function to calculate the final highlight position. It receives the
 *   [androidx.compose.ui.geometry.Size] of the composable and the current drag [androidx.compose.ui.geometry.Offset] and returns the [androidx.compose.ui.geometry.Offset] where the
 *   highlight should be centered.
 */
class InteractiveHighlight(
    val animationScope: CoroutineScope,
    val position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset }
) {

  /** Spring specification for the press/release animation (controls alpha/intensity). */
  private val pressProgressAnimationSpec = spring(0.5f, 300f, 0.001f)

  /** Spring specification for the position animation on release (snaps back to start). */
  private val positionAnimationSpec = spring(0.5f, 300f, Offset.Companion.VisibilityThreshold)

  /** Manages the animation for the highlight's intensity (0.0f to 1.0f). */
  private val pressProgressAnimation = Animatable(0f, 0.001f)

  /** Manages the animation for the highlight's position during drag. */
  private val positionAnimation =
      Animatable(
          Offset.Companion.Zero,
          Offset.Companion.VectorConverter,
          Offset.Companion.VisibilityThreshold
      )

  /** Stores the initial pointer down position to calculate drag offset. */
  private var startPosition = Offset.Companion.Zero

  /** The current progress of the press animation, from 0.0 (released) to 1.0 (pressed). */
  val pressProgress: Float
    get() = pressProgressAnimation.value

  /** The current drag offset from the initial [startPosition]. */
  val offset: Offset
    get() = positionAnimation.value - startPosition

  /**
   * The [android.graphics.RuntimeShader] used for the advanced highlight effect on API 33+. It creates a smooth
   * radial gradient.
   */
  private val shader =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          RuntimeShader(
              """
uniform float2 size;
layout(color) uniform half4 color;
uniform float radius;
uniform float2 position;

half4 main(float2 coord) {
    float dist = distance(coord, position);
    float intensity = smoothstep(radius, radius * 0.5, dist);
    return color * intensity;
}"""
          )
      } else {
        null
      }

  /**
   * The [androidx.compose.ui.Modifier] responsible for drawing the highlight effect. It draws a radial gradient via
   * [RuntimeShader] on API 33+ or a simple translucent [androidx.compose.ui.graphics.Color.Companion.White] rectangle as a fallback. The
   * effect is drawn with [androidx.compose.ui.graphics.BlendMode.Companion.Plus] to create an additive light effect.
   */
  val modifier: Modifier =
      Modifier.Companion.drawWithContent {
        val progress = pressProgressAnimation.value
        if (progress > 0f) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null) {
            // Base layer of highlight
            drawRect(Color.Companion.White.copy(0.08f * progress), blendMode = BlendMode.Companion.Plus)

            // Configure and draw the shader-based highlight
            shader.apply {
              val position = position(size, positionAnimation.value)
              setFloatUniform("size", size.width, size.height)
              setColorUniform("color", Color.Companion.White.copy(0.15f * progress).toArgb())
              setFloatUniform("radius", size.minDimension * 1.5f)
              setFloatUniform(
                  "position",
                  position.x.fastCoerceIn(0f, size.width),
                  position.y.fastCoerceIn(0f, size.height))
            }
            drawRect(ShaderBrush(shader), blendMode = BlendMode.Companion.Plus)
          } else {
            // Fallback for older Android versions
            drawRect(Color.Companion.White.copy(0.25f * progress), blendMode = BlendMode.Companion.Plus)
          }
        }

        drawContent()
      }

  /**
   * The [Modifier] responsible for capturing pointer input. It uses [inspectDragGestures] to:
   * - On onDragStart: Snap the highlight position and start animating [pressProgress] to 1f.
   * - On onDragEnd / onDragCancel: Animate [pressProgress] to 0f and animate the position back.
   * - On onDrag: Snap the highlight [positionAnimation] to the new pointer position.
   */
  val gestureModifier: Modifier =
      Modifier.Companion.pointerInput(animationScope) {
        inspectDragGestures(
            onDragStart = { down ->
              startPosition = down.position
              animationScope.launch {
                launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
                launch { positionAnimation.snapTo(startPosition) }
              }
            },
            onDragEnd = {
              animationScope.launch {
                launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
                launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
              }
            },
            onDragCancel = {
              animationScope.launch {
                launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
                launch { positionAnimation.animateTo(startPosition, positionAnimationSpec) }
              }
            }) { change, _ ->
              animationScope.launch { positionAnimation.snapTo(change.position) }
            }
      }
}