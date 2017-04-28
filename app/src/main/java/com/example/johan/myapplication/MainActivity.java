package com.example.johan.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity implements SurfaceTexture.OnFrameAvailableListener {

    private Bitmap heartBitmap;
    private TextureView textureView;
    private String cameraId;
    private Size imageDimension;
    private SurfaceTexture surface;
    MySurfaceRenderer myRenderer;
    protected CameraDevice cameraDevice;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private Handler mBackgroundHandler;
    private Handler mainHandler;
    private HandlerThread mBackgroundThread;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private int texture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainHandler = new Handler(Looper.getMainLooper());
        GLSurfaceView mGLSurfaceView = (GLSurfaceView) findViewById(R.id.Video);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLContextClientVersion(2);


        myRenderer = new MySurfaceRenderer(this);
        mGLSurfaceView.setRenderer(myRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        AssetManager assetMgr = getResources().getAssets();

        try {
            InputStream is = assetMgr.open("heart.png");
            heartBitmap = BitmapFactory.decodeStream(is);
//            int bytes = heartBitmap.getByteCount();
//            ByteBuffer buffer = ByteBuffer.allocate(bytes);
//            bitmap.copyPixelsToBuffer(buffer);
//            Bitmap.Config conf = bitmap.getConfig();
//            System.out.println(conf.name());
        } catch (IOException e) {
            e.printStackTrace();
        }


//        textureView = (TextureView) findViewById(R.id.texture);

//        textureView.setSurfaceTextureListener(textureListener);
    }

    public Size getImageDimension() {
        return imageDimension;
    }

    public Bitmap getHeartBitmap() {
        return heartBitmap;
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open

            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    /*TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };*/

    public void openCamera(int texture) {
        this.texture = texture;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = new SurfaceTexture(this.texture);
            texture.setOnFrameAvailableListener(this);
            myRenderer.setSurfaceTexture(texture);
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //CameraDevice.
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*public void startCamera(int texture) {
      surface = new SurfaceTexture(texture);
      surface.setOnFrameAvailableListener(this);
      mCamera = Camera
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        /*if (textureView.isAvailable()) {
            //openCamera();
        } else {
            //textureView.setSurfaceTextureListener(textureListener);
        }*/
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        GLSurfaceView mGLSurfaceView = (GLSurfaceView) findViewById(R.id.Video);
        mGLSurfaceView.requestRender();
    }
}
