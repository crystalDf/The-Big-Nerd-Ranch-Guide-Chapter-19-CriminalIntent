package com.star.criminalintent;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_crime_camera, container, false);

        Button takePictureButton = (Button) v.findViewById(R.id.crime_camera_takePictureButton);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {

                    // setCameraDisplayOrientation(getActivity(), 0, mCamera);

                    mCamera.takePicture(new Camera.ShutterCallback() {
                        @Override
                        public void onShutter() {
                            mProgressContainer.setVisibility(View.VISIBLE);
                        }
                    }, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            String filename = UUID.randomUUID().toString() + ".jpg";

                            FileOutputStream out = null;
                            boolean success = true;

                            try {
                                out = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
//                                out = new PrintWriter(file);
                                out.write(data);
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
                                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                                int rotation2 = getActivity().getResources().getConfiguration().orientation;
                                System.out.println("Rotation: " + rotation + "Rotaion2: " + rotation2);
                                getActivity().setResult(Activity.RESULT_OK, i);
                            } else {
                                getActivity().setResult(Activity.RESULT_CANCELED);
                            }

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
    }

    @Override
    @SuppressWarnings("deprcation")
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
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

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotation2 = getActivity().getResources().getConfiguration().orientation;
        // System.out.println("Rotation: " + rotation + "Rotaion2: " + rotation2);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }
}
