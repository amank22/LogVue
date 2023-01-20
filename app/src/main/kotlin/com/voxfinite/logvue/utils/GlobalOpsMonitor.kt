@file:OptIn(ExperimentalComposeUiApi::class)

package com.voxfinite.logvue.utils

import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.coroutines.flow.MutableStateFlow

val globalOpsFlow = MutableStateFlow<GlobalOp>(GlobalOp.AppStart)

sealed interface GlobalOp {
    object AppStart : GlobalOp
    object StartStream : GlobalOp

    object PauseStream : GlobalOp

}