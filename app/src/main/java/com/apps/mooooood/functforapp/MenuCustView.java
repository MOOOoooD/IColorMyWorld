package com.apps.mooooood.functforapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MenuCustView extends RelativeLayout {

    Button load_btn;
    Button colorBtn;
    Button menuBtn;


    public MenuCustView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);


        load_btn = findViewById(R.id.load_images);
        menuBtn = findViewById(R.id.menu_button);
        colorBtn = findViewById(R.id.color_button);


//
//        LayoutInflater layoutInflater = (LayoutInflater)context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        layoutInflater.inflate(R.layout.activity_menu_cust_view, this, true);

    }

}
