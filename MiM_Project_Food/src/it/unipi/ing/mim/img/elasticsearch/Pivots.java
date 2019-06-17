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

// This class extracts pivots from features, pivots are used to build the index on Elasticsearch

public class Pivots {
	
	private SeqImageSearch seqPivots = new SeqImageSearch();
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		//loads the extracted features in the ids list
		List<ImgDescriptor> ids = FeaturesStorage.load(Parameters.STORAGE_FILE);
		
		for(int i = 0; i < 5; i++) {
			System.out.print("-- DEBUG -- " + ids.get(i).getId() + "  ");
			for(int j = 0; j < ids.get(i).getFeatures().length; j++) {
				System.out.print(ids.get(i).getFeatures()[j] + "  ");
			}			
			System.out.println();
		}
		
		List<ImgDescriptor> pivs = Pivots.makeRandomPivots(ids, Parameters.NUM_PIVOTS);
		FeaturesStorage.store(pivs, Parameters.PIVOTS_FILE_GOOGLENET);
		
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
		
		// random permutation of the specified list 
		Collections.shuffle(ids);
		
		// the id of each pivot is its position in the list
		for(int i = 0; i <= nPivs; i++) {
			//System.out.println("-- DEBUG -- Descriptor: " + ids.get(i).getId());		
			tmpPivot = ids.get(i);
			tmpPivot.setId(Integer.toString(i));
			pivots.add(tmpPivot);
			//System.out.println("-- DEBUG -- Descriptor: " + ids.get(i).getId());
		}
		return pivots;
	}
	
	// evaluates the text format of a feature imgF using pivots
	// performs a sequential search to get the topK most similar pivots to imgF and uses them to create the string
	public String features2Text(ImgDescriptor imgF, int topK) {	
		StringBuilder sb = new StringBuilder();
			
		// topKPivots contains the topK most similar pivots in decreasing order
		List<ImgDescriptor> topKPivots = seqPivots.search(imgF, topK);
	
		// composes the text string using pivot ids	
		// the id of the j-th pivot is repeated (topK - j) times in the string 
		for(int j = 0; j < topK; j++) 
			for(int i = 0; i < (topK - j); i++)
				sb.append(topKPivots.get(j).getId() + " ");
	
		System.out.println("-- DEBUG -- Pivot string = " + sb.toString());
		
		return sb.toString();
	}
}