package lwjglpick; 

import java.io.*;
import java.nio.*;
import java.util.Vector;

/**
 * The ObjImporter just reads in an obj file (with corresponding mtl file) 
 * and stores the data into buffers. The usage should be like this:<br>
 * <code><br>
 * ObjImporter obj = new ObjImporter("SomeFile.obj");<br>
 * FloatBuffer vertexBuffer = obj.getVertexBuffer();<br>
 * FloatBuffer normalBuffer = obj.getNormalBuffer();<br>
 * FloatBuffer texCoordBuffer = obj.getTexCoordBuffer();<br>
 * IntBuffer dataIndexBuffer = obj.getDataIndexBuffer();<br>
 * Vector&lt;Material&rt;  materialVector = obj.getMaterialVector();<br>
 * IntBuffer materialIndexBuffer = obj.getMaterialIndexBuffer();<br>
 * </code><br>
 * Then you have to organize the data on your own!
 */
public class ObjImporter
{
	/**
	 * The Material class is just a 1:1 mapping of the mtl file into a java
	 * data structure.
	 */
	public class Material
	{
		public String newmtl = "";
		public float[] Ka = null;
		public float[] Kd = null;
		public float[] Ks = null;
		public float[] d = null;
		public float[] Ns = null;
		public float[] illum = null;
		public String map_Ka = "";
		public String map_Kd = "";
		public String map_Ks = "";
	}
	
	private int triangleCount = 0;
	private ByteArrayOutputStream vertexArrayStream = new ByteArrayOutputStream();
	private DataOutputStream vertexDataStream = new DataOutputStream(vertexArrayStream);
	private ByteArrayOutputStream normalArrayStream = new ByteArrayOutputStream();
	private DataOutputStream normalDataStream = new DataOutputStream(normalArrayStream);
	private ByteArrayOutputStream texCoordArrayStream = new ByteArrayOutputStream();
	private DataOutputStream texCoordDataStream = new DataOutputStream(texCoordArrayStream);
	private ByteArrayOutputStream dataIndexArrayStream = new ByteArrayOutputStream();
	private DataOutputStream dataIndexDataStream = new DataOutputStream(dataIndexArrayStream);
	private float maxX = Float.NEGATIVE_INFINITY;
	private float maxY = Float.NEGATIVE_INFINITY;
	private float maxZ = Float.NEGATIVE_INFINITY;
	private float minX = Float.POSITIVE_INFINITY;
	private float minY = Float.POSITIVE_INFINITY;
	private float minZ = Float.POSITIVE_INFINITY;
	
	private Vector<Material> materialVector = new Vector<Material>();
	private ByteArrayOutputStream materialIndexArrayStream = new ByteArrayOutputStream();
	private DataOutputStream materialIndexDataStream = new DataOutputStream(materialIndexArrayStream);
	
	/**
	 * Creates the ObjImporter for the given filename and fills the buffers.
	 * @param fileName The obj file that should be loaded.
	 */
	public ObjImporter(String fileName)
	{
		File file = new File(fileName);

		try
		{
			BufferedReader input =  new BufferedReader(new FileReader(file));
			try
			{
				String line = null;
				int currentMaterial = -1;
				
				while((line = input.readLine()) != null)
				{
					if(line.startsWith("#"))
						continue;
					String[] stringArray = line.split(" ");
					if(stringArray[0].equals("v"))
					{
						//Assume 3 values
						int i = 0;
						for(String element : stringArray)
						{
							try
							{
								//Discard the w component, if there is one
								if(i > 2)
									break;
								float number = new Float(element).floatValue();
								switch(i)
								{
									case 0: //X
										if(number < minX)
											minX = number;
										if(number > maxX)
											maxX = number;
										break;
									case 1: //Y
										if(number < minY)
											minY = number;
										if(number > maxY)
											maxY = number;
										break;
									case 2: //Z
										if(number < minZ)
											minZ = number;
										if(number > maxZ)
											maxZ = number;
										break;
								}
								i++;
								vertexDataStream.writeFloat(number);
							}
							catch(NumberFormatException ignore)
							{
							}
						}
					}
					else if(stringArray[0].equals("vt"))
					{
						//Assume 3 values
						for(String element : stringArray)
						{
							try
							{
								texCoordDataStream.writeFloat(new Float(element).floatValue());
							}
							catch(NumberFormatException ignore)
							{
							}
						}
					}
					else if(stringArray[0].equals("vn"))
					{
						//Assume 3 values
						for(String element : stringArray)
						{
							try
							{
								normalDataStream.writeFloat(new Float(element).floatValue());
							}
							catch(NumberFormatException ignore)
							{
							}
						}
					}
					else if(stringArray[0].equals("f"))
					{
						/* Here we can have the following formats:
						 * f v0 v1 v2 ...
						 * f v0/vt0 v1/vt1 v2/vt2 ...
						 * f v0//vn0 v1//vn1 v2//vn2 ...
						 * f v0/vt0/vn0 v1/vt1/vn1 v2/vt2/vn2 ...
						 */
						int property = 0;
						for(String element : stringArray)
						{
							if(element.equals(""))
								continue;
							//Restrict to triangles only!
							if(property >= 3)
								break;
							boolean propertyFound = false;
							String[] indexArray = element.split("/");
							for(String index : indexArray)
							{
								try
								{
									dataIndexDataStream.writeInt(new Integer(index).intValue() - 1);
									if(!propertyFound)
									{
										propertyFound = true;
										property++;
									}
								}
								catch(NumberFormatException ignore)
								{
								}
							}
						}
						if(hasMaterials())
							materialIndexDataStream.writeInt(new Integer(currentMaterial).intValue());
						triangleCount++;
					}
					else if(stringArray[0].equals("usemtl"))
					{
						currentMaterial = findMaterialByName(stringArray[1]);
					}
					else if(stringArray[0].equals("mtllib"))
					{
						importMtl(new File(file.getParentFile().getAbsolutePath() + File.separator + stringArray[1]).getCanonicalPath());
					}
				}
			}
			finally
			{
				input.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * How many triangles were loaded?
	 * @return The number of triangles in the obj file.
	 */
	public int getTriangleCount()
	{
		return triangleCount;
	}
	
	/**
	 * Returns the number of elements that belong together in the index buffer.
	 * @return The obj file can have the following formats: <br>
	 * f v0 v1 v2 -> returns 1 <br>
	 * f v0/vt0 v1/vt1 v2/vt2 -> returns 2 <br>
	 * f v0//vn0 v1//vn1 v2//vn2 -> returns 2 <br>
	 * f v0/vt0/vn0 v1/vt1/vn1 v2/vt2/vn2 -> returns 3
	*/
	public int getDataSize()
	{
		//By default only vertex indices
		int size = 1;
		if(hasTexCoords())
			size++;
		if(hasNormals())
			size++;
		return size;
	}
	
	/**
	 * Returns the relative offset in the index data of the texture 
	 * coordinate index.
	 * @return The obj file can have the following formats: <br>
	 * f v0 v1 v2 -> returns -1 <br>
	 * f v0/vt0 v1/vt1 v2/vt2 -> returns 1 <br>
	 * f v0//vn0 v1//vn1 v2//vn2 -> returns -1 <br>
	 * f v0/vt0/vn0 v1/vt1/vn1 v2/vt2/vn2 -> returns 1
	 */
	public int getTexCoordPosition()
	{
		if(hasTexCoords())
			return 1;
		return -1;
	}
	
	/**
	 * Returns the relative offset in the index data of the normal index.
	 * @return The obj file can have the following formats: <br>
	 * f v0 v1 v2 -> returns -1 <br>
	 * f v0/vt0 v1/vt1 v2/vt2 -> returns -1 <br>
	 * f v0//vn0 v1//vn1 v2//vn2 -> returns 1 <br>
	 * f v0/vt0/vn0 v1/vt1/vn1 v2/vt2/vn2 -> returns 2
	 */
	public int getNormalPosition()
	{
		int pos = 0;
		if(hasTexCoords())
			pos++;
		if(hasNormals())
			return pos + 1;
		return -1;
	}
	
	/**
	 * Does the obj file provide texture coordinates?
	 * @return True, if there are texture coordinates. False otherwise.
	 */
	public boolean hasTexCoords()
	{
		return texCoordArrayStream.size() != 0;
	}
	
	/**
	 * Does the obj file provide normals?
	 * @return True, if there are normals. False otherwise.
	 */
	public boolean hasNormals()
	{
		return normalArrayStream.size() != 0;
	}
	
	/**
	 * Does the obj file provide materials?
	 * @return True, if there are materials. False otherwise.
	 */
	public boolean hasMaterials()
	{
		return materialIndexArrayStream.size() != 0;
	}
	
	/**
	 * Access the shared vertex positions.
	 * @return Shared vertex positions.
	 */
	public FloatBuffer getVertexBuffer()
	{
		return ByteBuffer.wrap(vertexArrayStream.toByteArray()).asFloatBuffer();
	}
	
	/**
	 * Access the shared normals.
	 * @return Shared normals.
	 */
	public FloatBuffer getNormalBuffer()
	{
		return ByteBuffer.wrap(normalArrayStream.toByteArray()).asFloatBuffer();
	}
	
	/**
	 * Access the shared texture coordinates.
	 * @return Shared texture coordinates.
	 */
	public FloatBuffer getTexCoordBuffer()
	{
		return ByteBuffer.wrap(texCoordArrayStream.toByteArray()).asFloatBuffer();
	}
	
	/**
	 * Access the indices.
	 * @return The obj file can have the following formats: <br>
	 * f v0 v1 v2 -> return (VVV)* <br>
	 * f v0/vt0 v1/vt1 v2/vt2 -> return (VTVTVT)* <br>
	 * f v0//vn0 v1//vn1 v2//vn2 -> return (VNVNVN)* <br>
	 * f v0/vt0/vn0 v1/vt1/vn1 v2/vt2/vn2 -> return (VTNVTNVTN)* <br>
	 * where V is an index to a shared vertex, T is an index
	 * to a shared texture coordinate and N is an index to
	 * a shared normal.
	 */
	public IntBuffer getDataIndexBuffer()
	{
		return ByteBuffer.wrap(dataIndexArrayStream.toByteArray()).asIntBuffer();
	}
	
	/**
	 * Access the materials from the mtl file.
	 * @return Material vector from the mtl file.
	 */
	public Vector<Material> getMaterialVector()
	{
		return materialVector;
	}
	
	/**
	 * Access the material indices from the obj file.
	 * @return Used material indices for all triangles.
	 */
	public IntBuffer getMaterialIndexBuffer()
	{
		return ByteBuffer.wrap(materialIndexArrayStream.toByteArray()).asIntBuffer();
	}
	
	/**
	 * Minimum x position of the object.
	 * @return The minimum position along the x axis..
	 */
	public float getMinX()
	{
		return minX;
	}
	
	/**
	 * Minimum y position of the object.
	 * @return The minimum position along the y axis..
	 */
	public float getMinY()
	{
		return minY;
	}
	
	/**
	 * Minimum z position of the object.
	 * @return The minimum position along the z axis..
	 */
	public float getMinZ()
	{
		return minZ;
	}
	
	/**
	 * Maximum x position of the object.
	 * @return The maximum position along the x axis..
	 */
	public float getMaxX()
	{
		return maxX;
	}
	
	/**
	 * Maximum y position of the object.
	 * @return The maximum position along the y axis..
	 */
	public float getMaxY()
	{
		return maxY;
	}
	
	/**
	 * Maximum z position of the object.
	 * @return The maximum position along the z axis..
	 */
	public float getMaxZ()
	{
		return maxZ;
	}
	
	/**
	 * Import the mtl file and fill the buffers.
	 * @param fileName The mtl file that should be loaded.
	 */
	private void importMtl(String fileName)
	{
		File file = new File(fileName);

		try
		{
			BufferedReader input =  new BufferedReader(new FileReader(file));
			try
			{
				String line = null; 
				Material currentMaterial = null;
				
				while((line = input.readLine()) != null)
				{
					if(line.startsWith("#"))
						continue;
					String[] stringArray = line.split(" ");
					if(stringArray[0].equals("newmtl"))
					{
						currentMaterial = new Material();
						currentMaterial.newmtl = stringArray[1];
						materialVector.add(currentMaterial);
					}
					else if(currentMaterial != null)
					{
						if(stringArray[0].equals("Ka"))
						{
							currentMaterial.Ka = getFloatArray(stringArray, 3);
						}
						else if(stringArray[0].equals("Kd"))
						{
							currentMaterial.Kd = getFloatArray(stringArray, 3);
						}
						else if(stringArray[0].equals("Ks"))
						{
							currentMaterial.Ks = getFloatArray(stringArray, 3);
						}
						else if(stringArray[0].equals("d"))
						{
							currentMaterial.d = getFloatArray(stringArray, 1);
						}
						else if(stringArray[0].equals("Ns"))
						{
							currentMaterial.Ns = getFloatArray(stringArray, 1);
						}
						else if(stringArray[0].equals("illum"))
						{
							currentMaterial.illum = getFloatArray(stringArray, 1);
						}
						else if(stringArray[0].equals("map_Ka"))
						{
							currentMaterial.map_Ka = new File(file.getParentFile().getAbsolutePath() + File.separator + stringArray[1]).getCanonicalPath();
						}
						else if(stringArray[0].equals("map_Kd"))
						{
							currentMaterial.map_Kd = new File(file.getParentFile().getAbsolutePath() + File.separator + stringArray[1]).getCanonicalPath();
						}
						else if(stringArray[0].equals("map_Ks"))
						{
							currentMaterial.map_Ks = new File(file.getParentFile().getAbsolutePath() + File.separator + stringArray[1]).getCanonicalPath();
						}
					}
				}
			}
			finally
			{
				input.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a float array from an array of strings. You have to specify
	 * the assumed size and if that does not equal the number of floats,
	 * which occur, the result will be null.
	 * @param stringArray The array of strings to be regarded.
	 * @param assumedSize The number of floats that should occur.
	 * @return Float array from an array of strings. Some examples: <br>
	 * getFloatArray(["Ka", "0.6", "0.6", "0.6"], 3) -> [0.6, 0.6, 0.6] <br>
	 * getFloatArray(["Ka", "0.6", "0.6", "0.6"], 4) -> null
	 */
	private float[] getFloatArray(String[] stringArray, int assumedSize)
	{
		Vector<Float> targetVector = new Vector<Float>();
		for(String element : stringArray)
		{
			try
			{
				targetVector.add(new Float(element));
			}
			catch(Exception ignore)
			{
			}
		}
		if(assumedSize != targetVector.size())
			return null;
		float[] fTargetArray = new float[targetVector.size()];
		for(int i = 0; i < fTargetArray.length; i++)
			fTargetArray[i] = targetVector.get(i);
		return fTargetArray;
	}

	/**
	 * Find a material from the mtl file by its name.
	 * @param name The name of the material.
	 * @return The index in the shared material vector.
	 */
	private int findMaterialByName(String name)
	{
		for(int i = 0; i < materialVector.size(); i++)
			if(materialVector.get(i).newmtl.equals(name))
				return i;
		return -1;
	}
}
