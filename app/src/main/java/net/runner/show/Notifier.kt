package net.runner.show

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class TimedNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: "Reminder message"
        NotificationHelper.showNotification(applicationContext, title, message)
        return Result.success()
    }
}

object NotificationHelper {
    private const val CHANNEL_ID = "timed_notifications"

    fun createNotificationChannel(context:Context) {
        val name ="Dine Notification Channel"
        val descriptionText ="Channel for dine notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify((0..1000).random(), builder.build())
        }
    }
}
fun scheduleNotificationAt(context: Context, hour: Int, minute: Int, title: String, message: String) {
    val now = Calendar.getInstance()
    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }
    if (now.after(targetTime)) {
        targetTime.add(Calendar.DAY_OF_MONTH,1)
    }
    val delay = targetTime.timeInMillis - now.timeInMillis
    val data = workDataOf("title" to title, "message" to message)

    val request = PeriodicWorkRequestBuilder<TimedNotificationWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        title,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}

@Composable
fun ScheduleTimedNotifications(list:MutableList<Meal>) {
    val context = LocalContext.current
    NotificationHelper.createNotificationChannel(context)

    for (item in list) {
        if (item.Day == LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL,Locale.getDefault())) {
            scheduleNotificationAt(context, 7, 0, "Know your Breakfast", item.BreakFast)
            scheduleNotificationAt(context, 13, 0, "Know your Lunch", item.Lunch)
            scheduleNotificationAt(context, 19, 20, "Know your Dinner", item.Dinner)
        }
    }

}