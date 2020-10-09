package com.sanyinchen.bridge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.ReactInstanceManager
import com.facebook.soloader.SoLoader
import com.sanyinchen.test.nativemodule.TestPackages
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoLoader.init(this, false)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val textView = findViewById<TextView>(R.id.text)
        val mainHandle = Handler(Looper.getMainLooper())
        val reactInstanceManager = ReactInstanceManager.builder()
            .setApplication(application)
            .setJSBundleFile("assets://js-bridge-bundle.js")
            .addPackage(TestPackages {
                mainHandle.post {
                    textView.text = "this message is  from js-bridge : $it"
                }
            })
            .setNativeModuleCallExceptionHandler { e -> e.printStackTrace() }
            .build()
        reactInstanceManager.createReactContextInBackground()

    }


}
