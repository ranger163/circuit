// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.star.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.star.ui.rememberStableCoroutineScope
import kotlinx.coroutines.launch

class BottomSheetOverlay<Model : Any, Result : Any>(
  private val model: Model,
  private val dismissOnTapOutside: Boolean = true,
  private val onDismiss: (() -> Result)? = null,
  private val content: @Composable (Model, OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {
  @Composable
  override fun Content(navigator: OverlayNavigator<Result>) {
    val sheetState =
      rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = {
          if (dismissOnTapOutside) {
            if (it == ModalBottomSheetValue.Hidden) {
              // This is apparently as close as we can get to an "onDismiss" callback, which
              // unfortunately has no animation
              val result = onDismiss?.invoke() ?: error("no result!")
              navigator.finish(result)
            }
            true
          } else {
            false
          }
        }
      )
    ModalBottomSheetLayout(
      modifier = Modifier.fillMaxSize(),
      sheetContent = {
        // If this all looks dumb, it's because it is.
        // https://github.com/google/accompanist/issues/910
        Box(Modifier.padding(32.dp)) {
          @Suppress("MagicNumber") Box(Modifier.fillMaxSize(0.51f))
          // Delay setting the result until we've finished dismissing
          val coroutineScope = rememberStableCoroutineScope()
          content(model) { result ->
            // This is the OverlayNavigator.finish() callback
            coroutineScope.launch {
              sheetState.hide()
              navigator.finish(result)
            }
          }
        }
      },
      sheetState = sheetState,
      sheetShape = RoundedCornerShape(32.dp)
    ) {
      // Nothing here, left to the existing content
    }
    LaunchedEffect(sheetState) { sheetState.show() }
  }
}
