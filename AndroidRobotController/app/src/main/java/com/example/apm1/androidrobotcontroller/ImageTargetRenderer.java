package com.example.apm1.androidrobotcontroller;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vuforia;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by WILLIAM LIN on 10/16/2017 for the Android Robot Controller.
 * I COPY PASTED THIS FROM GITHUB
 * https://github.com/mattrayner/cordova-plugin-vuforia/blob/master/src/android/java/com/mattrayner/vuforia/app/ImageTargetRenderer.java
 */


public class ImageTargetRenderer implements GLSurfaceView.Renderer {

    private static final String LOGTAG = "ImageTargetRenderer";

    private VuforiaClass vClass;
    private VuforiaActivity mActivity;

    private Renderer mRenderer;

    boolean mIsActive = false;

    String mTargets = "";

    public ImageTargetRenderer(VuforiaActivity activity,
                               VuforiaClass session, String targets)
    {
        mActivity = activity;
        vClass = session;
        mTargets = targets;
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vClass.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vClass.onSurfaceChanged(width, height);
    }


    // Function for initializing the renderer.
    private void initRendering()
    {
        mRenderer = Renderer.getInstance();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);


    }


    // The render function.
    private void renderFrame()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = mRenderer.begin();
        mRenderer.updateVideoBackgroundTexture();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();

            String obj_name = trackable.getName();

            Log.d(LOGTAG, "MRAY :: Found: " + obj_name);

            /**
             * Our targets array has been flattened to a string so will equal something like: ["one", "two"]
             * So, to stop weak matches such as 'two' within ["onetwothree", "two"] we wrap the term in
             * speech marks such as '"two"'
             **/
            Boolean looking_for = mTargets.toLowerCase().contains("\"" + obj_name.toLowerCase() + "\"");

            if (looking_for)
            {
                mActivity.imageFound(obj_name);
            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        mRenderer.end();
    }

    public void updateTargetStrings(String targets) {
        mTargets = targets;
    }
}
