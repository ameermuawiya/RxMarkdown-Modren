package com.yydcdut.markdowndemo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.appcompat.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.yydcdut.markdowndemo.R;

import java.io.File;

public class ImageDialogView extends LinearLayout
        implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private static final int REQUEST_CAMERA = 10;
    private static final int REQUEST_GALLERY = 11;
    private static final String DEFAULT_PATH = "drawable://" + R.mipmap.ic_launcher;

    private int mCurrentCameraPictureIndex = 0;

    private ImageView mTargetImageView;
    private String mPath = DEFAULT_PATH;

    private TextInputEditText mWidthEditText;
    private TextInputEditText mHeightEditText;
    private TextInputEditText mDescriptionEditText;

    public ImageDialogView(Context context) {
        super(context);
        init(context);
    }

    public ImageDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_image, this, true);

        mTargetImageView = v.findViewById(R.id.img_image);
        mTargetImageView.setOnClickListener(this);

        mWidthEditText = v.findViewById(R.id.edit_width);
        mHeightEditText = v.findViewById(R.id.edit_height);
        mDescriptionEditText = v.findViewById(R.id.edit_description);

        if (!ImageLoader.getInstance().isInited()) {
            ImageLoaderConfiguration config =
                    new ImageLoaderConfiguration.Builder(context)
                            .threadPriority(Thread.NORM_PRIORITY - 2)
                            .denyCacheImageMultipleSizesInMemory()
                            .diskCacheSize(50 * 1024 * 1024)
                            .tasksProcessingOrder(QueueProcessingType.LIFO)
                            .build();
            ImageLoader.getInstance().init(config);
        }

        clear();
    }

    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(getContext(), mTargetImageView);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            File file =
                    new File(
                            getContext().getExternalCacheDir(),
                            "tmp" + mCurrentCameraPictureIndex + ".jpg");
            mPath = "file:/" + file.getAbsolutePath();
            ImageLoader.getInstance().displayImage(mPath, mTargetImageView);
            mCurrentCameraPictureIndex++;

        } else if (requestCode == REQUEST_GALLERY && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor =
                    getContext()
                            .getContentResolver()
                            .query(uri, projection, null, null, null);

            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int index =
                                cursor.getColumnIndexOrThrow(
                                        MediaStore.Images.Media.DATA);
                        mPath = "file:/" + cursor.getString(index);
                        ImageLoader.getInstance().displayImage(mPath, mTargetImageView);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!(getContext() instanceof Activity)) {
            Log.e("ImageDialogView", "Context is not Activity");
            return false;
        }

        Activity activity = (Activity) getContext();

        if (item.getItemId() == R.id.action_gallery) {
            Intent intent =
                    new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            activity.startActivityForResult(intent, REQUEST_GALLERY);
            return true;

        } else if (item.getItemId() == R.id.action_camera) {
            File file =
                    new File(
                            getContext().getExternalCacheDir(),
                            "tmp" + mCurrentCameraPictureIndex + ".jpg");
            if (file.exists()) file.delete();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            activity.startActivityForResult(intent, REQUEST_CAMERA);
            return true;
        }

        return false;
    }

    public void clear() {
        ImageLoader.getInstance().displayImage(DEFAULT_PATH, mTargetImageView);
        mWidthEditText.setText("200");
        mHeightEditText.setText("200");
        mDescriptionEditText.setText("");
        mPath = DEFAULT_PATH;
    }

    public int getImageWidth() {
        return parseIntSafe(mWidthEditText.getText());
    }

    public int getImageHeight() {
        return parseIntSafe(mHeightEditText.getText());
    }

    public String getPath() {
        return mPath;
    }

    public String getDescription() {
        return mDescriptionEditText.getText() != null
                ? mDescriptionEditText.getText().toString()
                : "";
    }

    private int parseIntSafe(CharSequence value) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }
}