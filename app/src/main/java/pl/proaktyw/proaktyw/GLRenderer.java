package pl.proaktyw.proaktyw;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * Created by Maciej Szalek on 2018-03-22.
 */

public class GLRenderer implements GLSurfaceView.Renderer {

        private Cube cube;
        private boolean mTranslucentBackground;
        private float angleCube = 0;     // rotational angle in degree for cube
        private float speedCube = 0.5f;


        public GLRenderer( boolean useTranslucentBackground,Context context) {
            mTranslucentBackground = useTranslucentBackground;
            cube = new Cube(context);
        }

        // Call back when the surface is first created or re-created.
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            gl.glEnable(GL11.GL_CULL_FACE);   // Enables depth-buffer for hidden surface removal
            gl.glDepthFunc(GL11.GL_LEQUAL);    // The type of depth testing to do
            gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);  // nice perspective view
            gl.glShadeModel(GL11.GL_SMOOTH);   // Enable smooth shading of color
            gl.glDisable(GL11.GL_DITHER);      // Disable dithering for better performance

            // Setup Texture, each time the surface is created
            cube.loadTexture(gl);             // Load images into textures
            gl.glEnable(GL10.GL_TEXTURE_2D);  // Enable texture
        }

        // Call back after onSurfaceCreated() or whenever the window's size changes
        public void onSurfaceChanged(GL10 gl, int width, int height) {

            // Set the viewport (display area) to cover the entire window
            gl.glViewport(0, 0, width, height);

            if (height == 0) height = 1;   // To prevent divide by zero
            // Perspective projection matrix
            float ratio = (float)width / height;

            // Setup perspective projection, with aspect ratio matches viewport
            gl.glMatrixMode(GL11.GL_PROJECTION); // Select projection matrix
            gl.glLoadIdentity();                 // Reset projection matrix
            // Use perspective projection
            GLU.gluPerspective(gl, 45, ratio, 0.1f, 100.f);

            gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
            gl.glLoadIdentity();
        }

        // Call back to draw the current frame.
        public void onDrawFrame(GL10 gl) {
             // Clear color and depth buffer
            gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            // ----- Render the Cube -----
            gl.glMatrixMode(GL11.GL_MODELVIEW);
            gl.glLoadIdentity();                  // Reset the model-view matrix

            gl.glTranslatef(0.0f, 0.0f, -10.0f);  // Translate into the screen

            gl.glRotatef(angleCube, 0.15f, 1.0f, 0.3f); // Rotate
            cube.draw(gl);

            // Update the rotational angle after each refresh.
            angleCube += speedCube;
        }

}

