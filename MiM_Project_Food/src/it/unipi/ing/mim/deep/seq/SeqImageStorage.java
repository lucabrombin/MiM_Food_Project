package it.unipi.ing.mim.deep.seq;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// This class extracts the features from all the images in a dataset

public class SeqImageStorage {

	public static void main(String[] args) throws Exception {				
		SeqImageStorage indexing = new SeqImageStorage();			
	
		// extracts the features from the images and creates the descriptors
		List<ImgDescriptor> descriptors = indexing.extractFeatures(Parameters.SRC_FOLDER);		
		
		/*CANCELLA
		for(int i = 0; i < 5; i++) {
			System.out.print("-- DEBUG -- " + descriptors.get(i).getId() + "   ");
			for(int j = 0; j < descriptors.get(i).getFeatures().length; j++) {
				System.out.print(descriptors.get(i).getFeatures()[j] + "  ");
			}			
			System.out.println();
		}
		*/

		// stores the list of descriptors in the specified file
		FeaturesStorage.store(descriptors, Parameters.STORAGE_FILE);
		
		System.out.println("-- DEBUG -- Features extracted");
	}
	
	// extracts the features from images stored in the specified folder using a DNNExtractor
	// creates and returns the list of corresponding descriptor
	private List<ImgDescriptor> extractFeatures(File imgFolder){
		List<ImgDescriptor>  descs = new ArrayList<ImgDescriptor>();

		// dir is a list containing all folders for the different classes of images
		File[] dir = imgFolder.listFiles();
		
		System.out.println("-- DEBUG -- Number of classes in the dataset = " + dir.length);
		
		DNNExtractor extractor = new DNNExtractor();
		
		// Examines each class in the folder
		for (int k = 0; k < dir.length; k++) {
			// files is a list containing all the images of the examined class
			File[] files = dir[k].listFiles();
			
			// foodClass is the name of the class
			String tmp = ""+dir[k];
			String foodClass = (tmp.split("img")[1]).substring(1);
			
			System.out.println("-- DEBUG -- " + k + " - Processing class " + foodClass);

			// For each image of the class, extracts the features 
			for(int i = 0; i < files.length; i++) {
				System.out.println("-- DEBUG -- " + i + " - Extracting features from " + files[i].getName());
				try {
					float[] features = extractor.extract(files[i], Parameters.DEEP_LAYER);
					descs.add(new ImgDescriptor(features, files[i].getName(), foodClass));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return descs;	
	}		
}
