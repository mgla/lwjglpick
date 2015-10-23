/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lwjglpick;

import java.nio.FloatBuffer;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;

public class Converter {

	/**
	 * Prepare a 3x3 matrix of lwjgl for loading it. <br>
	 */
	public static FloatBuffer getBufferFromMatrix(Matrix3f m)
	{
		return FloatBuffer.wrap(new float[] {
			m.m00, m.m01, m.m02,  0.0f,
			m.m10, m.m11, m.m12,  0.0f,
			m.m20, m.m21, m.m22,  0.0f,
			 0.0f,  0.0f,  0.0f,  1.0f});
		
	}
        
        
        public static FloatBuffer getBufferFromMatrix(Matrix4f m) {
            return FloatBuffer.wrap( new float[] {
                m.m00, m.m01, m.m02, m.m03, 
                m.m10, m.m11, m.m12, m.m13,
                m.m20, m.m21, m.m22, m.m23,
                m.m30, m.m31, m.m32, m.m33
            });
        }    
    
}
