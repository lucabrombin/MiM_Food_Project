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

public class Pivots {
	
	private SeqImageSearch seqPivots = new SeqImageSearch();
	
	//TODO
	public Pivots(File pivotsFile) throws ClassNotFoundException, IOException {
		//Load the pivots file
		seqPivots.open(pivotsFile);
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		List<ImgDescriptor> ids = FeaturesStorage.load(Parameters.STORAGE_FILE);
		List<ImgDescriptor> pivs = Pivots.makeRandomPivots(ids, Parameters.NUM_PIVOTS);
		FeaturesStorage.store(pivs, Parameters.PIVOTS_FILE_GOOGLENET);
		
		System.out.println("PIVOT SELECTED");
	}
	
	//TODO
	public static List<ImgDescriptor> makeRandomPivots(List<ImgDescriptor> ids, int nPivs) {
		
		ArrayList<ImgDescriptor> pivots = new ArrayList<ImgDescriptor>();
		ImgDescriptor tmpPivot;
		Collections.shuffle(ids);
		
		//LOOP
		//Create nPivs random pivots and add them in the pivots List
		for(int i = 0; i <= nPivs; i++) {
			tmpPivot = ids.get(i);
			tmpPivot.setId(Integer.toString(i));
			
			pivots.add(tmpPivot);
		}

		return pivots;
	}
	
	//TODO
	public String features2Text(ImgDescriptor imgF, int topK) {
		
		StringBuilder sb = new StringBuilder();
		//perform a sequential search to get the topK most similar pivots
		
		List<ImgDescriptor> topKPivots = seqPivots.search(imgF, topK);
		
		//LOOP
			//compose the text string using pivot ids
		
		for(int j = 0; j < topKPivots.size(); j++) 
			for(int i = 0; i<topK-j; i++)
				sb.append(topKPivots.get(j).getId() + " ");
	
		//System.out.println("features2Text: " + sb.toString());
		
		return sb.toString();
	}
	
}