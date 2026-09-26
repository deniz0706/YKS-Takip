package com.deniz0706.ykstakip

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(32, 48, 32, 32)
        root.setBackgroundColor(Color.rgb(18, 18, 18))

        val title = TextView(this)
        title.text = "YKS Takip"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "TYT / AYT denemelerini takip et"
        subtitle.textSize = 16f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 16, 0, 40)

        val tytButton = TextView(this)
        tytButton.text = "TYT"
        tytButton.textSize = 20f
        tytButton.setTextColor(Color.WHITE)
        tytButton.gravity = Gravity.CENTER
        tytButton.setPadding(0, 30, 0, 30)
        tytButton.setBackgroundColor(Color.rgb(45, 45, 45))

        val aytButton = TextView(this)
        aytButton.text = "AYT"
        aytButton.textSize = 20f
        aytButton.setTextColor(Color.WHITE)
        aytButton.gravity = Gravity.CENTER
        aytButton.setPadding(0, 30, 0, 30)
        aytButton.setBackgroundColor(Color.rgb(45, 45, 45))

        root.addView(title)
        root.addView(subtitle)

        root.addView(
            tytButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            aytButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }
}
