package com.pesoflow.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesoflow.app.ui.theme.ElevatedCard
import com.pesoflow.app.ui.theme.ObsidianBg
import com.pesoflow.app.ui.theme.SageBright
import com.pesoflow.app.ui.theme.SageContainer
import com.pesoflow.app.ui.theme.SagePrimary
import com.pesoflow.app.ui.theme.SubtleBorder

/**
 * Custom minimalist geometric Peso + Sage Pulse vector brand mark.
 */
@Composable
fun PesoFlowLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.44f

        // Inner dark sage circle
        drawCircle(
            color = SageContainer,
            radius = radius,
            center = center
        )

        // Outer hairline sage ring
        drawCircle(
            color = SagePrimary,
            radius = radius,
            center = center,
            style = Stroke(width = w * 0.055f)
        )

        // Minimalist geometric 'P' stem + bowl
        val pPath = Path().apply {
            moveTo(w * 0.38f, h * 0.70f)
            lineTo(w * 0.38f, h * 0.31f)
            lineTo(w * 0.54f, h * 0.31f)
            cubicTo(
                w * 0.66f, h * 0.31f,
                w * 0.66f, h * 0.53f,
                w * 0.54f, h * 0.53f
            )
            lineTo(w * 0.38f, h * 0.53f)
        }
        drawPath(
            path = pPath,
            color = SageBright,
            style = Stroke(
                width = w * 0.085f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Peso double strike bars
        drawLine(
            color = SagePrimary,
            start = Offset(w * 0.31f, h * 0.39f),
            end = Offset(w * 0.68f, h * 0.39f),
            strokeWidth = w * 0.048f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = SagePrimary,
            start = Offset(w * 0.31f, h * 0.46f),
            end = Offset(w * 0.68f, h * 0.46f),
            strokeWidth = w * 0.048f,
            cap = StrokeCap.Round
        )

        // Minimalist sage accent dot
        drawCircle(
            color = SagePrimary,
            radius = w * 0.045f,
            center = Offset(w * 0.60f, h * 0.66f)
        )
    }
}

/**
 * Minimalist card surface with subtle 1dp hairline border and matte finish.
 */
@Composable
fun MinimalistCard(
    modifier: Modifier = Modifier.fillMaxWidth(),
    containerColor: Color = ElevatedCard,
    borderColor: Color = SubtleBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}
