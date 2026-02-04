package com.matejdro.mbus.navigation.keys

import androidx.activity.BackEventCompat
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.TransformOrigin
import com.matejdro.mbus.navigation.animation.PredictiveBackFadeAnimationSpec
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.simplestack.StateChangeResult

abstract class BaseScreenKey : ScreenKey() {
   override fun backAnimation(scope: AnimatedContentTransitionScope<StateChangeResult>): ContentTransform {
      val scaleTransformOrigin = when (scope.targetState.backSwipeEdge) {
         BackEventCompat.EDGE_LEFT -> TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
         BackEventCompat.EDGE_RIGHT -> TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
         else -> TransformOrigin.Center
      }

      return (
         fadeIn(PredictiveBackFadeAnimationSpec()) +
            scaleIn(initialScale = 1.1f, transformOrigin = scaleTransformOrigin)
         ) togetherWith
         (
            fadeOut(PredictiveBackFadeAnimationSpec()) +
               scaleOut(targetScale = 0.9f, transformOrigin = scaleTransformOrigin)
            )
   }
}
