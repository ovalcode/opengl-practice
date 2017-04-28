package com.example.johan.myapplication;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Size;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by johan on 2017/04/17.
 */

public class DirectVideo {


    private final String vertexCamShaderCode =
            "attribute vec4 position;" +
                    "attribute vec2 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "uniform mat4 uMVPMatrix;" +
                    "void main()" +
                    "{"+
                    "gl_Position = uMVPMatrix * position;"+
                    "textureCoordinate = inputTextureCoordinate;" +
                    "}";

    private final String fragmentCamShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;                            \n" +
                    "uniform samplerExternalOES s_texture;               \n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                    "}";

    private final String vertexOverlayShaderCode =
//Test
            "attribute vec2 a_TexCoordinate;" +
                    "varying vec2 v_TexCoordinate;" +
//End Test
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position =  uMVPMatrix * vPosition;" +
                    //Test
                    "v_TexCoordinate = a_TexCoordinate;" +
                    //End Test
                    "}";

    private final String fragmentOverlayShaderCode =
            "precision mediump float;" +
                    "uniform vec4 v_Color;" +
//Test
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
//End Test
                    "void main() {" +
//"gl_FragColor = vColor;" +
                    "gl_FragColor = ( texture2D(u_Texture, v_TexCoordinate));" +
                    "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private  FloatBuffer vertexBufferOverlay;
    private ShortBuffer drawListBuffer;
    private final int mCamProgram;
    private final int mOverlayProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureCoordHandle;
    boolean vertexBufferPopulated = false;
    private float overlaySize = 1;
    private float overlaySizeStep = (float) 0.05;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    static float squareVertices[] = { // in counterclockwise order:
            -1.0f,  1.0f,
            -1.0f,  -1.0f,
            1.0f,  -1.0f,
            1.0f,  1.0f
    };

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    static float textureVertices[] = { // in counterclockwise order:
            0.0f,  0.0f,
            0.0f,  1f,
            1f,  1f,
            1f,  0.0f
    };

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private int texture;
    private int overlayTexture;

    public DirectVideo(int _texture, int _overlayTexture)
    {
        texture = _texture;
        overlayTexture = _overlayTexture;
//        ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
//        bb.order(ByteOrder.nativeOrder());
//        vertexBuffer = bb.asFloatBuffer();
//        vertexBuffer.put(squareVertices);
//        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        int vertexCamShader = MySurfaceRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexCamShaderCode);
        int fragmentCamShader = MySurfaceRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCamShaderCode);

        mCamProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mCamProgram, vertexCamShader);   // add the vertex shader to program
        GLES20.glAttachShader(mCamProgram, fragmentCamShader); // add the fragment shader to program
        GLES20.glLinkProgram(mCamProgram);

        int vertexOverlayShader = MySurfaceRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexOverlayShaderCode);
        int fragmentOverlayShader = MySurfaceRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentOverlayShaderCode);

        mOverlayProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mOverlayProgram, vertexOverlayShader);   // add the vertex shader to program
        GLES20.glAttachShader(mOverlayProgram, fragmentOverlayShader); // add the fragment shader to program
        GLES20.glLinkProgram(mOverlayProgram);

    }

    public void prepareVertexBuffer(Size imageSize) {
        int width = imageSize.getWidth();
        int height = imageSize.getHeight();

        float ratio = (float)width / (float)height;

        for (int i = 0; i < (squareVertices.length >> 1); i++) {
            float value = squareVertices[(i << 1)];
            squareVertices[(i << 1)] = value * ratio;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareVertices);
        vertexBuffer.position(0);
        vertexBufferPopulated = true;

        bb = ByteBuffer.allocateDirect(squareVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBufferOverlay = bb.asFloatBuffer();
        //vertexBufferOverlay.put(squareVertices);
        vertexBufferOverlay.position(0);

    }

    public void draw(float[] mvpMatrix, Size imageSize)
    {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDepthMask(false);

        //Draw Camera Frame
        //---------------------------------------------------------------------------
        GLES20.glUseProgram(mCamProgram);

//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

        mPositionHandle = GLES20.glGetAttribLocation(mCamProgram, "position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //prepare vertex buffer
        if (!vertexBufferPopulated) {
            prepareVertexBuffer(imageSize);
        }
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,vertexStride, vertexBuffer);

        int mTextureUniformHandle = GLES20.glGetUniformLocation(mCamProgram, "s_texture");
        GLES20.glUniform1i(mTextureUniformHandle, 0);
        mTextureCoordHandle = GLES20.glGetAttribLocation(mCamProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,vertexStride, textureVerticesBuffer);

        mColorHandle = GLES20.glGetAttribLocation(mCamProgram, "s_texture");

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mCamProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
        //Draw Overlay
        //---------------------------------------------------------------------------
        GLES20.glUseProgram(mOverlayProgram);

//        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        mPositionHandle = GLES20.glGetAttribLocation(mOverlayProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //prepare vertex buffer
        if (!vertexBufferPopulated) {
            prepareVertexBuffer(imageSize);
        }
        vertexBufferOverlay.position(0);
        for (int i= 0; i < squareVertices.length; i++) {
            vertexBufferOverlay.put(squareVertices[i] * overlaySize);
        }

        overlaySize = overlaySize + overlaySizeStep;
        if (overlaySize > 1.5 || overlaySize < 0.5)
            overlaySizeStep = -overlaySizeStep;
        vertexBufferOverlay.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,vertexStride, vertexBufferOverlay);

        mTextureCoordHandle = GLES20.glGetAttribLocation(mOverlayProgram, "a_TexCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        textureVerticesBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,vertexStride, textureVerticesBuffer);

        mTextureUniformHandle = GLES20.glGetUniformLocation(mOverlayProgram, "u_Texture");
        GLES20.glUniform1i(mTextureUniformHandle, 1);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mOverlayProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        drawListBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);

    }
}