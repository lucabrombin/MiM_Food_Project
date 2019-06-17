package it.unipi.ing.mim.img.elasticsearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.lucene.queryparser.classic.ParseException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.deep.seq.SeqImageSearch;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;
import it.unipi.ing.mim.deep.tools.Output;

// This class extracts the features for a given image and search for k most similar images

public class ElasticImgSearching implements AutoCloseable {

	private RestHighLevelClient client;
	
	private Pivots pivots;
	
	private int topKSearch;	
	
	private Map<String,ImgDescriptor> imgDescMap;
	
	//private static String foodClass;
		
	/*
	public static void main(String[] args) throws Exception {	
		try (ElasticImgSearching imgSearch = new ElasticImgSearching(Parameters.PIVOTS_FILE_GOOGLENET, Parameters.TOP_K_QUERY)) {
			// image query file
			File imgQuery = new File(Parameters.FOLDER_QUERY, "1003796.jpg");
			
			foodClass = "bruschetta";
			
			DNNExtractor extractor = new DNNExtractor();
			
			float[] imgFeatures = extractor.extract(imgQuery, Parameters.DEEP_LAYER);
			
			ImgDescriptor query = new ImgDescriptor(imgFeatures, imgQuery.getName(), foodClass);
			
			List<ImgDescriptor> res = imgSearch.search(query, Parameters.K);
			
			Output.toHTML(res, Parameters.BASE_URI, Parameters.RESULTS_HTML_ELASTIC);
					
			res = imgSearch.reorder(query, res);
			Output.toHTML(res, Parameters.BASE_URI, Parameters.RESULTS_HTML_REORDERED);
		}
	}*/
	
	// constructor
	public ElasticImgSearching(File pivotsFile, File datasetFile, int k) throws ClassNotFoundException, IOException {
		// loads pivots
		pivots = new Pivots(pivotsFile);
		topKSearch = k;
		
		// loads extracted features and initialized imgDescMap with couples <imageID, descriptor>
		imgDescMap = new HashMap<String, ImgDescriptor>();
		List<ImgDescriptor> descriptors = FeaturesStorage.load(datasetFile);
		for(ImgDescriptor el:descriptors) {
			imgDescMap.put(el.getId(), el);
		}
		
		// initializes the REST client to interact with Elasticsearch
		RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
		client = new RestHighLevelClient(builder);
	}
	
	// closes the REST client
	public void close() throws IOException {
		client.close();
	}
	
	// searches for the k most similar images to queryF
	public List<ImgDescriptor> search(ImgDescriptor queryF, int k) throws ParseException, IOException, ClassNotFoundException{
		List<ImgDescriptor> res = new ArrayList<ImgDescriptor>();
		String id, imgTxt, foodClass;
		ImgDescriptor imgD;		
		
		// converts the queryF to text format using pivots
		String queryTxt = pivots.features2Text(queryF, k);
		
		System.out.println("-- DEBUG -- Text format of the query: " + queryTxt);
		
		// creates the search object		
		SearchRequest sr = composeSearch(queryTxt, k);
		
		// performs search on Elasticsearch
		SearchResponse searchResponse = client.search(sr, RequestOptions.DEFAULT); 
		SearchHit[] hits = searchResponse.getHits().getHits();
	
		// for each result retrieves the corresponding ImgDescriptor from imgDescMap and call setDist to set the score
		for(int i = 0; i < hits.length; i++) {
			Map<String, Object> metadata = hits[i].getSourceAsMap();
			
			// retrieves the id
			id = (String)metadata.get(Fields.ID);
			
			// retrieves the text format
			imgTxt = (String)metadata.get(Fields.IMG);
			
			// retrieves the class
			foodClass = (String)metadata.get(Fields.FOOD_CLASS);
			
			System.out.println("-- DEBUG -- Result " + i + " - ID: "+ id + "\t Text format: " + imgTxt + "\t Class: " + foodClass);
			
			// retrieves the corresponding ImgDescriptor from the set of all image descriptors imgDescMap
			imgD = imgDescMap.get(id);
			res.add(imgD);		
		}		
		return res;
	}
	
	// sets the request for Elasticsearch
	private SearchRequest composeSearch(String query, int k) {
		// initializes the request
		SearchRequest searchRequest = new SearchRequest(Parameters.INDEX_NAME);
		
		// sets query 
		QueryBuilder simpleQuery = QueryBuilders.multiMatchQuery(query, Fields.IMG);
		
		SearchSourceBuilder sb = new SearchSourceBuilder(); 	
		sb.size(k);
		sb.query(simpleQuery);
		
		searchRequest.types("doc");
		searchRequest.source(sb);
	    return searchRequest;
	}
	
	// for each result, evaluates the distance with the query, calls setDist to set the distance, then sorts the results
	public List<ImgDescriptor> reorder(ImgDescriptor queryF, List<ImgDescriptor> res) throws IOException, ClassNotFoundException {
		double dist;
		for(ImgDescriptor el:res) {
			dist = el.distance(queryF);
			el.setDist(dist);	
		}
		res.sort(ImgDescriptor::compareTo);
		return res;
	}
}
