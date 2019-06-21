package it.unipi.ing.mim.deep;

import java.io.Serializable;

// This class implements the image descriptor, composed of:
// - the id of the image 
// - the list of extracted features
// - the name of the class to which the image belongs

public class ImgDescriptor implements Serializable, Comparable<ImgDescriptor> {

	private static final long serialVersionUID = 1L;
	
	// unique id of the image (usually its file name)
	private String id; 
	
	// used for sorting purposes
	private double dist; 
	
	// features extracted from the image
	private float[] normalizedVector; 
	
	// class of the image
	private String foodClass; 
	
	// constructor
	public ImgDescriptor(float[] features, String id, String foodClass) {
		if (features != null) {
			float norm2 = evaluateNorm2(features);
			this.normalizedVector = getNormalizedVector(features, norm2);
		}
		this.id = id;
		this.foodClass = foodClass;
	}
	
	// returns the id of the image
	public String getId() {
		return id;
	}

	// sets the id of the image
	public void setId(String id) {
		this.id = id;
	}

	// returns the vector of features of the image
	public float[] getFeatures() {
		return normalizedVector;
	}

	// returns the class of the image
	public String getFoodClass() {
		return foodClass;
	}
    
    // sets the class of the image
    public void setFoodClass(String foodClass) {
		this.foodClass = foodClass;
	}

	// returns the computed distance from another image
	public double getDist() {
		return dist;
	}

	// sets the distance from another image
	public void setDist(double dist) {
		this.dist = dist;
	}

	// compares with other friends using distances
	@Override
	public int compareTo(ImgDescriptor arg0) {
		return Double.valueOf(dist).compareTo(arg0.dist);
	}
	
	// evaluates the Euclidean distance
	public double distance(ImgDescriptor desc) {
		float[] queryVector = desc.getFeatures();
		
		dist = 0;
		for (int i = 0; i < queryVector.length; i++) {
			dist += (normalizedVector[i] - queryVector[i]) * (normalizedVector[i] - queryVector[i]);
		}
		dist = Math.sqrt(dist);
		
		return dist;
	}
	
	// normalizes values of the vector of features
	private float[] getNormalizedVector(float[] vector, float norm) {
		if (norm != 0) {
			for (int i = 0; i < vector.length; i++) {
				vector[i] = vector[i]/norm;
			}
		}
		return vector;
	}
	
	// computes the norm2
	private float evaluateNorm2(float[] vector) {
		float norm2 = 0;
		for (int i = 0; i < vector.length; i++) {
			norm2 += (vector[i]) * (vector[i]);
		}
		norm2 = (float) Math.sqrt(norm2);
		
		return norm2;
	}
}
