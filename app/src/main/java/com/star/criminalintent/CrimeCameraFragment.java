package com.star.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class CrimeCameraFragment extends Fragment {

    private static final String TAG = "CrimeCameraFragment";

    public static final String EXTRA_PHOTO_FILENAME =
            "com.star.criminalintent.photo_filename";

    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private SurfaceView mSurfaceView;

    private View mProgressContainer;

    private OrientationEventListener mOrientationEventListener;
    private Orientation mOrientation;

    public enum Orientation {
        ORIENTATION_PORTRAIT_NORMAL, ORIENTATION_PORTRAIT_INVERTED,
        ORIENTATION_LANDSCAPE_NORMAL, ORIENTATION_LANDSCAPE_INVERTED
    };

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_crime_camera, container, false);

        Button takePictureButton = (Button) v.findViewById(R.id.crime_camera_takePictureButton);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {

                    mCamera.takePicture(
                        new Camera.ShutterCallback() {
                            @Override
                            public void onShutter() {
                                mProgressContainer.setVisibility(View.VISIBLE);
                            }
                        },
                        null,
                        new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                saveJPEGPicture(data, camera);

                                getActivity().finish();
                            }
                        });
                }
            }
        });

        mSurfaceView = (SurfaceView) v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error setting up preview display", e);
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera == null) {
                    return;
                }

                mCamera.setParameters(getBestParameters(mCamera, width, height));

                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        });

        mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        return v;
    }

    @Override
    @SuppressWarnings("deprcation")
    public void onResume() {
        super.onResume();
        mCamera = Camera.open();

        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(getActivity(),
                    SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {
                    if ((orientation >= 315) || (orientation < 45)) {
                        mOrientation = Orientation.ORIENTATION_PORTRAIT_NORMAL;
                    } else if ((orientation < 315) && (orientation >= 225)) {
                        mOrientation = Orientation.ORIENTATION_LANDSCAPE_NORMAL;
                    } else if ((orientation < 225) && (orientation >= 135)) {
                        mOrientation = Orientation.ORIENTATION_PORTRAIT_INVERTED;
                    } else if ((orientation < 135) && (orientation > 45)) {
                        mOrientation = Orientation.ORIENTATION_LANDSCAPE_INVERTED;
                    }
                }
            };
        }

        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    @Override
    @SuppressWarnings("deprcation")
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    @SuppressWarnings("deprcation")
    private Camera.Parameters getBestParameters(Camera camera, int width, int height) {
        Camera.Parameters bestParameters = camera.getParameters();
        Camera.Size s = getBestSupportedSize(bestParameters.getSupportedPreviewSizes(), width, height);
        bestParameters.setPreviewSize(s.width, s.height);
        s = getBestSupportedSize(bestParameters.getSupportedPictureSizes(), width, height);
        bestParameters.setPictureSize(s.width, s.height);

        return bestParameters;
    }

    @SuppressWarnings("deprcation")
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }

        return bestSize;
    }

    private void saveJPEGPicture(byte[] data, Camera camera) {

        boolean success = true;
        String filename = UUID.randomUUID().toString() + ".jpg";

        File sdCard = Environment.getExternalStorageDirectory();

        File dir = new File(sdCard.getAbsolutePath() + File.separator + getActivity().getString(R.string.app_name));

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, filename);

        FileOutputStream out = null;

        try {
//            out = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            out = new FileOutputStream(file);
            out.write(PictureUtils.rotatePicture(data, mOrientation));
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error writing to file " + filename, e);
            success = false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing file " + filename, e);
                success = false;
            }
        }

        if (success) {
            Log.i(TAG, "JPEG saved at " + filename);
            Intent i = new Intent();
            i.putExtra(EXTRA_PHOTO_FILENAME, filename);
            getActivity().setResult(Activity.RESULT_OK, i);
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
        }
    }

}
