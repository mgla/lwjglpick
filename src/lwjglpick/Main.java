package lwjglpick;

import java.util.Hashtable;
import java.util.Random;
import java.util.Enumeration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBBufferObject;
import org.lwjgl.opengl.ARBVertexBufferObject;


public class Main {
    private int width = 640;
    private int height = 480;
    private float fov = 60.0f;
    private Vector4f cameraPosition = new Vector4f(7.0f, 5.0f, 5.0f, 1.0f);
    
    // Specifications from exercise04 solution
    private final int TEXTURE_CUBE_DUMMY = 0;
    private final int TEXTURE_CUBE_METAL = 1;
    private final int TEXTURE_TEX1 = 2;
    private final int TEXTURE_TEX2 = 3;
    private final int TEXTURE_TEX3 = 4;
    private final int TEXTURE_TEX4 = 5;
    private final int TEXTURE_YINGYANG = 6;
    private final int TEXTURE_COUNT = 7;
    private float GL_VERSION;
    //3 for position, 3 for normals, 2 for texture coordinates
    private final int SIZE_OF_DATA = 8;
    private final int SIZE_OF_FLOAT = 4;
    private int shaderProgram = 0;
    private final int LIGHT_RED = 0;
    private final int LIGHT_GREEN = 1;
    private final int LIGHT_BLUE = 2;
    private final int LIGHT_MAX = 3;
    private Vector4f[] lightPosition = new Vector4f[LIGHT_MAX];
    private Vector4f[] lightColor = new Vector4f[LIGHT_MAX];
    
    
    private IntBuffer textureid = IntBuffer.allocate(TEXTURE_COUNT);
    private final int GEOMETRY_CUBE = 0;
    private IntBuffer vboid = IntBuffer.allocate(1);
    
    // Class for player score management
    private Player player = new Player();
    private Random rand;

    
    // Time between spawn of two cubes in milliseconds
    private int spawnTime = 800;

    /**
     * Entry point for the java program.
     */
    public static void main(String[] args) {
        new Main().execute();
        System.exit(0);
    }

    /**
     * Execution of the initialization and the render loop. The display
     * will be destroyed when the close button is hit and the loop stops.
     */
    private void execute() {
        try {
            init();
        } catch (LWJGLException oLWJGLException) {
            oLWJGLException.printStackTrace();
            System.out.println("Failed to initialize Application.");
            return;
        }
        loop();
        destroy();
    }
    /**
     * This method converts a String to a ByteBuffer.
     * @param string The string that has to be converted.
     * @return A zero terminated ByteBuffer of the string in UTF-8.
     */
    private ByteBuffer getByteBufferFromString(String string)
    {
        string += "\0";
        try
        {
            return ByteBuffer.wrap(string.getBytes("UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method to load vertex and fragment shaders. <br>
     * @param vertFile The file name of the vertex shader.
     * @param fragFile The file name of the fragment shader.
     * @return Program context of the shader.
     */
    private int loadShaders(String vertFile, String fragFile)
    {
    	// CORRECTION
        ByteBuffer vertContent = getFileContent(vertFile);
        ByteBuffer fragContent = getFileContent(fragFile);

        shaderProgram = -1;

        int vertShader = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
        int fragShader = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

        ARBShaderObjects.glShaderSourceARB(vertShader, vertContent);
        ARBShaderObjects.glShaderSourceARB(fragShader, fragContent);

        ARBShaderObjects.glCompileShaderARB(vertShader);
        ARBShaderObjects.glCompileShaderARB(fragShader);

        shaderProgram = ARBShaderObjects.glCreateProgramObjectARB();

        ARBShaderObjects.glAttachObjectARB(shaderProgram, vertShader);
        ARBShaderObjects.glAttachObjectARB(shaderProgram, fragShader);

        ARBShaderObjects.glLinkProgramARB(shaderProgram);

        ARBShaderObjects.glDeleteObjectARB(vertShader);
        ARBShaderObjects.glDeleteObjectARB(fragShader);

        return shaderProgram;
    }

    /**
     * This returns the contents of a file as a ByteBuffer.
     * @param fileName The file name of which the content should be loaded.
     * @return The content of the file as ByteBuffer or null if the loading
     * failed.
     */
    private ByteBuffer getFileContent(String fileName)
    {
        File file = new File(fileName);
        try
        {
            FileInputStream input = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            input.read(data);
            input.close();
            return ByteBuffer.wrap(data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * This is the initialization of the open gl window. Here we set up
     * the display with its viewport and the projection matrix.<br>
     */
    private void init() throws LWJGLException {
        Display.setLocation(
                (Display.getDisplayMode().getWidth() - width) / 2,
                (Display.getDisplayMode().getHeight() - height) / 2);
        Display.setDisplayMode(new DisplayMode(width, height));
        Display.setTitle("Computer Graphics");
        Display.create();


        //Setup OpenGL
        for(int i = 0; i < LIGHT_MAX; i++)
        {
            lightPosition[i] = new Vector4f();
        }
        lightColor[LIGHT_RED] = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
        lightColor[LIGHT_GREEN] = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
        lightColor[LIGHT_BLUE] = new Vector4f(0.0f, 0.0f, 1.0f, 1.0f);

        //We don't change the material over time anymore
        //so set it fixed to white
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, FloatBuffer.wrap(new float[] {0.2f, 0.2f, 0.2f,  1.0f}));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, FloatBuffer.wrap(new float[] {0.8f, 0.8f, 0.8f,  1.0f}));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, FloatBuffer.wrap(new float[] {1.0f, 1.0f, 1.0f,  1.0f}));
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, FloatBuffer.wrap(new float[] {0.0f, 0.0f, 0.0f,  1.0f}));
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 64.0f);

        //Setup OpenGL
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_NORMALIZE);

        //Here we use the build-in OpenGL states of the lights. This could also
        //be done by own uniform variables in the shader.
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, FloatBuffer.wrap(new float[] {lightColor[LIGHT_RED].x, lightColor[LIGHT_RED].y, lightColor[LIGHT_RED].z, lightColor[LIGHT_RED].w}));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, FloatBuffer.wrap(new float[] {lightColor[LIGHT_RED].x, lightColor[LIGHT_RED].y, lightColor[LIGHT_RED].z, lightColor[LIGHT_RED].w}));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, FloatBuffer.wrap(new float[] {lightColor[LIGHT_RED].x, lightColor[LIGHT_RED].y, lightColor[LIGHT_RED].z, lightColor[LIGHT_RED].w}));
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, FloatBuffer.wrap(new float[] {lightColor[LIGHT_GREEN].x, lightColor[LIGHT_GREEN].y, lightColor[LIGHT_GREEN].z, lightColor[LIGHT_GREEN].w}));
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, FloatBuffer.wrap(new float[] {lightColor[LIGHT_GREEN].x, lightColor[LIGHT_GREEN].y, lightColor[LIGHT_GREEN].z, lightColor[LIGHT_GREEN].w}));
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, FloatBuffer.wrap(new float[] {lightColor[LIGHT_GREEN].x, lightColor[LIGHT_GREEN].y, lightColor[LIGHT_GREEN].z, lightColor[LIGHT_GREEN].w}));
        GL11.glEnable(GL11.GL_LIGHT1);
        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_AMBIENT, FloatBuffer.wrap(new float[] {lightColor[LIGHT_BLUE].x, lightColor[LIGHT_BLUE].y, lightColor[LIGHT_BLUE].z, lightColor[LIGHT_BLUE].w}));
        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_DIFFUSE, FloatBuffer.wrap(new float[] {lightColor[LIGHT_BLUE].x, lightColor[LIGHT_BLUE].y, lightColor[LIGHT_BLUE].z, lightColor[LIGHT_BLUE].w}));
        GL11.glLight(GL11.GL_LIGHT2, GL11.GL_SPECULAR, FloatBuffer.wrap(new float[] {lightColor[LIGHT_BLUE].x, lightColor[LIGHT_BLUE].y, lightColor[LIGHT_BLUE].z, lightColor[LIGHT_BLUE].w}));
        GL11.glEnable(GL11.GL_LIGHT2);
        GL11.glEnable(GL11.GL_LIGHTING);

        //Load shader from vert and frag files
        shaderProgram = loadShaders("res/shaders/phong.vert", "res/shaders/phong.frag");
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        
        //Generate the VBO buffer
        if(GL_VERSION >= 1.5) {
            GL15.glGenBuffers(vboid);
    	}
        else {        
            ARBVertexBufferObject.glGenBuffersARB(vboid);
    	}
        // MasterCube
        
        ObjImporter objImporter = new ObjImporter("res/geometry/Dummy.obj");
        if(GL_VERSION >= 1.5)
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid.get(GEOMETRY_CUBE));
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, createData(objImporter), GL15.GL_STATIC_DRAW);
        }
        else
        {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboid.get(GEOMETRY_CUBE));
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, createData(objImporter), ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
        
        GL_VERSION = Float.valueOf(GL11.glGetString(GL11.GL_VERSION).substring(0, 3)).floatValue();
        System.out.println("OpenGL Version: " + GL_VERSION);
        
        //Create the needed texture contexts
        GL11.glGenTextures(textureid);
        
        TextureImporter textureImporter = null;
        
        //The textures
        Hashtable <Integer, String>hash = new Hashtable();
        hash.put(TEXTURE_CUBE_DUMMY, "Dummy.jpg");
        hash.put(TEXTURE_CUBE_METAL, "metal.jpg");
        hash.put(TEXTURE_TEX1, "tex1.jpg");
        hash.put(TEXTURE_TEX2, "tex2.jpg");
        hash.put(TEXTURE_TEX3, "tex3.jpg");
        hash.put(TEXTURE_TEX4, "tex4.jpg");        
        hash.put(TEXTURE_YINGYANG, "ying-yang.jpg");
        
        Enumeration <Integer> e = hash.keys();
        
        while (e.hasMoreElements()) {
            Integer key = (Integer) e.nextElement();
            textureImporter = new TextureImporter("res/textures/"+hash.get(key));
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureid.get(key.intValue()));
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, textureImporter.getWidth(), textureImporter.getHeight(), 0, textureImporter.hasAlpha() ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, textureImporter.getData());
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        //Setup Projection and view port
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(fov, (float) width / (float) height, 0.1f, 100.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
        // Random generator
        this.rand = new Random();
        
    }

    /**
     * This is the render loop. It will stop, when the close button of the
     * Display is hit.
     */
    private void loop() {
        long startMillis = System.currentTimeMillis();
        long fpsCheck = System.currentTimeMillis() + 1000;
        long fps = 0;

        long time, oldtime;
        time = oldtime = 0;
        while (!Display.isCloseRequested()) {
        	
            // spawn additional Cubes if the time has come        	
            time = (System.currentTimeMillis() - startMillis);
            if ((time - oldtime) >= spawnTime) {
            	oldtime = time;
            	// choose random Texture
            	int tex = textureid.get(this.rand.nextInt(TEXTURE_COUNT));
                Cube c = new Cube(tex);                
                
                // c.setColor(new float[]{r.nextFloat(), r.nextFloat(), r.nextFloat(), 0}); 

                World.getInstance().addCube(c);

            }
            //Setting the lights before all renderings
            // CORRECTION
            float lightRadius = 4.0f;
            lightPosition[LIGHT_RED].set((float)Math.sin(0.001f * time) * lightRadius, (float)Math.cos(0.001f * time) * lightRadius, 0.0f, 1.0f);
            lightPosition[LIGHT_GREEN].set((float)Math.sin(0.002f * time) * lightRadius, 0.0f, (float)Math.cos(0.002f * time) * lightRadius, 1.0f);
            lightPosition[LIGHT_BLUE].set(0.0f, (float)Math.sin(0.004f * time) * lightRadius, (float)Math.cos(0.004f * time) * lightRadius, 1.0f);

            GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, FloatBuffer.wrap(
                new float[] {lightPosition[LIGHT_RED].x, lightPosition[LIGHT_RED].y, lightPosition[LIGHT_RED].z, lightPosition[LIGHT_RED].w}));
            GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, FloatBuffer.wrap(
                new float[] {lightPosition[LIGHT_GREEN].x, lightPosition[LIGHT_GREEN].y, lightPosition[LIGHT_GREEN].z, lightPosition[LIGHT_GREEN].w}));
            GL11.glLight(GL11.GL_LIGHT2, GL11.GL_POSITION, FloatBuffer.wrap(
                new float[] {lightPosition[LIGHT_BLUE].x, lightPosition[LIGHT_BLUE].y, lightPosition[LIGHT_BLUE].z, lightPosition[LIGHT_BLUE].w}));

            //Clear to the background color
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            //Setting the current object transformation
            GL11.glLoadIdentity();

            //View matrix
            GLU.gluLookAt(
                    cameraPosition.x, cameraPosition.y, cameraPosition.z,
                    0.0f, 0.0f, -5.0f,
                    0.0f, 1.0f, 0.0f);          
            
            // Choose texture

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureid.get(TEXTURE_CUBE_DUMMY));
            
            if(GL_VERSION >= 1.5)
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid.get(GEOMETRY_CUBE));
            else
                ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboid.get(GEOMETRY_CUBE));
			
            // Definiert ein Array mit Texturkoordinaten 

            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, SIZE_OF_DATA * SIZE_OF_FLOAT, 6 * SIZE_OF_FLOAT);
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			
            if(GL_VERSION >= 1.5) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            } else {
                ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
            }

            World.getInstance().draw();
            
            
            // End of texturing
           // GL11.glDisable(GL11.GL_LIGHTING);
            

            // Zeichne Grid
            GL11.glColor3f(1.0f, 1.0f, 1.0f);
            GL11.glLineWidth(2.0f);
            Primitives.drawGrid(20, 1.0f);

            //Swap the buffers
            Display.update();

            mouseHandler();

            //Count the frames per second
            long currentMillis = System.currentTimeMillis();
            if (fpsCheck > currentMillis) {
                fps++;
            } else {
                float timeUsed = (float) (fpsCheck - currentMillis + 1000);
                fpsCheck = currentMillis + 1000;
                Display.setTitle("Computer Graphics (" + ((float) fps / (timeUsed / 1000.0f)) + " fps)" + " Player points : " + player.getPoints());
                fps = 0;
            }
        }
    }



    private void mouseHandler() {
        if (!Mouse.isButtonDown(0)) {
            return;
        }
        // Bestimme Objekt bei Mausklick
        Vector2f mousePos = new Vector2f(Mouse.getX(), Mouse.getY());
        Cube c = World.getInstance().objectAtScreenPosition(mousePos);
        if (c == null) {
            return;
        }
        if (c.getId() == 0) {
            player.addPoints(1);
        } else {
            player.addPoints(2);
        }
        World.getInstance().removeCube(c.getId());

    }

    /**
     * Destroy the display. This is a simple method right now, but later we can
     * clean up other things within this function. <br>
     */
    private void destroy() {
        ARBBufferObject.glDeleteBuffersARB(vboid);
        Display.destroy();
    }
    
    /**
     * This method creates the VBO data of a loaded obj file.
     * @param objImporter The obj file that should be used for the creation
     * of the data buffer.
     * @return A buffer with vertex positions, normals and texture coordinates
     * that can be used for a VBO.
     */
    private FloatBuffer createData(ObjImporter objImporter)
    {
        FloatBuffer arrayBuffer = FloatBuffer.allocate(SIZE_OF_DATA * 3 * objImporter.getTriangleCount());
        FloatBuffer vertexBuffer = objImporter.getVertexBuffer();
        FloatBuffer normalBuffer = objImporter.getNormalBuffer();
        FloatBuffer texCoordBuffer = objImporter.getTexCoordBuffer();
        IntBuffer dataIndexBuffer = objImporter.getDataIndexBuffer();
        
        int index = 0;
        for(int i = 0; i < 3 * objImporter.getTriangleCount(); i++)
        {
            index = 3 * dataIndexBuffer.get(objImporter.getDataSize() * i);
            arrayBuffer.put(SIZE_OF_DATA * i, vertexBuffer.get(index));
            arrayBuffer.put(SIZE_OF_DATA * i + 1, vertexBuffer.get(index + 1));
            arrayBuffer.put(SIZE_OF_DATA * i + 2, vertexBuffer.get(index + 2));
            if(objImporter.getNormalPosition() < 0)
            {
                arrayBuffer.put(SIZE_OF_DATA * i + 3, 0.0f);
                arrayBuffer.put(SIZE_OF_DATA * i + 4, 0.0f);
                arrayBuffer.put(SIZE_OF_DATA * i + 5, 0.0f);
            }
            else
            {
                index = 3 * dataIndexBuffer.get(objImporter.getDataSize() * i + objImporter.getNormalPosition());
                arrayBuffer.put(SIZE_OF_DATA * i + 3, normalBuffer.get(index));
                arrayBuffer.put(SIZE_OF_DATA * i + 4, normalBuffer.get(index + 1));
                arrayBuffer.put(SIZE_OF_DATA * i + 5, normalBuffer.get(index + 2));
            }
            if(objImporter.getTexCoordPosition() < 0)
            {
                arrayBuffer.put(SIZE_OF_DATA * i + 6, 0.0f);
                arrayBuffer.put(SIZE_OF_DATA * i + 7, 0.0f);
            }
            else
            {
                index = 3 * dataIndexBuffer.get(objImporter.getDataSize() * i + objImporter.getTexCoordPosition());
                arrayBuffer.put(SIZE_OF_DATA * i + 6, texCoordBuffer.get(index));
                arrayBuffer.put(SIZE_OF_DATA * i + 7, texCoordBuffer.get(index + 1));
            }
        }
        return arrayBuffer;
    }
    
}
