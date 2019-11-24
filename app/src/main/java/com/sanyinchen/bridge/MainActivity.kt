package com.sanyinchen.bridge

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.facebook.Bridge
import com.facebook.soloader.SoLoader

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoLoader.init(this, false)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val textView = findViewById<TextView>(R.id.text)
        val click = findViewById<Button>(R.id.click)
        click.setOnClickListener {
            Bridge.INS.test(this.application)
        }
    }


}
