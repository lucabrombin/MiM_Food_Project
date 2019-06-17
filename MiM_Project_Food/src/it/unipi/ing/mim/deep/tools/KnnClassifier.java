package it.unipi.ing.mim.deep.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import it.unipi.ing.mim.deep.ImgDescriptor;

// This class implements the KNN classifier used to evaluate the class of a certain image

public class KnnClassifier {

	private String predictedClass;
	
	LinkedHashMap<String, Double> resultMap;
	
	// constructor
	public KnnClassifier() {
		resultMap = new LinkedHashMap<String, Double>();
	}
	
	public boolean isClassificationOk(String queryClass) {
		return this.predictedClass.equals(queryClass);
	}
	
	public String getPredictedClass() {
		return predictedClass;
	}
	
	//// returs a list of <class, confidence>
	// ids is the result of the search of the most similar images and is sorted according to decreasing order of similarity
	public Map<String, Double> classify(List<ImgDescriptor> ids) {
		String foodClass;
		
		// predicted class is evaluated by voting
		for(int i = 0; i < ids.size(); i++) {
			foodClass = ids.get(i).getFoodClass();
			
			// if the class is not yet in resultMap, it is inserted and the number of images belonging to it are set to 0
			if(!resultMap.containsKey(foodClass))
				resultMap.put(foodClass, 0.0);
			
			// gets the current value of the number of images of the class stored in the map and increases it by 1
			resultMap.replace(foodClass, resultMap.get(foodClass) + 1.0);
		}
		
		// list of food classes
		Set<String> listOfClasses = resultMap.keySet();
		for(Iterator<String> iterator = listOfClasses.iterator(); iterator.hasNext();) {
	        foodClass = iterator.next();
	        resultMap.replace(foodClass, resultMap.get(foodClass)/ids.size()*100);
	    }
		
		// the list of classes is ordered according to the confidence percentage
		resultMap = resultMap.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue((v1,v2)->v1.compareTo(v2))))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1,v2)->v1, LinkedHashMap::new));
		
		predictedClass = resultMap.keySet().iterator().next();
		
		//TODO
		/******EVALUATE PREDICTED CLASS BY WEIGHTED VOTING******/
		/*******************************************************/
						
		return resultMap;		
	}
}