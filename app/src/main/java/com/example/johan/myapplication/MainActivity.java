package com.example.johan.myapplication;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GLSurfaceView mGLSurfaceView = (GLSurfaceView) findViewById(R.id.Video);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLContextClientVersion(2);


        MySurfaceView myRenderer = new MySurfaceView();
        mGLSurfaceView.setRenderer(myRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
