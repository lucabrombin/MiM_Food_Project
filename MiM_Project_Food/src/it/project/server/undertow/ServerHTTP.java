package it.project.server.undertow;

import java.util.Base64;

import java.util.List;
import java.util.stream.Collectors;

import io.undertow.Undertow;
//import io.undertow.examples.UndertowExample;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.img.elasticsearch.ElasticImgSearching;
import it.unipi.ing.mim.deep.tools.KnnClassifier;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;

import org.apache.lucene.queryparser.classic.ParseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

public class ServerHTTP {
	
	static int numberOfRequest = 0;
	
    public static void main(final String[] args) {  	
    Undertow server = Undertow.builder()
		.addHttpListener(8100, "localhost")
		.setHandler(path()
			// creates and handles requests arriving to this endpoint
			.addPrefixPath( "/endpoint", websocket(new WebSocketConnectionCallback() {
    	
			@Override
			public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
				channel.getReceiveSetter().set( new AbstractReceiveListener() {
    			
					@Override
		            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
						long startTime = System.currentTimeMillis();
						
						numberOfRequest++;
		            	System.out.println("-- DEBUG -- Number of requests: " + numberOfRequest);
		                
		            	String receivedData = message.getData().toString();
		                System.out.println("-- DEBUG -- Received message " + receivedData);
											
						ServerHTTP myServer = new ServerHTTP(); 
											
						// creates the image query for the string base64
						BufferedImage queryImage = myServer.createQueryImage(receivedData);
		                                    
		                // FormData data = receivedData;
		                //long time = 0;
		                String messageData = null;
		                                    
		                /******* DEEP LEARNING PHASE *******/
		                // given the image received from the client, returns the k most similar images and the class of the image
		                try(ElasticImgSearching imgSearch = new ElasticImgSearching(Parameters.PIVOTS_FILE, Parameters.STORAGE_FILE, Parameters.TOP_K_QUERY)) {
		                	
		                	System.out.println("-- DEBUG -- Searching for similar images...");
		             
		                	// saves the received image in a file
		                	File imageQueryFile = new File("./receivedQuery" + Integer.toString(numberOfRequest) + ".jpg");
		                	ImageIO.write(queryImage, "jpg", imageQueryFile);
		                	
		        			DNNExtractor extractor = new DNNExtractor();
		        			
		        			// extracts features from the query
		        			float[] imgFeatures = extractor.extract(imageQueryFile, Parameters.DEEP_LAYER);
		        		
		        			// the last parameter is used to define the class of the query, it will be updated after the knn classification
		        			ImgDescriptor query = new ImgDescriptor(imgFeatures, imageQueryFile.getName(), null);                      				
		        					        			
		        			// finds the k most similar images to the given query
							List<ImgDescriptor> res = imgSearch.search(query, Parameters.K);
	
							// Orders result according to distance from the query 
		        			System.out.println("-- DEBUG -- Reordering...");
		        			res = imgSearch.reorder(query, res); 
		
		        			long startTimeKnn = System.currentTimeMillis();
							
		        			// classifies the query using the results of the search operation 
		        			KnnClassifier knn = new KnnClassifier();
		        			//Map<String, Double> sortedListedClasses = knn.classify(res);
		        			Map<String, Double> sortedListedClasses = knn.weightedClassify(res, query);
		        			
		        			System.out.println("-- DEBUG -- Predicted class: " + knn.getPredictedClass());
		        			query.setFoodClass(knn.getPredictedClass());
		        			
		        			long endTimeKnn = System.currentTimeMillis();
		        			System.out.println("-- DEBUG -- Time required to perform KNN (in milliseconds): " + (endTimeKnn - startTimeKnn));

		        			// creates the response message for the client
							messageData = myServer.createJSON(sortedListedClasses, res, knn.getPredictedClass()); 
							
							long endTime = System.currentTimeMillis();
							System.out.println("-- DEBUG -- Time required to search (in milliseconds): " + (endTime - startTime));
		                } catch (ClassNotFoundException | ParseException e) {
							e.printStackTrace();
						} finally {
							// the result of the search is elaborated and passed to the client in json format
							System.out.println("-- DEBUG -- Sending results: " + messageData);
		                    for(WebSocketChannel session : channel.getPeerConnections()) {
		                        WebSockets.sendText(messageData, session, null);
		                    }
		                    //time += System.currentTimeMillis();
		                    //System.out.println("ENDED IN: " + time + " MS");
		                }
					}
				});
                channel.resumeReceives();
    		}
			}))
            
			// path for the home page, reads the index file and visualizes it
        	.addPrefixPath("/", resource(new ClassPathResourceManager(ServerHTTP.class.getClassLoader(), ServerHTTP.class.getPackage()))
    		.addWelcomeFiles("./Foodle-master/index.html")))
			.build();
    
    	server.start();
	}
	
    // creates the query image from the string in base64 format received by the client
	public BufferedImage createQueryImage(String receivedData) throws IOException {
		// gets the part of the string base64 related to the image
        String[] temp = receivedData.split(",");
		String base64img = temp[1];
		
		// uses a Base64 decoder to retrieve the image
		byte[] decodedBytes = Base64.getDecoder().decode(base64img);
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
		
		return image;
	}

	// creates the response JSON message for the client
	public String createJSON(Map<String, Double> sortedListedClasses, List<ImgDescriptor> res, String predictedClass) {
		String messageData;
		
		JsonObject result = new JsonObject(); 
		result.addProperty("predicted_class", predictedClass);
		
		JsonArray imgClass = new JsonArray();	
		//boolean isTheBestClass = true;
	
		JsonObject similarFood;
		String foodClass, bestResult = "not found";		
		
		//For each detected similar class a JsonObject is created specifying
		// - the class
		// - the confidence 
		// - a list of the IDs of all images (detected by the search on Elasticsearch) that belong to that class
		for(Map.Entry<String, Double> entry : sortedListedClasses.entrySet()) {
			similarFood = new JsonObject();
			foodClass = entry.getKey();
			similarFood.addProperty("class", foodClass);
			similarFood.addProperty("confidence", entry.getValue());
				
			List<ImgDescriptor> listImages = res.stream().filter(p -> p.getFoodClass().equals(entry.getKey())).collect(Collectors.toList());			
			//System.out.println("-- DEBUG -- Food class: " + foodClass + "   " + listImages.size() + " elements of the class: " + listImages);
			
			if(foodClass.equals(predictedClass))  
				bestResult = res.get(0).getId();
			//System.out.println("-- DEBUG -- " + res.get(0));
			//System.out.println("-- DEBUG -- " + res.get(0).getId());
					
			JsonArray urls = new JsonArray();
			for(int i = 0; i < listImages.size(); i++) {
				urls.add(listImages.get(i).getId());
			}
			similarFood.add("imgs", urls);
			
			imgClass.add(similarFood);
		}
		
		result.addProperty("best_result", bestResult);
		result.add("related_classes", imgClass);
		
		//json object is converted to string to sent it to the client
		messageData = result.toString();
		
		System.out.println("-- DEBUG -- Messaggio: " + messageData);
		return messageData;
	}
}

