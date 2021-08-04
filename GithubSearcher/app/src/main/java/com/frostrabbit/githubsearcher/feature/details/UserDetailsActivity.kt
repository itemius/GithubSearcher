package com.frostrabbit.githubsearcher.feature.details

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.frostrabbit.githubsearcher.R
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso


class UserDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        val login = intent.getStringExtra("login")
        val avatarUrl = intent.getStringExtra("avatar")

        val userLogin = findViewById<TextView>(R.id.userName)
        userLogin.text = login

        val userImage = findViewById<ShapeableImageView>(R.id.userImage)
        Picasso.get().load(avatarUrl).into(userImage)

        val button: Button = findViewById(R.id.openWebPageButton)
        button.setOnClickListener(View.OnClickListener {
            val url = "https://github.com/" + intent.getStringExtra("login")
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })
    }
}