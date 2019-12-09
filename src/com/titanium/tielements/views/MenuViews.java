package com.titanium.tielements.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;

public class MenuViews extends LinearLayout {

    TextView menu_title;
    ImageView menu_icon;
    String title;
    int icon;

    public MenuViews(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public MenuViews(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public MenuViews(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }



    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        View base = inflate(context, R.layout.menu_view, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuViews, defStyleAttr, defStyleRes);
        title = typedArray.getString(R.styleable.MenuViews_title);
        icon = typedArray.getResourceId(R.styleable.MenuViews_icon, 0);
        menu_title = base.findViewById(R.id.menu_title);
        menu_icon = base.findViewById(R.id.menu_icon);
        if (!title.isEmpty()) {
            menu_title.setText(title);
        } else {
            menu_title.setText("Not Available");
        }
        menu_icon.setImageResource(icon);
        typedArray.recycle();
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public void setIcon(int image) {
        this.icon = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
