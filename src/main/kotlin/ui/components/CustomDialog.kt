package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.UndecoratedWindowAlertDialogProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.google.common.primitives.Floats

@Composable
fun StyledCustomVerticalDialog(onDismissRequest: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    CustomDialog(
        dialogWidthRatio = 0.28f,
        dialogHeightRatio = 0.56f, onDismissRequest = onDismissRequest
    ) {
        Box {
            val painter = painterResource("icons/layered_waves.svg")
            Image(painter, "styled", Modifier.fillMaxWidth()
                .graphicsLayer {
                    rotationX = 180f
                    rotationY = 180f
                    translationY = -50f
                }
                .align(Alignment.BottomCenter), Alignment.BottomCenter)
            content()
        }
    }
}

@Suppress("UnstableApiUsage")
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CustomDialog(
    backgroundAlpha: Float = 0.5f,
    dialogWidthRatio: Float = 0.4f,
    dialogHeightRatio: Float = 0.4f,
    dialogShape: Shape = RoundedCornerShape(8.dp),
    dialogElevation: Dp = 8.dp,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Floats.constrainToRange(backgroundAlpha, 0.0f, 1.0f)
    Floats.constrainToRange(dialogWidthRatio, 0.1f, 1.0f)
    Floats.constrainToRange(dialogHeightRatio, 0.1f, 1.0f)
    with(UndecoratedWindowAlertDialogProvider) {
        AlertDialog(onDismissRequest) {
            Dialog(
                onCloseRequest = onDismissRequest,
                state = rememberDialogState(width = Dp.Unspecified, height = Dp.Unspecified),
                undecorated = true,
                resizable = false,
                transparent = true,
                onKeyEvent = {
                    if (it.key == Key.Escape) {
                        onDismissRequest()
                        true
                    } else {
                        false
                    }
                },
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray.copy(backgroundAlpha))
                        .clickable(MutableInteractionSource(), null) { onDismissRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        Modifier
                            .fillMaxWidth(dialogWidthRatio)
                            .fillMaxHeight(dialogHeightRatio)
                            .clickable(MutableInteractionSource(), null) {}, dialogShape, elevation = dialogElevation
                    ) {
                        content()
                    }
                }
            }
        }
    }
}