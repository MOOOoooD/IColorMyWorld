package com.apps.mooooood.functforapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MenuCustView extends RelativeLayout {

    Button load_btn;
  //  Button colorBtn;
    ImageButton menuBtn;
    ImageButton eraseDraw;


    public MenuCustView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        menuBtn = findViewById(R.id.menu_button);
        eraseDraw = findViewById(R.id.erase_btn);

    }

}
