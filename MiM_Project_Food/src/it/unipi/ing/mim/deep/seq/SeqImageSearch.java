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

public class SeqImageSearch {

	private List<ImgDescriptor> descriptors;
	
	private static String foodClass;
		
	/*public static void main(String[] args) throws Exception {
		SeqImageSearch searcher = new SeqImageSearch();
		
		searcher.open(Parameters.STORAGE_FILE);
		
		// image query file
		File img = new File(Parameters.SRC_FOLDER, "000000005992.jpg");
		foodClass = "bruschetta";
		
		DNNExtractor extractor = new DNNExtractor();
		
		float[] features = extractor.extract(img, Parameters.DEEP_LAYER);
		ImgDescriptor query = new ImgDescriptor(features, img.getName(), foodClass);
				
		List<ImgDescriptor> res = searcher.search(query, Parameters.K);
			
		Output.toHTML(res, Parameters.BASE_URI, Parameters.RESULTS_HTML);
	}*/
		
	// loads the extracted features from storageFile and stores them in the descriptors
	public void open(File storageFile) throws ClassNotFoundException, IOException {
		descriptors = FeaturesStorage.load(storageFile);
	}
	
	// loops descriptors of features to perform a sequential scan search
	//	- computes the distance between each descriptor and the query
	//	- sorts the results
	//	- returns the k best results
	public List<ImgDescriptor> search(ImgDescriptor queryF, int k) {
		for (int i = 0; i < descriptors.size(); i++) {
			descriptors.get(i).distance(queryF);
		}
		Collections.sort(descriptors);
		
		return descriptors.subList(0, k);
	}
}
