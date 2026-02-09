package com.yydcdut.markdowndemo;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.yydcdut.markdown.MarkdownConfiguration;
import com.yydcdut.markdown.MarkdownEditText;
import com.yydcdut.markdown.MarkdownProcessor;
import com.yydcdut.markdown.syntax.edit.EditFactory;
import com.yydcdut.markdowndemo.view.EditScrollView;
import com.yydcdut.markdowndemo.view.HorizontalEditScrollView;
import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.RxMDEditText;
import com.yydcdut.rxmarkdown.RxMarkdown;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditActivity extends AppCompatActivity
    implements View.OnClickListener, EditScrollView.OnScrollChangedListener {

  private RxMDEditText mRxMDEditText;
  private MarkdownEditText mMarkdownEditText;
  private FloatingActionButton mFloatingActionButton;
  private HorizontalEditScrollView mHorizontalEditScrollView;

  private Observable<CharSequence> mObservable;
  private Disposable mDisposable;

  private MarkdownProcessor mMarkdownProcessor;
  private boolean isRx;
  private int mShortestDistance = -1;

  private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit);

    MaterialToolbar toolbar = findViewById(R.id.toolbar);
setSupportActionBar(toolbar);
    toolbar.setTitle("Edit");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mFloatingActionButton = findViewById(R.id.fab);
    mFloatingActionButton.setOnClickListener(this);

    EditScrollView editScrollView = findViewById(R.id.edit_scroll);
    editScrollView.setOnScrollChangedListener(this);

    mRxMDEditText = findViewById(R.id.edit_rx);
    mMarkdownEditText = findViewById(R.id.edit_md);
    mHorizontalEditScrollView = findViewById(R.id.scroll_edit);

    isRx = getIntent().getBooleanExtra("is_rx", false);

    if (isRx) {
      mRxMDEditText.setVisibility(View.VISIBLE);
      rxMarkdown();
    } else {
      mMarkdownEditText.setVisibility(View.VISIBLE);
      markdown();
    }

    copyDemoPictureAsync();
  }

  private void markdown() {
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
            .setCodeBgColor(0xffff4444)
            .setTodoColor(0xffaa66cc)
            .setTodoDoneColor(0xffff8800)
            .setUnOrderListColor(0xff00ddff)
            .build();

    mHorizontalEditScrollView.setEditTextAndConfig(mMarkdownEditText, config);
    mMarkdownEditText.setText(Const.MD_SAMPLE);

    mMarkdownProcessor = new MarkdownProcessor(this);
    mMarkdownProcessor.config(config);
    mMarkdownProcessor.factory(EditFactory.create());
    mMarkdownProcessor.live(mMarkdownEditText);
  }

  private void rxMarkdown() {

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
            .setCodeBgColor(0xffff4444)
            .setTodoColor(0xffaa66cc)
            .setTodoDoneColor(0xffff8800)
            .setUnOrderListColor(0xff00ddff)
            .build();

    mHorizontalEditScrollView.setEditTextAndConfig(mRxMDEditText, config);
    mRxMDEditText.setText(Const.MD_SAMPLE);

    mObservable =
        RxMarkdown.live(mRxMDEditText)
            .config(config)
            .factory(EditFactory.create())
            .intoObservable()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == R.id.action_enable && isRx && mObservable != null) {

      final long time = System.currentTimeMillis();

      mDisposable =
          mObservable.subscribeWith(
              new DisposableObserver<CharSequence>() {

                @Override
                public void onNext(CharSequence charSequence) {
                  Snackbar.make(
                          mFloatingActionButton,
                          String.valueOf(System.currentTimeMillis() - time),
                          Snackbar.LENGTH_SHORT)
                      .show();
                }

                @Override
                public void onError(Throwable e) {
                  Snackbar.make(mFloatingActionButton, e.getMessage(), Snackbar.LENGTH_SHORT)
                      .show();
                }

                @Override
                public void onComplete() {}
              });

      return true;

    } else if (item.getItemId() == R.id.action_disable) {

      if (mDisposable != null) {
        mDisposable.dispose();
        mDisposable = null;
        mRxMDEditText.clear();
      }
      return true;

    } else if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {
    if (isRx) {
      ShowActivity.startShowActivity(this, mRxMDEditText.getText().toString(), true);
    } else {
      ShowActivity.startShowActivity(this, mMarkdownEditText.getText().toString(), false);
    }
  }

  @Override
  public void onScrollChanged(int l, int t, int oldl, int oldt) {
    if (mShortestDistance == -1) {
      mShortestDistance = mRxMDEditText.getLineHeight() * 3 / 2;
    }
    if (Math.abs(t - oldt) > mShortestDistance) {
      mFloatingActionButton.requestFocus();
    }
  }

  private void copyDemoPictureAsync() {

    ioExecutor.execute(
        () -> {
          try {
            File dir = new File(getExternalFilesDir(null), "rxMarkdown");
            if (!dir.exists()) dir.mkdirs();

            File outFile = new File(dir, "b.jpg");

            AssetManager am = getAssets();
            InputStream in = am.open("b.jpg");
            OutputStream out = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
              out.write(buffer, 0, read);
            }

            in.close();
            out.close();

            mainHandler.post(
                () ->
                    Snackbar.make(mFloatingActionButton, "Demo image saved", Snackbar.LENGTH_SHORT)
                        .show());

          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mHorizontalEditScrollView.handleResult(requestCode, resultCode, data);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mDisposable != null) {
      mDisposable.dispose();
    }
    ioExecutor.shutdown();
  }
}
