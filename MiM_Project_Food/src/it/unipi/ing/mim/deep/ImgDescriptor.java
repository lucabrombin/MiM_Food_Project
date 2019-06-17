package it.unipi.ing.mim.deep;

import java.io.Serializable;

public class ImgDescriptor implements Serializable, Comparable<ImgDescriptor> {

	private static final long serialVersionUID = 1L;
	
	// unique id of the image (usually file name)
	private String id; 
	
	// used for sorting purposes
	private double dist; 
	
	// image features
	private float[] normalizedVector; 
	
	private String foodClass; 
	
	public ImgDescriptor(float[] features, String id, String foodClass) {
		if (features != null) {
			float norm2 = evaluateNorm2(features);
			this.normalizedVector = getNormalizedVector(features, norm2);
		}
		this.id = id;
		this.foodClass = foodClass;
	}
	
	public float[] getFeatures() {
		return normalizedVector;
	}
	
    public String getId() {
		return id;
	}
    
    public void setId(String id) {
		this.id = id;
	}
    
    public String getFoodClass() {
		return foodClass;
	}

    public void setFoodClass(String foodClass) {
		this.foodClass = foodClass;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	// compares with other friends using distances
	@Override
	public int compareTo(ImgDescriptor arg0) {
		return Double.valueOf(dist).compareTo(arg0.dist);
	}
	
	// evaluates Euclidean distance
	public double distance(ImgDescriptor desc) {
		
		//System.out.println("Distance");
		float[] queryVector = desc.getFeatures();
		
		dist = 0;
		for (int i = 0; i < queryVector.length; i++) {
			dist += (normalizedVector[i] - queryVector[i]) * (normalizedVector[i] - queryVector[i]);
		}
		dist = Math.sqrt(dist);
		
		return dist;
	}
	
	// normalizes values of the vector
	private float[] getNormalizedVector(float[] vector, float norm) {
		if (norm != 0) {
			for (int i = 0; i < vector.length; i++) {
				vector[i] = vector[i]/norm;
			}
		}
		return vector;
	}
	
	// computes norm2
	private float evaluateNorm2(float[] vector) {
		float norm2 = 0;
		for (int i = 0; i < vector.length; i++) {
			norm2 += (vector[i]) * (vector[i]);
		}
		norm2 = (float) Math.sqrt(norm2);
		
		return norm2;
	}
	
	@Override
    public String toString() {
        return "ImgDescriptor [id=" + id + ", dist=" + dist + ", normalizedVector" + normalizedVector.toString() + ", foodClass=" + foodClass + "]";
    }
    
}
