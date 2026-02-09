package com.yydcdut.markdowndemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.MarkdownTextView;
import com.yydcdut.markdown.loader.MDImageLoader;
import com.yydcdut.markdown.syntax.text.TextFactory;
import com.yydcdut.markdown.theme.ThemeSunburst;
import com.yydcdut.markdowndemo.loader.OKLoader;
import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMDTextView;
import com.yydcdut.rxmarkdown.RxMarkdown;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ShowActivity extends AppCompatActivity {

  public static final String EXTRA_CONTENT = "extra_content";

  private RxMDTextView rxTextView;
  private MarkdownTextView normalTextView;

  private String content;
  private MDImageLoader imageLoader;

  private boolean isRxMode = true;
  private Disposable rxDisposable;

  private Toast toast;

  public static void startShowActivity(Activity a, String content, boolean isRx) {
    Intent i = new Intent(a, ShowActivity.class);
    i.putExtra(EXTRA_CONTENT, content);
    i.putExtra("is_rx", isRx);
    a.startActivity(i);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show);

    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setTitle("Show");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    rxTextView = findViewById(R.id.txt_md_show_rx);
    normalTextView = findViewById(R.id.txt_md_show);

    rxTextView.setMovementMethod(LinkMovementMethod.getInstance());
    normalTextView.setMovementMethod(LinkMovementMethod.getInstance());

    content = getIntent().getStringExtra(EXTRA_CONTENT);
    isRxMode = getIntent().getBooleanExtra("is_rx", true);

    if (TextUtils.isEmpty(content)) {
      Snackbar.make(rxTextView, "No Text", Snackbar.LENGTH_SHORT).show();
      return;
    }

    imageLoader = new OKLoader(this);

    render();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_show, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }

    if (item.getItemId() == R.id.action_rx) {
      isRxMode = true;
      render();
      return true;
    }

    if (item.getItemId() == R.id.action_normal) {
      isRxMode = false;
      render();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void render() {
    clearRx();

    rxTextView.setVisibility(View.GONE);
    normalTextView.setVisibility(View.GONE);

    if (isRxMode) {
      rxTextView.setVisibility(View.VISIBLE);
      renderRx();
    } else {
      normalTextView.setVisibility(View.VISIBLE);
      renderNormal();
    }
  }

  private void renderRx() {
    RxMDConfiguration config =
        new RxMDConfiguration.Builder(this)
            .setDefaultImageSize(50, 50)
            .setBlockQuotesLineColor(0xff33b5e5)
            .setHeader1RelativeSize(1.6f)
            .setHeader2RelativeSize(1.5f)
            .setHeader3RelativeSize(1.4f)
            .setHeader4RelativeSize(1.3f)
            .setHeader5RelativeSize(1.2f)
            .setHeader6RelativeSize(1.1f)
            .setHorizontalRulesColor(0xff99cc00)
            .setHorizontalRulesHeight(1)
            .setCodeBgColor(0xffff4444)
            .setTodoColor(0xffaa66cc)
            .setTodoDoneColor(0xffff8800)
            .setUnOrderListColor(0xff00ddff)
            .setRxMDImageLoader(imageLoader)
            .setLinkFontColor(Color.BLUE)
            .showLinkUnderline(false)
            .setTheme(new ThemeSunburst())
            .setOnLinkClickCallback((v, l) -> toast(l))
            .setOnTodoClickCallback(
                (v, line, num) -> {
                  toast("line " + num);
                  return rxTextView.getText();
                })
            .build();

    rxDisposable =
        RxMarkdown.with(content, this)
            .config(config)
            .factory(TextFactory.create())
            .intoObservable()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                new DisposableObserver<CharSequence>() {
                  @Override
                  public void onNext(CharSequence cs) {
                    rxTextView.setText(cs, TextView.BufferType.SPANNABLE);
                  }

                  @Override
                  public void onError(Throwable e) {
                    e.printStackTrace();
                  }

                  @Override
                  public void onComplete() {}
                });
  }

  private void renderNormal() {
    MarkdownConfiguration config =
        new MarkdownConfiguration.Builder(this)
            .setDefaultImageSize(50, 50)
            .setBlockQuotesLineColor(0xff33b5e5)
            .setHeader1RelativeSize(1.6f)
            .setHeader2RelativeSize(1.5f)
            .setHeader3RelativeSize(1.4f)
            .setHeader4RelativeSize(1.3f)
            .setHeader5RelativeSize(1.2f)
            .setHeader6RelativeSize(1.1f)
            .setHorizontalRulesColor(0xff99cc00)
            .setHorizontalRulesHeight(1)
            .setCodeBgColor(0xffff4444)
            .setTodoColor(0xffaa66cc)
            .setTodoDoneColor(0xffff8800)
            .setUnOrderListColor(0xff00ddff)
            .setRxMDImageLoader(imageLoader)
            .setLinkFontColor(Color.BLUE)
            .showLinkUnderline(false)
            .setTheme(new ThemeSunburst())
            .setOnLinkClickCallback((v, l) -> toast(l))
            .setOnTodoClickCallback(
                (v, line, num) -> {
                  toast("line " + num);
                  return normalTextView.getText();
                })
            .build();

    MarkdownProcessor processor = new MarkdownProcessor(this);
    processor.factory(TextFactory.create());
    processor.config(config);
    normalTextView.setText(processor.parse(content));
  }

  private void clearRx() {
    if (rxDisposable != null && !rxDisposable.isDisposed()) {
      rxDisposable.dispose();
      rxDisposable = null;
    }
  }

  private void toast(String msg) {
    if (toast == null) {
      toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }
    toast.setText(msg);
    toast.show();
  }

  @Override
  protected void onDestroy() {
    clearRx();
    super.onDestroy();
  }
}
