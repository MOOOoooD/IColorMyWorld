package com.apps.mooooood.functforapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * MenuCustView.java - Custom view class that inflates a custom container
 *  holding buttons on left side of activity_user_paint.xml
 *  Class used primarily to setup and initiate image buttons
 * @author Denise Fullerton
 * @since created 1/28/18
 * @since last updated - 5/12/18
 */

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
