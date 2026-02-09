package com.yydcdut.markdowndemo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownEditText;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.MarkdownTextView;
import com.yydcdut.markdown.syntax.edit.EditFactory;
import com.yydcdut.markdown.syntax.text.TextFactory;
import com.yydcdut.markdowndemo.loader.OKLoader;
import com.yydcdut.markdowndemo.view.HorizontalEditScrollView;
import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableObserver;

public class CompareActivity extends AppCompatActivity implements TextWatcher {

    private RxMDEditText mRxMDEditText;
    private RxMDTextView mRxMDTextView;
    private RxMDConfiguration mRxMDConfiguration;
    private Disposable mDisposable;

    private MarkdownTextView mMarkdownTextView;
    private MarkdownEditText mMarkdownEditText;
    private MarkdownProcessor mMarkdownProcessor;

    private boolean isRx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Compare");

        HorizontalEditScrollView scrollView = findViewById(R.id.scroll_edit);
        isRx = getIntent().getBooleanExtra("is_rx", false);

        mRxMDEditText = findViewById(R.id.edit_rx);
        mRxMDTextView = findViewById(R.id.txt_md_show_rx);

        mMarkdownEditText = findViewById(R.id.edit_md);
        mMarkdownTextView = findViewById(R.id.txt_md_show);

        if (isRx) {
            mRxMDEditText.setVisibility(View.VISIBLE);
            mRxMDTextView.setVisibility(View.VISIBLE);
            mRxMDEditText.addTextChangedListener(this);
            mRxMDEditText.setText(Const.MD_SAMPLE);
            rxMarkdown(scrollView);
        } else {
            mMarkdownEditText.setVisibility(View.VISIBLE);
            mMarkdownTextView.setVisibility(View.VISIBLE);
            mMarkdownEditText.addTextChangedListener(this);
            mMarkdownEditText.setText(Const.MD_SAMPLE);
            markdown(scrollView);
        }
    }

    private void rxMarkdown(HorizontalEditScrollView scrollView) {
        mRxMDConfiguration = new RxMDConfiguration.Builder(this)
                .setDefaultImageSize(400, 400)
                .setBlockQuotesLineColor(0xff33b5e5)
                .setRxMDImageLoader(new OKLoader(this))
                .build();

        scrollView.setEditTextAndConfig(mRxMDEditText, mRxMDConfiguration);

        RxMarkdown.live(mRxMDEditText)
                .config(mRxMDConfiguration)
                .factory(EditFactory.create())
                .intoObservable()
                .subscribe();
    }

    private void markdown(HorizontalEditScrollView scrollView) {
        MarkdownConfiguration config = new MarkdownConfiguration.Builder(this)
                .setDefaultImageSize(400, 400)
                .setBlockQuotesLineColor(0xff33b5e5)
                .setRxMDImageLoader(new OKLoader(this))
                .build();

        scrollView.setEditTextAndConfig(mMarkdownEditText, config);

        mMarkdownProcessor = new MarkdownProcessor(this);
        mMarkdownProcessor.config(config);
        mMarkdownProcessor.factory(EditFactory.create());
        mMarkdownProcessor.live(mMarkdownEditText);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isRx) {
            if (mDisposable != null) mDisposable.dispose();

            mDisposable =
                    RxMarkdown.with(s.toString(), this)
                            .config(mRxMDConfiguration)
                            .factory(TextFactory.create())
                            .intoObservable()
                            .subscribeWith(new DisposableObserver<CharSequence>() {
                                @Override public void onNext(CharSequence cs) {
                                    mRxMDTextView.setText(cs, TextView.BufferType.SPANNABLE);
                                }
                                @Override public void onError(Throwable e) {}
                                @Override public void onComplete() {}
                            });
        } else {
            mMarkdownTextView.setText(mMarkdownProcessor.parse(s.toString()));
        }
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) mDisposable.dispose();
    }
}