package com.yydcdut.markdowndemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.yydcdut.markdowndemo.R;

public class LinkDialogView extends LinearLayout {

    private TextInputEditText mDescriptionEditText;
    private TextInputEditText mLinkEditText;

    public LinkDialogView(Context context) {
        super(context);
        init(context);
    }

    public LinkDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LinkDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_link, this, true);
        mDescriptionEditText = v.findViewById(R.id.edit_description_link);
        mLinkEditText = v.findViewById(R.id.edit_link);
    }

    public void clear() {
        mDescriptionEditText.setText("");
        mLinkEditText.setText("http://");
    }

    public String getDescription() {
        return mDescriptionEditText.getText() != null
                ? mDescriptionEditText.getText().toString()
                : "";
    }

    public String getLink() {
        return mLinkEditText.getText() != null
                ? mLinkEditText.getText().toString()
                : "";
    }
}