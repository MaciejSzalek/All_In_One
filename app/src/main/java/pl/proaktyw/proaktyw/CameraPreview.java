package pl.proaktyw.proaktyw;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Maciej Szalek on 2018-03-21.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;
    boolean isPreviewRunning = false;
    Camera.PreviewCallback callback;
    Activity mActivity;

    public CameraPreview( Activity activity){
        super(activity);

        mActivity = activity;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            mCamera = Camera.open();

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
            int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation){
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees  = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }
            mCamera.setDisplayOrientation((cameraInfo.orientation - degrees + 360) % 360);

            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e(TAG, "Error setting camera preview: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
        for (Camera.Size s : prevSizes ){
            if((s.height <= height) && (s.width <= width)){
                params.setPreviewSize(s.width, s.height);
            }
        }
        mCamera.setParameters(params);
        mCamera.startPreview();
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this) {
            try{
                if (mCamera != null) {
                    mCamera.stopPreview();
                    isPreviewRunning = false;
                    mCamera.release();
                }
            }catch(Exception e){
                Log.e("Camera", e.getMessage());
            }
        }
    }
    public void onPreviewFrame(byte[] arg0, Camera arg1){
        if(callback != null)
            callback.onPreviewFrame(arg0, arg1);
    }
}
