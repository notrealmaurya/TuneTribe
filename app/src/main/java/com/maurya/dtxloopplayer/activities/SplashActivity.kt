package com.maurya.dtxloopplayer.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.maurya.dtxloopplayer.MainActivity
import com.maurya.dtxloopplayer.databinding.ActivitySplashBinding
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var activitySplashBinding: ActivitySplashBinding

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activitySplashBinding.root)

        Handler(Looper.myLooper()!!).postDelayed(
            {
                val intent = Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                )
                startActivity(intent)
                finish()
            },
//            2250
            1
        )

    }
}