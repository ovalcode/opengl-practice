package com.example.johan.myapplication;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by johan on 2017/04/13.
 */

public class MySurfaceView implements GLSurfaceView.Renderer {

    private static final String vShaderStr =
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}";

    private static final String fShaderStr =
            "precision mediump float;" +
                    //"uniform vec4 vColor;" +
            "void main() {" +
            " gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
//                    " gl_FragColor = vColor;" +
            "}";

    private int vertexShader;
    private int fragmentShader;
    private int glProgram;

    MySurfaceView() {
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vShaderStr);
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderStr);

        glProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(glProgram, vertexShader);
        GLES20.glAttachShader(glProgram, fragmentShader);

        //GLES20.glBindAttribLocation(glProgram, 0, "vPosition");

        GLES20.glLinkProgram(glProgram);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //required for matrix setup
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        float vVertices[] = {0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f};

        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vVertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = vertexByteBuffer.asFloatBuffer();




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

    }

    public static int loadShader(int type, String shaderCode) {
        //Create a Vertex Shader Type Or a Fragment Shader Type (GLES20.GL_VERTEX_SHADER OR GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        //Add The Source Code and Compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
