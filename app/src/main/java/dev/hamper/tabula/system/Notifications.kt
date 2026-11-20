package dev.hamper.tabula.system

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import dev.hamper.tabula.MainActivity
import dev.hamper.tabula.R
import dev.hamper.tabula.data.dayWord
import dev.hamper.tabula.data.daysTo
import dev.hamper.tabula.compat.time.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import dev.hamper.tabula.store

object Notifications {
    const val DAILY = "daily"
    const val MOCK = "mock"
    const val ID_DAILY = 11
    const val ID_MOCK = 12
    private const val INK = 0xFF357265.toInt()

    fun channels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(DAILY, "Denná pripomienka", NotificationManager.IMPORTANCE_DEFAULT))
        nm.createNotificationChannel(NotificationChannel(MOCK, "Nácvik testu", NotificationManager.IMPORTANCE_DEFAULT).apply { setShowBadge(false) })
    }

    fun open(ctx: Context, route: String, code: Int = route.hashCode()): PendingIntent = PendingIntent.getActivity(
        ctx, code,
        Intent(Intent.ACTION_VIEW, Uri.parse("tabula://$route"), ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    /** Daily nudge with two real actions. Fired from settings; there is no scheduler in the prototype. */
    private fun nextText(ctx: Context) = dev.hamper.tabula.screens.nextLesson(dev.hamper.tabula.Tabula.store(ctx))?.let { "${it.name}, ${it.minutes} min" } ?: "nácvik ${dev.hamper.tabula.Tabula.store(ctx).course.test.word.gen}"

    fun reminder(ctx: Context) {
        val later = PendingIntent.getBroadcast(ctx, 1, Intent(ctx, NotificationActionReceiver::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, DAILY)
            .setSmallIcon(R.drawable.ic_stat)
            .setColor(INK)
            .setContentTitle(run {
                val st = dev.hamper.tabula.Tabula.store(ctx)
                // daysTo clamps a past day to 0: "je dnes" only when the day really is today
                val raw = dev.hamper.tabula.data.daysUntil(st.testDay()); val d = raw.coerceAtLeast(0)
                val window = st.testWindow()
                when {
                    // an exam without a day yet: the days are to the exam period, not to the exam
                    window != null && raw > 0 -> "${st.course.short}: do skúškového $d ${dayWord(d)}"
                    window != null && !st.testOver() -> "${st.course.short}: ${st.course.test.short} v skúškovom do ${window.second.format(dev.hamper.tabula.data.DayFmt)}"
                    raw < 0 || st.testOver() -> "${st.course.short}: ${st.course.test.short} je za tebou"
                    d == 0 -> "${st.course.short}: ${st.course.test.short} je dnes"
                    else -> "${st.course.short}: do ${st.course.test.word.gen} $d ${dayWord(d)}"
                }
            })
            .setContentText("Dnes: ${nextText(ctx)}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Dnes: ${nextText(ctx)}. Potom príklad z ${dev.hamper.tabula.Tabula.store(ctx).course.test.word.gen} na papier."))
            .setContentIntent(open(ctx, "dnes"))
            .addAction(0, "Začať lekciu", open(ctx, "lesson"))
            .addAction(0, "Neskôr", later)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(ID_DAILY, n)
    }

    /**
     * Android 16 Live Update: ProgressStyle plus a request to be promoted, so the countdown
     * sits in a status-bar chip and on the lock screen. Older systems get a plain ongoing one.
     */
    fun mock(ctx: Context, end: Long): Notification {
        val leftMs = (end - SystemClock.elapsedRealtime()).coerceAtLeast(0)
        val paper = dev.hamper.tabula.Tabula.mockPaper
        val total = (paper?.let { dev.hamper.tabula.screens.paperMinutes(it) } ?: 70) * 60
        val elapsed = total - (leftMs / 1000).toInt()
        val wall = System.currentTimeMillis() + leftMs
        val title = "Nácvik " + (paper?.let { dev.hamper.tabula.screens.paperExam(dev.hamper.tabula.screens.paperById(it)).word.gen } ?: "testu")
        if (Build.VERSION.SDK_INT >= 36) {
            // Segments split the time by the problems' minutes, so the bar doubles as a pacing guide.
            val colors = listOf(0xFF357265.toInt(), 0xFFBF8420.toInt(), 0xFF5A5E98.toInt())
            val probs = paper?.let { dev.hamper.tabula.screens.paperById(it).problems }.orEmpty()
            val mins = probs.map { it.minutes }.takeIf { it.isNotEmpty() && it.sum() > 0 } ?: listOf(16, 28, 26)
            val style = Notification.ProgressStyle()
                .setProgressSegments(mins.mapIndexed { i, m -> Notification.ProgressStyle.Segment((m * total / mins.sum()).coerceAtLeast(1)).setColor(colors[i % colors.size]) })
                .setProgress(elapsed)
                .setStyledByProgress(true)
            return Notification.Builder(ctx, MOCK)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(title)
                .setContentText("Píš na papier, čas beží")
                .setStyle(style)
                .setOngoing(true)
                .setWhen(wall).setShowWhen(true).setUsesChronometer(true).setChronometerCountDown(true)
                .setColor(INK)
                .setContentIntent(open(ctx, "mockrun"))
                .addExtras(android.os.Bundle().apply { putBoolean("android.requestPromotedOngoing", true) })
                .addAction(Notification.Action.Builder(null, "Odovzdať", open(ctx, "grade?mock=stop")).build())
                .build()
        }
        return NotificationCompat.Builder(ctx, MOCK)
            .setSmallIcon(R.drawable.ic_stat).setContentTitle(title).setContentText("Píš na papier, čas beží")
            .setOngoing(true).setWhen(wall).setShowWhen(true).setUsesChronometer(true).setChronometerCountDown(true)
            .setProgress(total, elapsed, false).setContentIntent(open(ctx, "mockrun")).build()
    }
}

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationManagerCompat.from(context).cancel(Notifications.ID_DAILY)
    }
}

/** Keeps the countdown alive and its notification current while the app is in the background. */
class MockTimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var end = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf(); return START_NOT_STICKY }
        end = intent?.getLongExtra(END, 0L) ?: 0L
        ServiceCompat.startForeground(this, Notifications.ID_MOCK, Notifications.mock(this, end), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        scope.launch {
            while (isActive && SystemClock.elapsedRealtime() < end) {
                delay(30_000)
                getSystemService(android.app.NotificationManager::class.java).notify(Notifications.ID_MOCK, Notifications.mock(this@MockTimerService, end))
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }

    companion object {
        private const val END = "end"
        private const val STOP = "stop"
        fun start(ctx: Context, end: Long) { ctx.startForegroundService(Intent(ctx, MockTimerService::class.java).putExtra(END, end)) }
        fun stop(ctx: Context) { ctx.startService(Intent(ctx, MockTimerService::class.java).setAction(STOP)) }
    }
}
