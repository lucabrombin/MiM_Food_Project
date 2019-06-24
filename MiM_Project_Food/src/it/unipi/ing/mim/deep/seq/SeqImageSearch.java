package it.unipi.ing.mim.deep.seq;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;
import it.unipi.ing.mim.deep.tools.Output;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

// This class is used to select the most similar images to a certain image query

public class SeqImageSearch {

	private List<ImgDescriptor> descriptors;
		
	// loads from storageFile of pivots
	public void open(File storageFile) throws ClassNotFoundException, IOException {
		descriptors = FeaturesStorage.load(storageFile);
	}
	
	// loops pivots to perform a sequential scan search
	//	- computes the distance between each pivot descriptor and the query
	//	- sorts the results
	//	- returns the k best results
	public List<ImgDescriptor> search(ImgDescriptor queryF, int k) {
		for (int i = 0; i < descriptors.size(); i++) 
			descriptors.get(i).distance(queryF);
		
		Collections.sort(descriptors);
		
		return descriptors.subList(0, k);
	}

	public List<ImgDescriptor> searchHeap(ImgDescriptor queryF, int k) {
		PriorityQueue<double> heap = new PriorityQueue<double>(Collections.reverseOrder());

		for (int i = 0; i < descriptors.size(); i++) {
			descriptors.get(i).distance(queryF);
			heap.add(descriptors.get(i).getDist());
		}
		return new List<ImgDescriptor>(heap).subList(0, k);
	}
}
