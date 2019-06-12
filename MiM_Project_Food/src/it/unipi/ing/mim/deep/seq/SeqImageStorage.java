package it.unipi.ing.mim.deep.seq;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SeqImageStorage {

	public static void main(String[] args) throws Exception {
				
		SeqImageStorage indexing = new SeqImageStorage();
				
		List<ImgDescriptor> descriptors = indexing.extractFeatures(Parameters.SRC_FOLDER);
		
		FeaturesStorage.store(descriptors, Parameters.STORAGE_FILE);
		
		System.out.println("FEATURE EXTRACTED");
	}
	
	private List<ImgDescriptor> extractFeatures(File imgFolder){
		List<ImgDescriptor>  descs = new ArrayList<ImgDescriptor>();

		// LISTA CONTENENTE LE CARTELLE DELLE DIVERSE IMG
		File[] dir = imgFolder.listFiles();
		
		System.out.println(dir.length + "-" + dir[0]);
		
		DNNExtractor extractor = new DNNExtractor();
		
		// PER OGNUNA DELLE 101 CLASSI
		// HO MESSO 40 PERCHE ALTRIMENTI 101 CLASSI NON MI ENTRAVANO IN MEMORIA
		// VA MESSO dir.length
		for (int k = 0; k < 40; k++) {
			
			File[] files = dir[k].listFiles();
			
			// OTTENGO IL NOME DELLA CLASSE
			String temp = ""+dir[k];
			String foodClass = (temp.split("img")[1]).substring(1);
			
			System.out.println(k + " - PROCESSING CLASS: " + foodClass);
			
			// PER OGNI FOTO DI UNA DETERMINATA CLASSE
			for(int i = 0; i < files.length; i++) {
				//System.out.println(i + " - extracting " + files[i].getName());
				try {
					long time = -System.currentTimeMillis();
					float[] features = extractor.extract(files[i], Parameters.DEEP_LAYER);
					time += System.currentTimeMillis();
					//System.out.println(time);
					descs.add(new ImgDescriptor(features, files[i].getName(), foodClass));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return descs;	
	}		
}
