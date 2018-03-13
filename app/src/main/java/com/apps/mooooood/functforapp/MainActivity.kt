package com.apps.mooooood.functforapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val MAINTAG = "MAIN-DBUG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Open CV Button - not in paint functions
        // Image load, different filter functions
        opcv_btn.setOnClickListener({
            val opCVIntent = Intent(this, OpCVImage_using::class.java)
            startActivity(opCVIntent)
        })

        // Paint Activity Button
        // Image load, filter image, user paint activity - actual paint activity
        paint_btn.setOnClickListener({
            val paintIntent = Intent(this, UserPaint_using::class.java)
            startActivity(paintIntent)
        })


        /*
        close_btn.setOnClickListener({
            Log.d(MAINTAG,"close app onclick")
            finish()
            //   System.exit(0)

        })
        */

    }
}