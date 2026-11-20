package dev.hamper.tabula.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider as DayNight
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.hamper.tabula.MainActivity
import dev.hamper.tabula.data.dayWord
import dev.hamper.tabula.data.daysTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.hamper.tabula.store

// the app's look of 18. 11. (ui/Smer.kt C+, ui/Theme.kt Tmavy): its paper and ink, the deep green, the dark page
private val Bg = DayNight(day = Color(0xFFF3EEE3), night = Color(0xFF232B28))
private val Ink = DayNight(day = Color(0xFF161512), night = Color(0xFFEEEBE3))
private val Dim = DayNight(day = Color(0xFF5D564C), night = Color(0xFFB3BAB4))
private val Cta = DayNight(day = Color(0xFF1C6B4E), night = Color(0xFFD9E8DF))
private val OnCta = DayNight(day = Color(0xFFFFFDF8), night = Color(0xFF1E3A30))
private val Tint = DayNight(day = Color(0xFFD6EADF), night = Color(0xFF24382F))

private fun route(ctx: Context, r: String) = Intent(Intent.ACTION_VIEW, Uri.parse("tabula://$r"), ctx, MainActivity::class.java)

class TabulaWidget : GlanceAppWidget() {
    private var done = 0
    private var next = ""
    private var course = ""
    private var word = "testu"
    private var window = false
    private var over = false
    private var short = ""
    private var total = 0
    override val sizeMode = SizeMode.Responsive(setOf(DpSize(120.dp, 120.dp), DpSize(250.dp, 120.dp)))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = dev.hamper.tabula.Tabula.store(context)
        val days = daysTo(store.testDay())
        done = store.course.shown().testLessons.count { it.id in store.lessonsDone }
        total = store.course.shown().testLessons.size
        course = store.course.short
        word = store.course.test.word.gen
        // an exam without a day yet (LA1's skúška before its KOS terms): the count is to the exam period, not to the exam
        window = store.testWindow() != null
        over = store.testOver()
        short = store.course.test.short
        next = dev.hamper.tabula.screens.nextLesson(store)?.let { "${it.name}, ${it.minutes} min" } ?: "Nácvik ${store.course.test.word.gen}, ${store.course.test.minutes ?: 70} min"
        provideContent { GlanceTheme { Body(context, days) } }
    }

    @Composable
    private fun Body(ctx: Context, days: Int) {
        val wide = LocalSize.current.width >= 240.dp
        Box(GlanceModifier.fillMaxSize().background(Bg).cornerRadius(26.dp).clickable(actionStartActivity(route(ctx, "dnes"))).padding(16.dp)) {
            if (wide) Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Column(GlanceModifier.defaultWeight()) { Count(days) }
                Spacer(GlanceModifier.width(12.dp))
                Column(GlanceModifier.defaultWeight(), verticalAlignment = Alignment.CenterVertically) { Next(ctx) }
            } else Column(GlanceModifier.fillMaxSize()) {
                Count(days)
                Spacer(GlanceModifier.defaultWeight())
                Text(next, style = TextStyle(color = Dim, fontSize = 13.sp), maxLines = 1)
            }
        }
    }

    @Composable
    private fun Count(days: Int) {
        // in the exam period itself (no day picked) there is nothing to count down to
        Text(when { over -> "Za tebou"; window && days <= 0 -> "skúškové"; else -> "$days ${dayWord(days)}" }, style = TextStyle(color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold), maxLines = 1)
        Text(when { over -> "$short z $course"; window && days <= 0 -> "termín v KOS ($course)"; window -> "do skúškového ($course)"; else -> "do $word z $course" }, style = TextStyle(color = Dim, fontSize = 13.sp), maxLines = 1)
        Spacer(GlanceModifier.height(10.dp))
        Row(GlanceModifier.fillMaxWidth()) {
            // One cell each: a Glance row holds ten children at most.
            // more lessons than cells: each cell stands for a share of them
            val cells = total.coerceIn(1, 10)
            val filled = if (total == 0) 0 else done * cells / total
            for (i in 0 until cells) {
                Box(GlanceModifier.defaultWeight().padding(end = if (i < cells - 1) 3.dp else 0.dp)) {
                    Box(GlanceModifier.fillMaxWidth().height(6.dp).background(if (i < filled) Cta else Tint).cornerRadius(3.dp)) {}
                }
            }
        }
    }

    @Composable
    private fun Next(ctx: Context) {
        Text("Dnes", style = TextStyle(color = Dim, fontSize = 13.sp))
        Text(next, style = TextStyle(color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium), maxLines = 2)
        Spacer(GlanceModifier.height(8.dp))
        Box(
            GlanceModifier.fillMaxWidth().height(48.dp).background(Cta).cornerRadius(16.dp).clickable(actionStartActivity(route(ctx, "lesson"))),
            contentAlignment = Alignment.Center,
        ) { Text("Začať", style = TextStyle(color = OnCta, fontSize = 15.sp, fontWeight = FontWeight.Bold)) }
    }
}

class TabulaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TabulaWidget()
    companion object {
        fun refresh(ctx: Context) { CoroutineScope(Dispatchers.Default).launch { TabulaWidget().updateAll(ctx) } }
    }
}
