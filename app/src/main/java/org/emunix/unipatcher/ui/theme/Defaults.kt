package org.emunix.unipatcher.ui.theme

import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val MaxContentWidthDp = 840

val MaxContentWidth: Dp = MaxContentWidthDp.dp

fun Modifier.maxContentWidth(): Modifier =
    widthIn(max = MaxContentWidth)