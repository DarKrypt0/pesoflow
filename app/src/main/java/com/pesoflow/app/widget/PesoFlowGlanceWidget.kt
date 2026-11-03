package com.pesoflow.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pesoflow.app.MainActivity
import com.pesoflow.app.data.local.PesoFlowDatabase
import kotlinx.coroutines.flow.first
import java.util.Locale

private val WidgetBg = Color(0xFF111512)
private val SageAccent = Color(0xFFB7CDA8)
private val TextMain = Color(0xFFF2F5F2)
private val TextDim = Color(0xFF8E9B91)
private val TrackColor = Color(0xFF262E28)

/**
 * Minimal "Remaining budget" home screen widget matching the PesoFlow design.
 */
class PesoFlowGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = PesoFlowDatabase.getInstance(context).budgetDao()
        val envelopes = dao.observeEnvelopes().first()
        val planned = envelopes.sumOf { it.assignedAmount }
        val spent = envelopes.sumOf { it.spentAmount }
        val remaining = (planned - spent).coerceAtLeast(0.0)
        val ratio = if (planned <= 0.0) 0f else (spent / planned).toFloat().coerceIn(0f, 1f)

        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        provideContent {
            WidgetContent(remaining, spent, planned, ratio, openApp)
        }
    }

    @Composable
    private fun WidgetContent(
        remaining: Double,
        spent: Double,
        planned: Double,
        ratio: Float,
        openApp: Intent
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(WidgetBg)
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .clickable(actionStartActivity(openApp))
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PesoFlow",
                    style = TextStyle(
                        color = ColorProvider(SageAccent),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "This month",
                    style = TextStyle(color = ColorProvider(TextDim), fontSize = 12.sp)
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Text(
                text = "Remaining budget",
                style = TextStyle(color = ColorProvider(TextDim), fontSize = 12.sp)
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = peso(remaining),
                style = TextStyle(
                    color = ColorProvider(TextMain),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Normal
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            LinearProgressIndicator(
                progress = ratio,
                color = ColorProvider(SageAccent),
                backgroundColor = ColorProvider(TrackColor),
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .cornerRadius(3.dp)
            )
            Spacer(modifier = GlanceModifier.height(10.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = "${peso(spent)} of ${peso(planned)}",
                    style = TextStyle(color = ColorProvider(TextDim), fontSize = 11.sp),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "${(ratio * 100).toInt()}% spent",
                    style = TextStyle(color = ColorProvider(SageAccent), fontSize = 11.sp)
                )
            }
        }
    }

    private fun peso(v: Double): String =
        if (v % 1.0 == 0.0) String.format(Locale.US, "₱%,.0f", v)
        else String.format(Locale.US, "₱%,.2f", v)
}

class PesoFlowWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PesoFlowGlanceWidget()
}
