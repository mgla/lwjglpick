package lwjglpick;

import org.lwjgl.util.vector.Vector3f;


public class Plane {
    
    private float a,b,c,d;
    
    public enum Side {
        LEFT, 
        RIGHT, 
        EQUAL
    }

    // a x + b y + c z - d = 0
    public Plane(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    
    
    public Side point_cmp_to_plane(Vector3f v) {
        float value = v.x * a + v.y * b + v.z * c - d;
        if(value > 0) {
            return Side.RIGHT;
        } else {
            if(value < 0) {
                return Side.LEFT;
            } else {
                return Side.EQUAL;
            }
        }
    } 
    
    
}
