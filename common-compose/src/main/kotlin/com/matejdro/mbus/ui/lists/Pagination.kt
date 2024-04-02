package com.matejdro.mbus.ui.lists

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow

@Composable
fun LazyListState.DetectScrolledToBottom(callback: () -> Unit) {
   LaunchedEffect(this, callback) {
      snapshotFlow {
         val atBottom = layoutInfo.visibleItemsInfo.lastOrNull()?.index == (layoutInfo.totalItemsCount - 1)
         layoutInfo.totalItemsCount to atBottom
      }.collect { (_, atBottom) ->
         if (atBottom) {
            callback()
         }
      }
   }
}
