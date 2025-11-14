/*
 * Copyright 2024 The AndroidLiquidGlass Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Original source:
 * https://github.com/Kyant0/AndroidLiquidGlass/blob/master/catalog/src/main/java/com/kyant/backdrop/catalog/utils/DragGestureInspector.kt
 * Date taken: 2025-11-13
 *
 * Description: This file was originally created by Kyant0 Minor modifications were made for
 * integration into UniVERSE
 */
package com.android.universe.ui.utils

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.util.fastFirstOrNull

/**
 * Awaits and inspects drag gestures, providing callbacks for different stages of the gesture.
 *
 * This function is an extension on [PointerInputScope] and is intended to be used within a
 * `pointerInput` modifier. It repeatedly waits for a gesture to start (`awaitEachGesture`), awaits
 * the first "down" event, and then tracks the drag motion of that pointer.
 *
 * It provides more granular control than standard `detectDragGestures` by offering distinct
 * callbacks for start, end, and cancellation.
 *
 * @param onDragStart Called when the initial "down" event is detected. Provides the initial
 *   [PointerInputChange].
 * @param onDragEnd Called when the pointer is lifted ("up" event), successfully completing the
 *   drag. Provides the final [PointerInputChange].
 * @param onDragCancel Called if the gesture is interrupted or canceled (e.g., the pointer event is
 *   consumed elsewhere).
 * @param onDrag Called for each drag motion event. Provides the current [PointerInputChange] and
 *   the [Offset] representing the change in position since the last event.
 */
suspend fun PointerInputScope.inspectDragGestures(
    onDragStart: (down: PointerInputChange) -> Unit = {},
    onDragEnd: (change: PointerInputChange) -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
  awaitEachGesture {
    val initialDown = awaitFirstDown(false, PointerEventPass.Initial)

    val down = awaitFirstDown(false)
    val drag = initialDown

    onDragStart(down)
    onDrag(drag, Offset.Zero)
    val upEvent = drag(pointerId = drag.id, onDrag = { onDrag(it, it.positionChange()) })
    if (upEvent == null) {
      onDragCancel()
    } else {
      onDragEnd(upEvent)
    }
  }
}

/**
 * A private helper function that tracks a specific pointer's drag motion.
 *
 * It continuously awaits pointer events, filtering for the specified [pointerId]. It calls [onDrag]
 * for each move event and returns the final "up" event if the drag completes successfully.
 *
 * @param pointerId The [PointerId] to track.
 * @param onDrag A callback invoked for each drag event change.
 * @return The final [PointerInputChange] corresponding to the "up" event, or `null` if the gesture
 *   is canceled, consumed, or the pointer is already up.
 */
private suspend inline fun AwaitPointerEventScope.drag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit
): PointerInputChange? {
  val isPointerUp = currentEvent.changes.fastFirstOrNull { it.id == pointerId }?.pressed != true
  if (isPointerUp) {
    return null
  }
  var pointer = pointerId
  while (true) {
    val change = awaitDragOrUp(pointer) ?: return null
    if (change.isConsumed) {
      return null
    }
    if (change.changedToUpIgnoreConsumed()) {
      return change
    }
    onDrag(change)
    pointer = change.id
  }
}

/**
 * Awaits either a drag event or an "up" event for a specific pointer.
 *
 * This helper function handles multi-touch scenarios where the tracked pointer might go up, but
 * another pointer goes down, transferring the "drag" focus.
 *
 * @param pointerId The [PointerId] to track.
 * @return The [PointerInputChange] for the next relevant drag or "up" event, or `null` if the event
 *   stream ends unexpectedly.
 */
private suspend inline fun AwaitPointerEventScope.awaitDragOrUp(
    pointerId: PointerId
): PointerInputChange? {
  var pointer = pointerId
  while (true) {
    val event = awaitPointerEvent()
    val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
    if (dragEvent.changedToUpIgnoreConsumed()) {
      val otherDown = event.changes.fastFirstOrNull { it.pressed }
      if (otherDown == null) {
        return dragEvent
      } else {
        pointer = otherDown.id
      }
    } else {
      val hasDragged = dragEvent.previousPosition != dragEvent.position
      if (hasDragged) {
        return dragEvent
      }
    }
  }
}
