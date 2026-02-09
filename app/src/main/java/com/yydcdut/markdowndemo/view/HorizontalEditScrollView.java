package com.yydcdut.markdowndemo.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownEditText;
import com.yydcdut.markdowndemo.R;
import com.yydcdut.markdowndemo.controller.BlockQuotesController;
import com.yydcdut.markdowndemo.controller.CenterAlignController;
import com.yydcdut.markdowndemo.controller.CodeController;
import com.yydcdut.markdowndemo.controller.HeaderController;
import com.yydcdut.markdowndemo.controller.HorizontalRulesController;
import com.yydcdut.markdowndemo.controller.ImageController;
import com.yydcdut.markdowndemo.controller.LinkController;
import com.yydcdut.markdowndemo.controller.ListController;
import com.yydcdut.markdowndemo.controller.StrikeThroughController;
import com.yydcdut.markdowndemo.controller.StyleController;
import com.yydcdut.markdowndemo.controller.TodoController;

public class HorizontalEditScrollView extends FrameLayout
        implements View.OnClickListener, View.OnLongClickListener {

    private MarkdownEditText mMarkdownEditText;

    private HeaderController mHeaderController;
    private StyleController mStyleController;
    private CenterAlignController mCenterAlignController;
    private HorizontalRulesController mHorizontalRulesController;
    private TodoController mTodoController;
    private StrikeThroughController mStrikeThroughController;
    private CodeController mCodeController;
    private BlockQuotesController mBlockQuotesController;
    private ListController mListController;
    private ImageController mImageController;
    private LinkController mLinkController;

    public HorizontalEditScrollView(Context context) {
        this(context, null);
    }

    public HorizontalEditScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalEditScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context)
                .inflate(R.layout.layout_horizontal_scroll, this, true);
    }

    public void setEditTextAndConfig(
            @NonNull MarkdownEditText markdownEditText,
            @NonNull MarkdownConfiguration markdownConfiguration
    ) {
        mMarkdownEditText = markdownEditText;

        mHeaderController = new HeaderController(markdownEditText, markdownConfiguration);
        mStyleController = new StyleController(markdownEditText);
        mCenterAlignController = new CenterAlignController(markdownEditText);
        mHorizontalRulesController = new HorizontalRulesController(markdownEditText);
        mTodoController = new TodoController(markdownEditText);
        mStrikeThroughController = new StrikeThroughController(markdownEditText);
        mCodeController = new CodeController(markdownEditText);
        mBlockQuotesController = new BlockQuotesController(markdownEditText);
        mListController = new ListController(markdownEditText);
        mImageController = new ImageController(markdownEditText);
        mLinkController = new LinkController(markdownEditText);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int[] clickIds = {
                R.id.img_header1,
                R.id.img_header2,
                R.id.img_header3,
                R.id.img_header4,
                R.id.img_header5,
                R.id.img_header6,
                R.id.img_bold,
                R.id.img_italic,
                R.id.img_center_align,
                R.id.img_horizontal_rules,
                R.id.img_todo,
                R.id.img_todo_done,
                R.id.img_strike_through,
                R.id.img_inline_code,
                R.id.img_code,
                R.id.img_block_quote,
                R.id.img_unorder_list,
                R.id.img_order_list,
                R.id.img_link,
                R.id.img_photo
        };

        for (int id : clickIds) {
            findViewById(id).setOnClickListener(this);
        }

        findViewById(R.id.img_block_quote).setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mMarkdownEditText == null) return;

        int id = v.getId();

        if (id == R.id.img_header1) {
            mHeaderController.doHeader(1);
        } else if (id == R.id.img_header2) {
            mHeaderController.doHeader(2);
        } else if (id == R.id.img_header3) {
            mHeaderController.doHeader(3);
        } else if (id == R.id.img_header4) {
            mHeaderController.doHeader(4);
        } else if (id == R.id.img_header5) {
            mHeaderController.doHeader(5);
        } else if (id == R.id.img_header6) {
            mHeaderController.doHeader(6);
        } else if (id == R.id.img_bold) {
            mStyleController.doBold();
        } else if (id == R.id.img_italic) {
            mStyleController.doItalic();
        } else if (id == R.id.img_center_align) {
            mCenterAlignController.doCenter();
        } else if (id == R.id.img_horizontal_rules) {
            mHorizontalRulesController.doHorizontalRules();
        } else if (id == R.id.img_todo) {
            mTodoController.doTodo();
        } else if (id == R.id.img_todo_done) {
            mTodoController.doTodoDone();
        } else if (id == R.id.img_strike_through) {
            mStrikeThroughController.doStrikeThrough();
        } else if (id == R.id.img_inline_code) {
            mCodeController.doInlineCode();
        } else if (id == R.id.img_code) {
            mCodeController.doCode();
        } else if (id == R.id.img_block_quote) {
            mBlockQuotesController.doBlockQuotes();
        } else if (id == R.id.img_unorder_list) {
            mListController.doUnOrderList();
        } else if (id == R.id.img_order_list) {
            mListController.doOrderList();
        } else if (id == R.id.img_link) {
            mLinkController.doImage();
        } else if (id == R.id.img_photo) {
            mImageController.doImage();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.img_block_quote) {
            mBlockQuotesController.addNestedBlockQuotes();
            return true;
        }
        return false;
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (mImageController != null) {
            mImageController.handleResult(requestCode, resultCode, data);
        }
    }
}