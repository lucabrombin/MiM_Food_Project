package it.unipi.ing.mim.deep.tools;

import java.io.File;
import java.util.List;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.img.elasticsearch.ElasticImgSearching;

public class TestClassification {

	public static void main(String[] args) throws Exception {
		
		TestClassification myTest = new TestClassification();
		
		//FOLDER DELLE IMMAGINI PER IL TESTING
		//File imgFolder = new File("C:\\Users\\lbrom\\Desktop\\UPMC_Food101\\images\\train");
		File imgFolder = Parameters.SRC_FOLDER;
		File[] dir = imgFolder.listFiles();
		
		DNNExtractor extractor = new DNNExtractor();

		//NUMERO DI CLASSI DA TESTARE
		int numberOfFolder = dir.length;
		//AL MOMENTO SONO STATE ESTRATTE LE FEATURE DELLE PRIME 30 CLASSI
		numberOfFolder = 30;

		//CLASSE CORRISPONDENTE ALLA QUERY 
		String actual_class = null;
		//CLASSE PREDETTA DAL CLASSIFICATORE
		String predicted_class = null;

		int tested = 0;
		int matchedClass = 0;
		
		//ACCURACY DEL CLASSIFIER
		float accuracy;
		
		//PER OGNI CLASSE
		for (int k = 0; k < numberOfFolder; k++) { 
			
			//PER VEDERE L'ACCURACY DELLA CLASSE ATTUALE
			//accuracy = 0;
			
			//NOME DEI FILE DELLA K-ESIMA CLASSE
			File[] files = dir[k].listFiles();
			
			//OTTENGO IL NOME DELLA CLASSE ATTUALE
			actual_class = myTest.getActualClass(dir, k);
			System.out.println("ACTUAL CLASS: " + actual_class);

			int numb = files.length;
			numb = 150;
			
			//PER OGNI SAMPLE DELLA K-ESIMA CLASSE 
			for(int i = 0; i < numb; i++){
				
				tested++;
				
				File imgQuery = new File(""+files[i]);

				try (ElasticImgSearching imgSearch = new ElasticImgSearching(Parameters.PIVOTS_FILE_GOOGLENET, Parameters.TOP_K_QUERY)) {
					//ESTRAGGO LE FEATURE DELLA QUERY
					float[] imgFeatures = extractor.extract(imgQuery, Parameters.DEEP_LAYER);
					//L'ULTIMO PARAMETRO SERVER PER DEFINIRE LA CLASSE DELLA QUERY
					ImgDescriptor query = new ImgDescriptor(imgFeatures, imgQuery.getName(), "bruschetta");
					//CERCO LE K IMMAGINI SIMILI ALLA QUERY PASSATA IN INGRESSO
					List<ImgDescriptor> res = imgSearch.search(query, Parameters.K);
					//SORTING DELLE IMMAGINI
					res = imgSearch.reorder(query, res); 
					//CLASIFICATION DELLA QUERY UTILIZZANDO IL RISULTATO DELLA RICERCA
					KnnClassifier knn = new KnnClassifier();
					//10-NN CLASSIFIER
					String predictedClass = knn.classify(res, 10);
					
					if(predictedClass.equals(actual_class)) {
						matchedClass++;
					}
					
					//System.out.println("PREDICTED CLASS: " + predictedClass + "\tACTUAL CLASS: " + actual_class);
					//DA QUI SI POTREBBERO SALVARE LA CLASSE PREDETTA E L'ACTUAL CLASS E CREARE LA CONFUSION MATRIX 
					//SI POSSONO ANCHE CALCOLARE IL FPR E FNR. 
				}
			}
			
			//COMPUTE ACCURACY
			accuracy = (float)matchedClass/(float)tested;
			System.out.println("ACCURACY: "+ accuracy);
		}
		
		accuracy = (float)matchedClass/(float)tested;
		System.out.println("FINAL ACCURACY: "+ accuracy);
		
	}
	
	public String getActualClass(File[] dir, int k) {
		String actual_class;
		
		//OTTENGO IL NOME DELLA CLASSE ATTUALE
		actual_class = ""+dir[k];
		//actual_class = (actual_class.split("train")[1]).substring(1);
		actual_class = (actual_class.split("img")[1]).substring(1);
		
		return actual_class;
	}
		
}
