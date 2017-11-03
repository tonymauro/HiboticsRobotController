package com.example.apm1.androidrobotcontroller;

/**
 * Created by WILLIAM LIN on 11/1/2017 for the Android Robot Controller.
 * THIS IS FOR THE RENDERER
 */

import com.vuforia.State;

public interface RendererC {

    void renderFrame(State state, float[] projectionMatrix);

}
