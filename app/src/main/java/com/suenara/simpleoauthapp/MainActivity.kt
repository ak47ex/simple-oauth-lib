package com.suenara.simpleoauthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.suenara.R
import com.suenara.simpleoauthlib.OauthConfig
import com.suenara.simpleoauthlib.SimpleOauth

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    lateinit var authButton: Button
    lateinit var signOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authButton = findViewById(R.id.auth_button)
        authButton.setOnClickListener { SimpleOauth.launchSignInActivity(this, OAUTH_CONFIG) }
        signOutButton = findViewById(R.id.sign_out_button)
        signOutButton.setOnClickListener { SimpleOauth.signOut(this) }
    }

    override fun onResume() {
        super.onResume()
        signOutButton.isVisible = SimpleOauth.isSignedIn(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        SimpleOauth.checkActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private val OAUTH_CONFIG: OauthConfig = TODO()
    }
}