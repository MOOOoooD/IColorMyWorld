package com.apps.mooooood.functforapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class MenuCustView extends RelativeLayout {

    ImageButton menuBtn, eraseDraw, undoBtn, redoBtn;


    public MenuCustView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        menuBtn = findViewById(R.id.menu_button);
        eraseDraw = findViewById(R.id.draw_tool_btn);
        undoBtn = findViewById(R.id.undo_button);
        redoBtn = findViewById(R.id.redo_button);
    }

}
