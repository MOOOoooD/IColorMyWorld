package com.apps.mooooood.icolormyworld

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main - inflates main screen that contains two buttons
 *      opcv_btn - instantiates OpenCVImage_using class
 *      paint_bth - instantiates UserPaint_using class
 *  Main written in Kotlin
 * @author Denise Fullerton
 * @date 5/12/18
 *
 */

class MainActivity : AppCompatActivity() {

    /**
     * Inflates activity_main.xml
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * Button - onClick method to instantiate OpCVImage_using and
         *      inflate activity_op_cvimage.xml
         */
        opcv_btn.setOnClickListener({
            val opCVIntent = Intent(this, OpCVImage_using::class.java)
            startActivity(opCVIntent)
        })

        /**
         * Button - onClick method to instantiate UserPaint_using object and
         *      inflate activity_user_paint.xml
         */
        paint_btn.setOnClickListener({
            val paintIntent = Intent(this, UserPaint_using::class.java)
            startActivity(paintIntent)
        })


    }
}