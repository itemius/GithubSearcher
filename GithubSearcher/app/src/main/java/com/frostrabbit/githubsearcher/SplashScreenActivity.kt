package com.frostrabbit.githubsearcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.frostrabbit.githubsearcher.feature.search.SearchActivity


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this@SplashScreenActivity, SearchActivity::class.java))
        finish()
    }
}