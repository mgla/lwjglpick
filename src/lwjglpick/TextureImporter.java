package lwjglpick;

/**
 * This file is taken from Exercise04 Solution. 
 */


import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;
   
import javax.imageio.ImageIO;

import org.lwjgl.util.XPMFile;

/**
 * The TextureImporter just reads in an image file and stores the
 * data into a buffer. The usage should be like this:<br>
 * <code><br>
 * TextureImporter tex = new TextureImporter("SomeFile.jpg");<br>
 * ByteBuffer data = tex.getData();<br>
 * int width = tex.getWidth();<br>
 * int height = tex.getHeighth();<br>
 * boolean alpha = tex.hasAlpha();<br>
 * </code><br>
 * Then you can upload the data to the graphics card. Please note 
 * that the texture will be power of two automatically!
 */
public class TextureImporter
{    
    private ByteBuffer data = null;
    private int width = 2;
    private int height = 2;
    private boolean hasAlpha = false;

    /**
     * Creates the TextureImporter for the given filename and fills the buffer.
     * @param fileName The image file that should be loaded.
     */
    public TextureImporter(String fileName)
    {
        File file = new File(fileName);

        try
        {
            BufferedImage image = ImageIO.read(file);
            
            //Get power of two texture size
            while(width < image.getWidth())
                width *= 2;
            while(height < image.getHeight())
                height *= 2;
            hasAlpha = image.getColorModel().hasAlpha();
            
            BufferedImage texture;
            if(hasAlpha)
            {
                texture = new BufferedImage(
                    new ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        new int[] {8, 8, 8, 8},
                        true,
                        false,
                        ComponentColorModel.TRANSLUCENT,
                        DataBuffer.TYPE_BYTE), 
                    Raster.createInterleavedRaster(
                        DataBuffer.TYPE_BYTE, 
                        width, 
                        height, 
                        4, 
                        null), 
                    false, 
                    new Hashtable());
            }
            else
            {
                texture = new BufferedImage(
                    new ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        new int[] {8, 8, 8, 0},
                        false,
                        false,
                        ComponentColorModel.OPAQUE,
                        DataBuffer.TYPE_BYTE), 
                    Raster.createInterleavedRaster(
                        DataBuffer.TYPE_BYTE, 
                        width, 
                        height, 
                        3, 
                        null), 
                    false, 
                    new Hashtable());
            }
            
            //Draw the texture with the new format
            Graphics graphics = texture.getGraphics();
            graphics.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), 0, image.getHeight(), image.getWidth(), 0, null);
            
            //Build a byte buffer from the temporary image
            //that be used by OpenGL to produce a texture.
            byte[] bytes = ((DataBufferByte)texture.getRaster().getDataBuffer()).getData();

            data = ByteBuffer.allocateDirect(bytes.length);
            data.order(ByteOrder.nativeOrder());
            data.put(bytes, 0, bytes.length);
            data.flip();
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Get the image width.
     * @return Image width.
     */
    public int getWidth()
    {
        return width;
    }
    
    /**
     * Get the image height.
     * @return Image height.
     */
    public int getHeight()
    {
        return height;
    }
    
    /**
     * Do we have an alpha channel.
     * @return If image is RGB return false. If image is RGBA return true.
     */
    public boolean hasAlpha()
    {
        return hasAlpha;
    }
    
    /**
     * Get the image data. This can be RGB or RGBA. Use the
     * {@link #hasAlpha() hasAlpha} method to find out the current
     * mode of the texture.
     * @return Image data.
     */
    public ByteBuffer getData()
    {
        return data;
    }
}
