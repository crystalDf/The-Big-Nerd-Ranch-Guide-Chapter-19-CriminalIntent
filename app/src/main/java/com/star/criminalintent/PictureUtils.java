package com.star.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class PictureUtils {

    @SuppressWarnings("deprecation")
    public static BitmapDrawable getScaledDrawable(Activity activity, String path) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        double destWidth = display.getWidth();
        double destHeight = display.getHeight();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        double srcWidth = options.outWidth;
        double srcHeight = options.outHeight;

        int inSampleSize = 1;
        if ((srcHeight > destHeight) || (srcWidth > destWidth)) {
            if (srcWidth > srcHeight) {
                inSampleSize = (int) Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = (int) Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return new BitmapDrawable(activity.getResources(), bitmap);
    }

    public static void cleanImageView(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();

        bitmapDrawable.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }

    public static byte[] rotatePicture(byte[] data, CrimeCameraFragment.Orientation orientation) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();

        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                matrix.postRotate(90);
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                matrix.postRotate(270);
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                matrix.postRotate(0);
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                matrix.postRotate(180);
                break;
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        return out.toByteArray();
    }
}
