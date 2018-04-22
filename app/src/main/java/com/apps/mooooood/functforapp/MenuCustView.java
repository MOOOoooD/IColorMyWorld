package com.apps.mooooood.functforapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MenuCustView extends RelativeLayout {

    Button load_btn;
  //  Button colorBtn;
    Button menuBtn;


    public MenuCustView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        menuBtn = findViewById(R.id.menu_button);

    }

}
