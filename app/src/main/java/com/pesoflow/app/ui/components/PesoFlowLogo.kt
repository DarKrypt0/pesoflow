package com.pesoflow.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pesoflow.app.ui.theme.ElevatedCard
import com.pesoflow.app.ui.theme.SubtleBorder

/**
 * Exact recreation of the Figma PesoFlow squircle logo:
 * Dark forest-obsidian rounded squircle (#1C221E) with the pale sage (#D4E0CD)
 * Peso 'P' whose vertical stem flows into a wave at the bottom.
 */
@Composable
fun PesoFlowLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Color(0xFF1C221E)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokeColor = Color(0xFFD4E0CD)
            val mainStroke = w * 0.082f

            // 'P' loop (top bar + right curve + middle return)
            val loopPath = Path().apply {
                moveTo(w * 0.34f, h * 0.24f)
                lineTo(w * 0.56f, h * 0.24f)
                cubicTo(
                    w * 0.74f, h * 0.24f,
                    w * 0.74f, h * 0.54f,
                    w * 0.56f, h * 0.54f
                )
                lineTo(w * 0.34f, h * 0.54f)
            }
            drawPath(
                path = loopPath,
                color = strokeColor,
                style = Stroke(width = mainStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Vertical stem + flowing bottom wave tail
            val stemWavePath = Path().apply {
                moveTo(w * 0.34f, h * 0.24f)
                lineTo(w * 0.34f, h * 0.77f)
                cubicTo(
                    w * 0.43f, h * 0.77f,
                    w * 0.50f, h * 0.67f,
                    w * 0.60f, h * 0.67f
                )
                lineTo(w * 0.74f, h * 0.67f)
            }
            drawPath(
                path = stemWavePath,
                color = strokeColor,
                style = Stroke(width = mainStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Two horizontal Peso bars extending left of the stem through the loop
            drawLine(
                color = strokeColor,
                start = Offset(w * 0.23f, h * 0.35f),
                end = Offset(w * 0.68f, h * 0.35f),
                strokeWidth = mainStroke * 0.92f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = strokeColor,
                start = Offset(w * 0.23f, h * 0.45f),
                end = Offset(w * 0.68f, h * 0.45f),
                strokeWidth = mainStroke * 0.92f,
                cap = StrokeCap.Round
            )
        }
    }
}

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

