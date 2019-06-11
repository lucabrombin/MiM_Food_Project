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

public class KnnClassifier {
	
	private float classificationConfidence;
	private String predictedClass;
	LinkedHashMap<String,Integer> resultMap = new LinkedHashMap<String,Integer>();
	
	public boolean isClassificationOk(String queryClass) {
		return this.predictedClass.equals(queryClass);
	}

	//CLASSIFICATION TASK 
	//IDS è IL RISULTATO DELLA RICERCA DELLE IMMAGINI SIMILI ALLA QUERY 
	//K IL IL NUMERO DI IMMAGINI SIMILI (K-NN)
	public String classify(List<ImgDescriptor> ids, int k){
		String label;
		int value;
		int numberOfClass = 0;
		for(int i = 0; i < k; i++){
			ImgDescriptor descriptor = ids.get(i);
			label = descriptor.getFoodClass();
			
			//SE LA CLASSE PRESA IN COSIDERAZIONE NON è STATA GIA INSERITA NELLA COLLEZZIONE 
			//ALLORA LA INSERISCO E INIZIALIZZO IL NUMERO DI IMMAGINI CHE APPARTENGONO A 
			//QUELLA CLASSE A 0
			if(!resultMap.containsKey(label))
				resultMap.put(descriptor.getFoodClass(),0);
			
			//AUMENTO IL CONTEGGIO DELLE IMMAGINI RIFERITO AD UNA DETERMINATA CLASSSE
			value = resultMap.get(label);
			resultMap.replace(label,value+1);
		}
		
		//PRENDO LA CLASSE CON L'OCCORRENZA MAGGIORE NEL SET, SARA' LA PREDICTED CLASS
		this.predictedClass=Collections.max(resultMap.entrySet(), (res1, res2) -> res1.getValue() - res2.getValue()).getKey();
		
		//CALCOLO LA PERCENTIALE DI APPARTNENZA SULLA CLASSE PREDETTA DELLA QUERY RISPETTO A TUTTE LE K CLASSI
		value = resultMap.get(this.predictedClass);
		this.classificationConfidence = ((float)value/(float)k)*100;
		
		return this.predictedClass;
	}
	
	public Map<String,Double> getClassWithConfidence(List<ImgDescriptor> ids, int k){
		//LISTA CONTENENTE LE CLASSI
		Set<String> listOfClass = resultMap.keySet();
		//numberOfClass = resultMap.keySet().size();
		//System.out.println("NUMBER OF CLASS: " + numberOfClass);
		
		//CREO UNA LISTA CON:
		//KEY: CLASS
		//VALUE: CONFIDENCE 
		LinkedHashMap<String,Double> classWithConfidence = new LinkedHashMap<String,Double>();
		String foodClass;
		int numberOfImgPerClass;
		float confidence;
		for (Iterator<String> it = listOfClass.iterator(); it.hasNext(); ) {
	        String f = it.next();
	        //System.out.print(f + "-- \t" + resultMap.get(f) + "\t ");
	        numberOfImgPerClass = resultMap.get(f);
	        confidence =  ((float)numberOfImgPerClass/(float)k)*100;
	        //System.out.println(confidence);
	        classWithConfidence.put(f, (double)confidence);
	    }
		
		//ORDINO LA LISTA DELLE CLASSI IN BASE ALLA LORO CONFIDENCE PERCENTAGE
		Map<String,Double> sorted = classWithConfidence.entrySet().stream()
			    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue((v1,v2)->v1.compareTo(v2))))
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1,v2)->v1, LinkedHashMap::new));
		
		//STAMPA DELLA LISTA ORDINATA
		/*for (Map.Entry<String, Double> entry : sorted.entrySet()) {
			String classe = entry.getKey();
		    Double conf = entry.getValue();
		    System.out.println(classe + " - " + conf);
		 }*/
		
		//System.out.println("## PREDICTED FOOD CLASS ##" + predictedClass);
		
		return sorted;
	}
	
	/*public String classifyTest(List<ImgDescriptor> ids, int k){
		LinkedHashMap<String,Integer> resultMap=new LinkedHashMap<String,Integer>();
		String label;
		int value;
		for(int i = 0; i < k; i++){
			ImgDescriptor descriptor = ids.get(i);
			label=descriptor.getFoodClass();
			if(!resultMap.containsKey(label))
				resultMap.put(descriptor.getFoodClass(),0);
			value=resultMap.get(label);
			resultMap.replace(label,value+1);
		}
		this.predictedClass=Collections.max(resultMap.entrySet(), (res1, res2) -> res1.getValue() - res2.getValue()).getKey();		
		return this.predictedClass;
	}*/

}