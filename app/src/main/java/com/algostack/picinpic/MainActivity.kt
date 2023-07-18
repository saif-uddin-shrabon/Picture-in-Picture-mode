package com.algostack.picinpic

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ModuleInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.MediaController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.algostack.picinpic.databinding.ActivityMainBinding
import java.lang.reflect.Modifier

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val isPipSupported: Boolean by lazy {
        packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    private val myReceiver = MyReceiver()

    private var videoViewBounds = Rect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.picinpic
        binding.videoView.setVideoPath(videoPath)

        val mediaController = MediaController(this)
        binding.videoView.setMediaController(mediaController)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.start()

        binding.videoView.viewTreeObserver.addOnGlobalLayoutListener {
            binding.videoView.getGlobalVisibleRect(videoViewBounds)
        }
    }

    val icon = Icon.createWithResource(this, android.R.drawable.ic_menu_camera)

    private fun updatePipParams(): PictureInPictureParams? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val actions = arrayListOf<RemoteAction>(
                RemoteAction(
                    icon,
                    "Camera",
                    "Switch to camera",
                    PendingIntent.getBroadcast(
                        applicationContext,
                        0,
                        Intent(applicationContext, myReceiver::class.java),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            )

            val aspectRatio = Rational(16, 9)
            PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .setSourceRectHint(videoViewBounds)
                .setActions(actions)
                .build()
        } else {
            null
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (!isPipSupported) {
            return
        }

        updatePipParams()?.let { params ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                enterPictureInPictureMode(params)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(myReceiver)
    }
}

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("Clicked on PIP action")
    }
}
