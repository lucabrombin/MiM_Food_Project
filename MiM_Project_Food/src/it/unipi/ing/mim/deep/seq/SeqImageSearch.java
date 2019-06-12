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
		
	public static void main(String[] args) throws Exception {

		SeqImageSearch searcher = new SeqImageSearch();
		
		searcher.open(Parameters.STORAGE_FILE);
		
		//Image Query File
		File img = new File(Parameters.SRC_FOLDER, "000000005992.jpg");
		foodClass = "bruschetta";
		
		DNNExtractor extractor = new DNNExtractor();
		
		float[] features = extractor.extract(img, Parameters.DEEP_LAYER);
		ImgDescriptor query = new ImgDescriptor(features, img.getName(), foodClass);
				
		long time = -System.currentTimeMillis();
		List<ImgDescriptor> res = searcher.search(query, Parameters.K);
		time += System.currentTimeMillis();
		//System.out.println("Sequential search time: " + time + " ms");
		
		Output.toHTML(res, Parameters.BASE_URI, Parameters.RESULTS_HTML);

	}
		
	public void open(File storageFile) throws ClassNotFoundException, IOException {
		descriptors = FeaturesStorage.load(storageFile );
	}
	
	public List<ImgDescriptor> search(ImgDescriptor queryF, int k) {
		long time = -System.currentTimeMillis();
		for (int i=0;i<descriptors.size();i++){
			descriptors.get(i).distance(queryF);
		}
		time += System.currentTimeMillis();
		//System.out.println(time + " ms");

		Collections.sort(descriptors);
		
		return descriptors.subList(0, k);
	}

}
