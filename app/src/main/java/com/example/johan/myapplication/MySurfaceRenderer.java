package com.example.johan.myapplication;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by johan on 2017/04/13.
 */

public class MySurfaceRenderer implements GLSurfaceView.Renderer {

    private static final String vShaderCamStr =
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}";

    private static final String fShaderCamStr =
            "precision mediump float;" +
                    //"uniform vec4 vColor;" +
            "void main() {" +
            " gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
//                    " gl_FragColor = vColor;" +
            "}";

    DirectVideo mDirectVideo;

    private int vertexCamShader;
    private int fragmentCamShader;
    private int glCamProgram;
    private MainActivity mainActivity;
    private int textures[];
    private SurfaceTexture surfaceTexture;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    MySurfaceRenderer(MainActivity activity) {
        this.mainActivity = activity;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        /*GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vShaderStr);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderStr);

        glProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(glProgram, vertexShader);
        GLES20.glAttachShader(glProgram, fragmentShader);

        //GLES20.glBindAttribLocation(glProgram, 0, "vPosition");

        GLES20.glLinkProgram(glProgram);
        texture = createTexture();
        this.mainActivity.openCamera(texture);*/
        textures = createTexture();
        mDirectVideo = new DirectVideo(textures[0], textures[1]);
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        mainActivity.openCamera(textures[0]);
        //create texture and start camera with this in mainactivity
    }

    private int[] createTexture()
    {
        int[] texture = new int[2];

        GLES20.glGenTextures(2,texture, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);




        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[1]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mainActivity.getHeartBitmap(), 0);
//---------------------------------------------------------------------------------


        return texture;
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //required for matrix setup
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        //This Projection Matrix is applied to object coordinates in the onDrawFrame() method
//        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Matrix.orthoM(mProjMatrix,0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        /*float vVertices[] = {0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f};

        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vVertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = vertexByteBuffer.asFloatBuffer();

        surfaceTexture.updateTexImage();


        //FloatBuffer.allocateD(vVertices.length).order(ByteOrder.nativeOrder());
        vertexBuffer.put(vVertices);
        vertexBuffer.position(0);

        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(glProgram);
        int mPositionHandle = GLES20.glGetAttribLocation(glProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,  false, 12, vertexBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glVertexAttribPointer()
*/

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
        Matrix.setRotateM(mRotationMatrix, 0, 0, 0, 0, -1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

        if (surfaceTexture == null)
            return;
        float[] mtx = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mtx);

        mDirectVideo.draw(mMVPMatrix, mainActivity.getImageDimension());
    }

    public static int loadShader(int type, String shaderCode) {
        //Create a Vertex Shader Type Or a Fragment Shader Type (GLES20.GL_VERTEX_SHADER OR GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        //Add The Source Code and Compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void setSurfaceTexture (SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }
}
