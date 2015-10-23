/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lwjglpick;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class World {

    private static World instance = new World();
    //! how long the object travels from begin to end position
    private int objectDuration = 1500;
    // object entrance point
    private Matrix4f objectEntrance = new Matrix4f();
    private Vector3f direction = new Vector3f(1f / objectDuration, 0, -1f / objectDuration);
    private Matrix4f movement = new Matrix4f();
    private Vector3f center = new Vector3f(0, 0, 0);
    private float xlen = 25;
    private float ylen = 25;
    private float zlen = 25;
    private TreeMap<Integer, Cube> objects = new TreeMap<Integer, Cube>();
    private IntBuffer vboObjectIds;
    private final int maxCubes = 100;
    
    // StartID for Cubes
    private int cubeId = 0;
    
    // Fading speed of a cube - the smaller the slower.
    private float fadingSpeed = 1.0f / 100;
    private LinkedList<Cube> fadingCubesOut = new LinkedList<Cube>();
    private LinkedList<Cube> fadingCubesIn = new LinkedList<Cube>();

    private World() {
        //setup inital / final position        
        objectEntrance.m30 = center.x - 2; // - xlen / 2f + 1;
        movement.load(objectEntrance);

        vboObjectIds = IntBuffer.allocate(maxCubes);
        ARBBufferObject.glGenBuffersARB(vboObjectIds);
    }

    public static World getInstance() {
        return instance;
    }

    /**
     * Adds a cube at the entrance point
     */
    public void addCube(Cube c) {
    	// CORRECTION
        ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboObjectIds.get(cubeId));
        ARBBufferObject.glBufferDataARB(GL15.GL_ARRAY_BUFFER, Primitives.createCubeData(), GL15.GL_STATIC_DRAW);
        c.setId(cubeId);
        c.setAlpha(cubeId / (float)maxCubes);
        //objects.put(cubeId, c);
        fadingCubesIn.add(c);
        cubeId++;
        if (cubeId >= maxCubes) {
            cubeId = 0;
        }
    }

    /**
     * Removes Cube from the active objects.
     */
    public void removeCube(int cubeId) {
        //start fading the cube
        Cube c = objects.get(cubeId);
        c.setAlpha(1);
        fadingCubesOut.add(c);
        objects.remove(cubeId);
    }

    /**
     * Remove a cube from the OpenGL buffer
     * @param cubeId
     */
    private void removeCubeGL(int cubeId) {
        IntBuffer b = IntBuffer.allocate(1);
        b.put(0, vboObjectIds.get(cubeId));
        ARBBufferObject.glDeleteBuffersARB(b);
    }

    /**
     * Fades a cube out a bit
     */
    public void fadeCubeOut(Cube c) {
    	// CORRECTION
        c.setAlpha(c.getAlpha() - fadingSpeed);
    }

    /**
     * Fades a cube in a bit
     */
    public void fadeCubeIn(Cube c){
    	// CORRECTION
        c.setAlpha(c.getAlpha() + fadingSpeed);
    }
    
    /**
     * Tests if an objects is within the world space
     */
    public boolean objectInWorld(Cube c) {
        Vector3f objCenter = c.getCenter();
        float objSize = c.getSize();
        if (Math.abs(objCenter.x - center.x) < (xlen - objSize) / 2f) {
            if (Math.abs(objCenter.y - center.y) < (ylen - objSize) / 2f) {
                if (Math.abs(objCenter.z - center.z) < (zlen - objSize) / 2f) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Draws active and fading objects 
     */
    public void drawObject(Cube cube) {
    	// CORRECTION

        final int numberOfTriangles = 12;
        final int SIZE_OF_DATA = 6;
        final int SIZE_OF_FLOAT = 4;

        
        ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, vboObjectIds.get(cube.getId()));
        
        float[] cubeColor = cube.getColor();
        GL11.glColor4f(cubeColor[0], cubeColor[1], cubeColor[2], cubeColor[3]);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, cube.getTextureID());

        //GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        ARBMultitexture.glClientActiveTextureARB(GL13.GL_TEXTURE0);
        
        GL11.glVertexPointer(3, GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 0);
        GL11.glNormalPointer(GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 3 * SIZE_OF_FLOAT);
        
        GL11.glPushMatrix();
        	GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3 * numberOfTriangles);
        GL11.glPopMatrix();
        
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
    
    /**
     * Moves object by direction.
     */
    public void moveObject(Cube c) {
    	// CORRECTION
        Matrix4f mv = new Matrix4f();
        mv.load(objectEntrance);
        //move cube
        c.setCenter(Vector3f.add(c.getCenter(), direction, null));
        mv.translate(c.getCenter());
        GL11.glPushMatrix();
        GL11.glMultMatrix(Converter.getBufferFromMatrix(mv));
        drawObject(c);
        GL11.glPopMatrix();
        ARBBufferObject.glBindBufferARB(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void draw() {
    	// CORRECTION
        Iterator<Cube> it = objects.values().iterator();
        LinkedList<Integer> cubesToFade = new LinkedList<Integer>();
        while (it.hasNext()) {
            Cube c = it.next();
            // Objects within world space are moved
            if (objectInWorld(c)) {
                moveObject(c);
            } else {
            // Objects out of world space a set to fade
                cubesToFade.add(c.getId());
            }
        }
        
        // remove fading objects from the list of active objects
        if (cubesToFade.size() > 0) {
            Integer[] cubesArray = new Integer[cubesToFade.size()];
            cubesToFade.toArray(cubesArray);
            for (int i = 0; i < cubesArray.length; ++i) {
                removeCube(cubesArray[i]);
            }
        }


        //perform fading
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ListIterator<Cube> fit = fadingCubesOut.listIterator();
        while (fit.hasNext()) {
            Cube c = fit.next();
            fadeCubeOut(c);
            GL11.glPushMatrix();
            Matrix4f m = new Matrix4f();
            m.load(objectEntrance);
            m.translate(c.getCenter());
            GL11.glMultMatrix(Converter.getBufferFromMatrix(m));
            drawObject(c);
            GL11.glPopMatrix();
            if(c.getColor()[3] <= 0){
                removeCubeGL(c.getId());
                fit.remove();
            }
        }
        fit = fadingCubesIn.listIterator();
        while(fit.hasNext()){
            Cube c = fit.next();
            fadeCubeIn(c);
            GL11.glPushMatrix();
            Matrix4f m = new Matrix4f();
            m.load(objectEntrance);
            m.translate(c.getCenter());
            GL11.glMultMatrix(Converter.getBufferFromMatrix(m));
            drawObject(c);
            GL11.glPopMatrix();
            if(c.getColor()[3] >= 0){
                objects.put(c.getId(), c);
                fit.remove();
            }            
        }
        GL11.glDisable(GL11.GL_BLEND);


    }
    
    /**
     * Get Cube by Screen position - the picking function.
     */
    public Cube objectAtScreenPosition(Vector2f pos) {
    	// CORRECTION
        FloatBuffer color = BufferUtils.createFloatBuffer(4);
        //in pixels
        final int mouseHeight = 1;
        final int mouseWidth = 1;
        
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glPushClientAttrib(GL11.GL_CLIENT_PIXEL_STORE_BIT);

        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, GL11.GL_FALSE);
        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, GL11.GL_FALSE);
        GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, GL11.GL_FALSE);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        
        draw();

        GL11.glReadPixels((int) pos.x, (int) pos.y, mouseWidth, mouseHeight, GL11.GL_RGBA, GL11.GL_FLOAT, color);
        GL11.glPopClientAttrib();
        
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);

        Iterator<Cube> it = objects.values().iterator();
        float[] cubeColor;
        Cube c;
        int alpha = Math.round(color.get(3) * maxCubes);

        while (it.hasNext()) {
            c = it.next();
            cubeColor = c.getColor();
            if (alpha == (int) (cubeColor[3] * maxCubes)) {
                return c;
            }
        }
        return null;
    }
}
