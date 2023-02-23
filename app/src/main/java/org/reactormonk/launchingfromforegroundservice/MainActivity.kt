package org.reactormonk.launchingfromforegroundservice

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.reactormonk.launchingfromforegroundservice.ui.theme.LaunchingFromForegroundServiceTheme

class LaunchingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startForegroundService(Intent(this, ForegroundService::class.java))
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "Got created")

        setContent {
            LaunchingFromForegroundServiceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Button(onClick = {
                        sendBroadcast(Intent("START"))
                    }) {
                        Text("Restart this activity from service")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, ForegroundService::class.java))
    }
}

class ForegroundService: Service() {
    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    val CHANNEL_ID = "CHANNEL01"

    lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("ForegroundService", "Got intent: $intent")
                val i = Intent(context, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                }
                startActivity(i)
            }
        }

        registerReceiver(receiver, IntentFilter().also {
            it.addAction("START")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val chan = NotificationChannel(
            CHANNEL_ID,
            "Background service 01",
            NotificationManager.IMPORTANCE_HIGH
        )
        service.createNotificationChannel(chan)

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentText("Content")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        Log.d("ForegroundService", "Going into foreground")
        startForeground(1, notification)

        return START_STICKY
    }
}