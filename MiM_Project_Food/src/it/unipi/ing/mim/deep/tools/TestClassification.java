package it.unipi.ing.mim.deep.tools;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import org.locationtech.jts.util.Debug;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.img.elasticsearch.ElasticImgSearching;

public class TestClassification {

	public static void main(String[] args) throws Exception {
		
		TestClassification myTest = new TestClassification();

		// folder with images used for testing
		File imgFolder = Parameters.SRC_TEST_FOLDER;
	
		File[] dir = imgFolder.listFiles();
		
		DNNExtractor extractor = new DNNExtractor();

		// number of classes to test
		int numberOfFolder = dir.length;	
		int total = 0;

		// actual_class is the real class of the query image, predicted_class is the one predicted by the classifier
		String actual_class = null, predicted_class = null;

		// accuracy of the classifier, computed as the number of correct classified elements (sum of true positives for each class) over the total
		double accuracy = 0.0;
		
		// precision is computed for each class as TP / (TP + FP)
		double[] class_precision = new double[numberOfFolder];
		
		// recall is computed for each class as TP / (TP + FN)
		double[] class_recall = new double[numberOfFolder];
		 
		// aggregated values macroaveraged precision, microaveraged precision, macroaveraged recall, microaveraged recall
		double macro_avg_precision = 0.0,  micro_avg_precision = 0.0, macro_avg_recall = 0.0, micro_avg_recall = 0.0;
		
		int micro_avg_precision_num = 0, micro_avg_precision_den = 0, micro_avg_recall_num = 0, micro_avg_recall_den = 0;
		
		LinkedHashMap<String, Integer> true_positives = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> false_positives = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> false_negatives = new LinkedHashMap<String, Integer>();
		
		for(int k = 0; k < numberOfFolder; k++) {
			true_positives.put(myTest.getActualClass(dir, k), 0);
			false_positives.put(myTest.getActualClass(dir, k), 0);
			false_negatives.put(myTest.getActualClass(dir, k), 0);
		}
		
		// each class is examined
		for (int k = 0; k < numberOfFolder; k++) { 
			// initializes the name of the actual class
			actual_class = myTest.getActualClass(dir, k);
			System.out.println("-- DEBUG -- The actual class is: " + actual_class);				
			
			// contains the names of images in the k-th class
			File[] files = dir[k].listFiles();
			
			// each image of each class is examined
			for(int i = 0; i < files.length; i++) {					
				total++;
				
				File imgQuery = new File("" + files[i] );

				try (ElasticImgSearching imgSearch = new ElasticImgSearching(Parameters.PIVOTS_FILE, Parameters.STORAGE_FILE, Parameters.TOP_K_QUERY)) {
					// extracts the features for the query
					float[] imgFeatures = extractor.extract(imgQuery, Parameters.DEEP_LAYER);
					
					// creates the corresponding descriptor
					ImgDescriptor query = new ImgDescriptor(imgFeatures, imgQuery.getName(), null);
	
					// searches the k most similar images 
					List<ImgDescriptor> res = imgSearch.search(query, Parameters.K);
					
					// sorts the results
					res = imgSearch.reorder(query, res); 
					
					// uses the knn classifier to classify the image
					KnnClassifier knn = new KnnClassifier();
					knn.classify(res);
					predicted_class = knn.getPredictedClass();
					
					
					if(predicted_class.equals(actual_class)) {
						// if predicted_class is equal to actual_class, the image is a true positive
						true_positives.put(actual_class, true_positives.get(actual_class) + 1);	
					} else {
						// if predicted_class is NOT equal to actual_class 
						
						// - the image is a false negative for actual_class (the image belongs to actual_class but it is classified as not belonging to it)
						false_negatives.put(actual_class, false_negatives.get(actual_class) + 1);	
						
						// - the image is a false positive for predicted_class (the image does not belong to predicted_class but is is classified as belonging to it)
						false_positives.put(predicted_class, false_positives.get(predicted_class) + 1);	
					}
				}
			}
		}
		
		for(int k = 0; k < numberOfFolder; k++) {
			String tmp_class = myTest.getActualClass(dir, k);
			
			accuracy += true_positives.get(tmp_class);
			
			if(true_positives.get(tmp_class) == 0 && false_positives.get(tmp_class) == 0) {
				System.out.println("-- DEBUG -- Error in computing precision of the class");
				class_precision[k] = 0;
			} else {
				class_precision[k] = true_positives.get(tmp_class) / (true_positives.get(tmp_class) + false_positives.get(tmp_class));
			}
			macro_avg_precision += class_precision[k];
			micro_avg_precision_num += true_positives.get(tmp_class);
			micro_avg_precision_den += true_positives.get(tmp_class) + false_positives.get(tmp_class);
			
			if(true_positives.get(tmp_class) == 0 && false_negatives.get(tmp_class) == 0) {
				System.out.println("-- DEBUG -- Error in computing recall of the class");
				class_recall[k] = 0;
			} else {
				class_recall[k] = true_positives.get(tmp_class) / (true_positives.get(tmp_class) + false_negatives.get(tmp_class));
			}		
			
			macro_avg_recall += class_recall[k];
			micro_avg_recall_num += true_positives.get(tmp_class);
			micro_avg_recall_den += true_positives.get(tmp_class) + false_negatives.get(tmp_class);
		}
		
		accuracy /= total;
		
		macro_avg_precision /= numberOfFolder;
		macro_avg_recall /= numberOfFolder;
		
		micro_avg_precision = micro_avg_precision_num / micro_avg_precision_den;
		micro_avg_recall = micro_avg_recall_num / micro_avg_recall_den;
		
		System.out.println("-- DEBUG -- Accuracy = " +  accuracy);
		System.out.println("-- DEBUG -- Macroaveraged precision = " +  macro_avg_precision);
		System.out.println("-- DEBUG -- Microaveraged precision = " +  micro_avg_precision);
		System.out.println("-- DEBUG -- Macroaveraged recall = " +  macro_avg_recall);
		System.out.println("-- DEBUG -- Microaveraged recall = " +  micro_avg_recall);
	}
	
	public String getActualClass(File[] dir, int k) {
		// name of the actual_class
		
		String actual_class = "" + dir[k];
		actual_class = (actual_class.split("test")[1]).substring(1);		
		return actual_class;
	}		
}
