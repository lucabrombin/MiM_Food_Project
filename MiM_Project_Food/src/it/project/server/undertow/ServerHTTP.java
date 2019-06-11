package it.project.server.undertow;

import io.undertow.Undertow;
import io.undertow.server.handlers.form.FormData;
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

import org.apache.http.HttpResponse;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;

public class ServerHTTP {

    public static void main(final String[] args) {
    	
        Undertow server = Undertow.builder()
                .addHttpListener(8100, "localhost")
                .setHandler(path()
                		//CREO E GESTISCO LE RICHIESTE CHE ARRIVO A QUESTO ENDPONT
                        .addPrefixPath("/endpoint", websocket(new WebSocketConnectionCallback() {

                            @Override
                            public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                                channel.getReceiveSetter().set(new AbstractReceiveListener() {

                                    @Override
                                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                                        final String receivedData = message.getData().toString();
                                        //System.out.println(receivedData);
                                        
                                        //PRENDO LA PARTE DELLA STRINGA RELATVA ALL'IMMAGINE
                                        String[] temp = receivedData.split(",");
                                        String base64img = temp[1]; 
                                        
                                        //DALLA STRINGA BASE64 OTTENGO L'IMG
                                        BufferedImage image = null;
                                        byte[] imageByte;
                                        try {
                                            BASE64Decoder decoder = new BASE64Decoder();
                                            imageByte = decoder.decodeBuffer(base64img);
                                            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
                                            image = ImageIO.read(bis);
                                            bis.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        
                                        //SOLUZIONE TEMPORANEA?
                                        //CREAO UN'IMMAGINE DAL BUFFEREDIMAGE TEMPORANEA COSI CHE POSSA ESSERE PASSATA IN INPUT ALL'EXTRACTOR
                                        File outputfile = new File("./query.jpg");
                                        ImageIO.write(image, "jpg", outputfile);
                                        
                                        //FormData data = receivedData;
                                        long time = 0;
                                        String messageData = null;
                                        
                                        /*******DEEP LEARNING PHASE*******/
                                        //DATA UN'IMMAGINE PASSATA DAL CLIENT, RESTITUIRE LE K IMMAGINE PIU' SIMILI
                                        //DATA UN'IMMAGINE PASSATA DAL CLIENT, RESTITUIRE LA CLASSE DI APPARTENANZE
                                        try (ElasticImgSearching imgSearch = new ElasticImgSearching(Parameters.PIVOTS_FILE_GOOGLENET, Parameters.TOP_K_QUERY)) {
                                        	
                                        	System.out.println("SEARCHING SIMILAR IMAGES..");
                                        	time = -System.currentTimeMillis();
                                        	
                                        	//AL MOMENTO QUESTA QUERY è PRE SETTATA, MA DOVREMO METTERE 
                                        	//IL MESSAGGIO CHE VIENE MANDATO DA CLIENT
                                        	//File imgQuery = new File(Parameters.FOLDER_QUERY, "1039733.jpg");                                      	
                                        	File imgQuery = new File("./query.jpg");
                                        	
                                			DNNExtractor extractor = new DNNExtractor();
                                			
                                			//ESTRAGGO LE FEATURE DELLA QUERY
                                			float[] imgFeatures = extractor.extract(imgQuery, Parameters.DEEP_LAYER);
                                			//L'ULTIMO PARAMETRO SERVER PER DEFINIRE LA CLASSE DELLA QUERY
                                			
                                			ImgDescriptor query = new ImgDescriptor(imgFeatures, imgQuery.getName(), "bruschetta");                      				
                                				
                                			//CERCO LE K IMMAGINI SIMILI ALLA QUERY PASSATA IN INGRESSO
                                			List<ImgDescriptor> res = imgSearch.search(query, Parameters.K);

                                			//CLASIFICATION DELLA QUERY UTILIZZANDO IL RISULTATO DELLA RICERCA
                                			KnnClassifier knn = new KnnClassifier();
                                			String predictedClass = knn.classify(res, Parameters.K);
                                			System.out.println("PREDICTED CLASS: " + predictedClass);
                                			
                                			//LISTA DELLE CLASSI ORDINATA PER CONFIDENCE 
                                			Map<String,Double> sortedLListedClass = knn.getClassWithConfidence(res, Parameters.K);
                                			                                			
                                			//ORDINO IL RISULTATO DELLA RICERCA IN BASE ALLA SIMILARITY
                                			System.out.println("REORDERING..");
                                			res = imgSearch.reorder(query, res);                          			
                                			
                                			JsonObject result = new JsonObject(); 
                                			JsonArray classe = new JsonArray();
                                			JsonArray img = new JsonArray();
                                			
                                			result.addProperty("predicted_class", predictedClass);
                                			
                                			boolean isTheBestClass = true;
                                			String bestResult = "not found";
                                			for (Map.Entry<String, Double> entry : sortedLListedClass.entrySet()) {
                                				JsonObject similar = new JsonObject();
                                				String foodClass = entry.getKey();
                                			    Double conf = entry.getValue();
                                			    
                                			    similar.addProperty("class", foodClass);
                                			    similar.addProperty("confidence", conf);
                                			    
                                			    JsonArray urls = new JsonArray();
                                			    for (int i = 0; i < res.size(); i++) {
                                			    	
                            			    		if(res.get(i).getFoodClass().equals(foodClass)) {
                            			    			if(isTheBestClass) {
                                    			    		bestResult = res.get(i).getId();
                                    			    		isTheBestClass = false;
                                    			    	} else {
                                    			    		urls.add(res.get(i).getId());
                                    			    	}
                                			    	}                             			    	                                			    
                                			    similar.add("imgs", urls);                              			    
                                			    }
                                			    
                                			    classe.add(similar);
                                			    //System.out.println(classe + " - " + conf);
                                			 }
                                			
                                			result.addProperty("best_result", bestResult);
                                			result.add("related_classes", classe);
                                			
                                			/*for (int i = 0; i < res.size(); i++) {
                                				JsonObject similar = new JsonObject();
                                				//System.out.println(i + " - " + (float) res.get(i).getDist() + "\t" + res.get(i).getId() + "\t" + res.get(i).getFoodClass() );
                                				String nameFieldImg = "img";
                                				String nameFieldClass = "classe";
                                				similar.addProperty(nameFieldImg, res.get(i).getId());
                                				similar.addProperty(nameFieldClass, res.get(i).getFoodClass());
                                				img.add(similar);
                                			}
                                			result.add("imgs", img);*/
                                			System.out.println(result.toString());
                                			messageData = result.toString();
                                			                                			
                                        } catch (ClassNotFoundException | ParseException e) {
											e.printStackTrace();
										}finally {
											/*******RISPOSTA AL CLIENT*******/
											//QUI ELABORIAMO IL RISULTATO DELLA RICERCA E PASSIAMO IL RISULATTO AL CLIENT
											//IL RISULTATO SARA' MANDANTO CON UN JSON
											System.out.println("SENDING RESULTS..");
											//System.out.println("SEND: " + messageData);
                                            for (WebSocketChannel session : channel.getPeerConnections()) {
                                                WebSockets.sendText(messageData, session, null);
                                            }
                                            time += System.currentTimeMillis();
                                            float seconds = 0;
                                            seconds = time/1000;
                                            System.out.println("ENDED IN: " + seconds + " SECONDS");
                                        }
                                    }
                                });
                                channel.resumeReceives();
                            }

                        }))
                        //PATH PER LA PAGINA HOME, LEGGE IL FILE INDEX E LO VISUALIZZA 
                        .addPrefixPath("/", resource(new ClassPathResourceManager(ServerHTTP.class.getClassLoader(), ServerHTTP.class.getPackage()))
                                .addWelcomeFiles("./Foodle-master/index.html")))
                .build();

        server.start();
    }
}

