package lwjglpick;

import org.lwjgl.util.vector.Vector3f;

public class Cube {
    private Vector3f center;
    private Vector3f initalPosition;
    private float size = 1;
    private float[] color = new float[4];
    private int id = 0;
    private int textureID = 0;
    
    
    
    public Cube(int textureID){
        center = new Vector3f(0,0,0);
        this.initalPosition = new Vector3f(center);
        this.textureID = textureID;
    }
    
    public Cube(Vector3f initialPosition, int textureID){
        this.initalPosition = initialPosition;
        this.center = new Vector3f(initialPosition);
        this.textureID = textureID;
    }
      
    public Cube(Vector3f initialPosition, float size, int textureID){
        this.initalPosition = initialPosition;
        this.center = new Vector3f(initialPosition);
        this.size = size;
        this.textureID = textureID;
    }

    public Cube(Vector3f initialPosition, float size, float[] color, int textureID){ 
        this.initalPosition = initialPosition;
        this.center = new Vector3f(initialPosition);
        this.size = size;
        this.color = color.clone();
        this.textureID = textureID;
    }    
    
    public Vector3f getCenter(){
        return center;
    }
    public int getTextureID()
    {
    	return this.textureID;
    }
    
    public float getSize(){
        return size;
    }
    
    public float[] getColor(){
        return color;
    }
    
    public void setToInitialPosition(){
        this.center = new Vector3f(initalPosition);
    }
    
    public void setCenter(Vector3f center){
        this.center = center;
    }
    
    public void setSize(float size){
        this.size = size;
    }
    
    public void setColor(float[] color){
        this.color = color.clone();
    }
    
    public void setId(int id){
        this.id = id;
    }
    
    public int getId(){
        return id;
    }
    
    public float getAlpha(){
        return color[3];
    }
    
    public void setAlpha(float alpha){
        color[3] = alpha;
    }
    
    
}
