package kr.co.bepo.geofencingapp.broadcastreceiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.util.Constants.NOTIFICATION_CHANNEL_ID
import kr.co.bepo.geofencingapp.util.Constants.NOTIFICATION_CHANNEL_NAME
import kr.co.bepo.geofencingapp.util.Constants.NOTIFICATION_ID

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("BroadcastReceiver", errorMessage)
            return
        }

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d("BroadcastReceiver", "Geofence ENTER")
                displayNotification(context, "Geofence ENTER")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("BroadcastReceiver", "Geofence EXIT")
                displayNotification(context, "Geofence EXIT")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d("BroadcastReceiver", "Geofence DWELL")
                displayNotification(context, "Geofence DWELL")
            }
            else -> {
                Log.d("BroadcastReceiver", "Invalid Type")
                displayNotification(context, "Geofence INVALID TYPE")
            }
        }
    }

    private fun displayNotification(context: Context, geofenceTransition: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Geofence")
            .setContentText(geofenceTransition)
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}