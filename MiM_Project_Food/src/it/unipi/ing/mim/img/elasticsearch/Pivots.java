package it.unipi.ing.mim.img.elasticsearch;

import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.deep.seq.SeqImageSearch;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// This class selects random pivots from features
// Pivots are used to build the index on Elasticsearch

public class Pivots {
	
	private SeqImageSearch seqPivots = new SeqImageSearch();
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		// loads the image descriptors in the ids list
		List<ImgDescriptor> ids = FeaturesStorage.load(Parameters.STORAGE_FILE);
		
		for(int i = 0; i < 5; i++) {
			System.out.println(ids.get(i).getFeatures().length);		
			System.out.println();
		}
		
		
		// selects NUM_PIVOTS random pivots
		List<ImgDescriptor> pivs = Pivots.makeRandomPivots(ids, Parameters.NUM_PIVOTS);
		
		// stores the extracted pivots in the specified file
		FeaturesStorage.store(pivs, Parameters.PIVOTS_FILE);
		
		System.out.println("-- DEBUG -- Pivots selected");
	}
	
	// constructor: loads the pivots file
	public Pivots(File pivotsFile) throws ClassNotFoundException, IOException {
		seqPivots.open(pivotsFile);
	}
	
	// selects nPivs random pivots from the list ids 
	public static List<ImgDescriptor> makeRandomPivots(List<ImgDescriptor> ids, int nPivs) {
		ArrayList<ImgDescriptor> pivots = new ArrayList<ImgDescriptor>();
		ImgDescriptor tmpPivot;
		
		// random permutation of the list of descriptors 
		Collections.shuffle(ids, new Random(42));
		
		// the id of each pivot is its position in the list
		for(int i = 0; i <= nPivs; i++) {
			tmpPivot = ids.get(i);
			System.out.println(ids.get(i).getId());
			tmpPivot.setId(Integer.toString(i));
			pivots.add(tmpPivot);
		}
		return pivots;
	}
	
	// evaluates the text representation of the image imgF using pivots
	// performs a sequential search to get the topK most similar pivots to imgF and uses them to create the string
	public String features2Text(ImgDescriptor imgF, int topK) {	
		StringBuilder sb = new StringBuilder();
			
		// topKPivots contains the topK most similar pivots in decreasing order
		List<ImgDescriptor> topKPivots = seqPivots.search(imgF, topK);
	
		// composes the string using pivot ids	
		// the id of the j-th pivot is repeated (topK - j) times in the string 
		for(int j = 0; j < topK; j++) 
			for(int i = 0; i < (topK - j); i++)
				sb.append(topKPivots.get(j).getId() + " ");
		
		//System.out.println("-- DEBUG -- Pivot string for the image " + sb.toString());
		
		return sb.toString();
	}
}