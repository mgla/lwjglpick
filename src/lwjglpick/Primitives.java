package lwjglpick;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

/**
 * Some provided data for rendering and testing VBOs.
 */
class Primitives
{
	/**
	 * Create vertex data for a cube. The data will be structured as follows: (VxVyVzNxNyNz)* where VxVyVz are the vertex positions and NxNyNz are the normal vectors.
	 * @return Vertices and normals of a cube.
	 */
	public static FloatBuffer createCubeData() 
	{
		return FloatBuffer.wrap(new float[] {
			-1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
			-1.0f,  1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
			 1.0f,  1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
			 
			 1.0f,  1.0f,  1.0f,  0.0f,  1.0f,  0.0f,
			 1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
			-1.0f,  1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
			
			-1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f,
			 1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f,
			 1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f,
			 
			 1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f,
			-1.0f, -1.0f,  1.0f,  0.0f, -1.0f,  0.0f,
			-1.0f, -1.0f, -1.0f,  0.0f, -1.0f,  0.0f,
			 
			-1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 
			 1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			-1.0f,  1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
			 
			-1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			-1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			 1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			 
			 1.0f,  1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			 1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			-1.0f, -1.0f, -1.0f,  0.0f,  0.0f, -1.0f,
			
			 1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f,  1.0f, -1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
			 
			 1.0f,  1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f, -1.0f,  1.0f,  1.0f,  0.0f,  0.0f,
			 1.0f, -1.0f, -1.0f,  1.0f,  0.0f,  0.0f,
						
			-1.0f, -1.0f, -1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f,  1.0f,  1.0f, -1.0f,  0.0f,  0.0f,
			
			-1.0f,  1.0f,  1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f,  1.0f, -1.0f, -1.0f,  0.0f,  0.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  0.0f,  0.0f});
	}
	
	/**
	* Render intermediate cube.
	*/
	public static void drawCube() 
	{
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glNormal3f(0.0f, 1.0f, 0.0f);
			GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			
			GL11.glNormal3f(0.0f, -1.0f, 0.0f);
			GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
			
			GL11.glNormal3f(0.0f, 0.0f, 1.0f);
			GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
			
			GL11.glNormal3f(0.0f, 0.0f, -1.0f);
			GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(1.0f, -1.0f, -1.0f);
			
			GL11.glNormal3f(1.0f, 0.0f, 0.0f);
			GL11.glVertex3f(1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(1.0f, 1.0f, -1.0f);
			GL11.glVertex3f(1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(1.0f, -1.0f, 1.0f);
			
			GL11.glNormal3f(-1.0f, 0.0f, 0.0f);
			GL11.glVertex3f(-1.0f, -1.0f, -1.0f);
			GL11.glVertex3f(-1.0f, -1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, 1.0f);
			GL11.glVertex3f(-1.0f, 1.0f, -1.0f);
		GL11.glEnd();
	}
	
	/**
     * Render immediate grid.
     * @param repetitions Number of lines to be drawn in each direction.
     * @param increment Distance between the lines.
     */
    public static void drawGrid(int repetitions, float increment) 
    {
        float size = repetitions * increment;
        GL11.glBegin(GL11.GL_LINES);
            for(int i = -repetitions; i <= repetitions; i++)
            {
                GL11.glVertex3f(-size, -1.0f, i * increment);
                GL11.glVertex3f( size, -1.0f, i * increment);
            }
            for(int i = -repetitions; i <= repetitions; i++)
            {
                GL11.glVertex3f(i * increment, -1.0f, -size);
                GL11.glVertex3f(i * increment, -1.0f,  size);
            }
        GL11.glEnd();
    }
}
