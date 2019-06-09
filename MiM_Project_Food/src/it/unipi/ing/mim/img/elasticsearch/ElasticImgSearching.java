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

public class ElasticImgSearching implements AutoCloseable {

	private RestHighLevelClient client;
	
	private Pivots pivots;
	
	private int topKSearch;
	
	private Map<String,ImgDescriptor> imgDescMap;
	
	private static String foodClass;
		
	public static void main(String[] args) throws Exception {
		
		try (ElasticImgSearching imgSearch = new ElasticImgSearching(Parameters.PIVOTS_FILE, Parameters.TOP_K_QUERY)) {
			//Image Query File
			File imgQuery = new File(Parameters.FOLDER_QUERY, "1003796.jpg");
			
			foodClass = "bruschetta";
			
			DNNExtractor extractor = new DNNExtractor();
			
			float[] imgFeatures = extractor.extract(imgQuery, Parameters.DEEP_LAYER);
			
			ImgDescriptor query = new ImgDescriptor(imgFeatures, imgQuery.getName(), foodClass);
					
			long time = -System.currentTimeMillis();
			List<ImgDescriptor> res = imgSearch.search(query, Parameters.K);
			time += System.currentTimeMillis();
			//System.out.println("Search time: " + time + " ms");
			
			Output.toHTML(res, Parameters.BASE_URI, Parameters.RESULTS_HTML_ELASTIC);
			
			//Uncomment for the optional step
			res = imgSearch.reorder(query, res);
			Output.toHTML(res, Parameters.BASE_URI, Parameters.RESULTS_HTML_REORDERED);
		}
	}
	
	//TODO
	public ElasticImgSearching(File pivotsFile, int topKSearch) throws ClassNotFoundException, IOException {
		//Initialize pivots, imgDescMap, REST
		
		pivots = new Pivots(pivotsFile);
		this.topKSearch = topKSearch;
		
		RestClientBuilder builder = RestClient.builder(new HttpHost("localhost",9200,"http"));
		client = new RestHighLevelClient(builder);
		
		//Optional part:
		imgDescMap = new HashMap<String,ImgDescriptor>();
		List<ImgDescriptor>descriptors = FeaturesStorage.load(Parameters.STORAGE_FILE);
		for(ImgDescriptor el:descriptors) {
			imgDescMap.put(el.getId(), el);
		}
	}
	
	//TODO
	public void close() throws IOException {
		//close REST client
	}
	
	//TODO
	public List<ImgDescriptor> search(ImgDescriptor queryF, int k) throws ParseException, IOException, ClassNotFoundException{
		List<ImgDescriptor> res = new ArrayList<ImgDescriptor>();
		
		//convert queryF to text
		String queryString = pivots.features2Text(queryF, k);
		
		//call composeSearch to get SearchRequest object
		
		SearchRequest sr = composeSearch(queryString,k);
		
		
		//perform elasticsearch search
		SearchResponse searchResponse = client.search(sr,RequestOptions.DEFAULT); 
		SearchHit[] hits = searchResponse.getHits().getHits();
		
		
		//LOOP to fill res
			//for each result retrieve the ImgDescriptor from imgDescMap and call setDist to set the score
		String id;
		String imgTxt;
		ImgDescriptor imgD;
		String foodClass;
		
		for (int i = 0; i < hits.length; i++) {
			Map<String,Object> metadata = hits[i].getSourceAsMap();
			id = (String)metadata.get(Fields.ID);
			imgTxt = (String)metadata.get(Fields.IMG);
			foodClass = (String)metadata.get(Fields.FOOD_CLASS);
			
			//System.out.println("ID: "+ id + "\t IMG_TXT:" + imgTxt + "\t CLASS: " + foodClass);
			
			imgD = imgDescMap.get(id);
			
			ImgDescriptor el = new ImgDescriptor(imgD.getFeatures(),id, foodClass);
			res.add(el);
			
		}		
		
		
		return res;
	}
	
	//TODO
	private SearchRequest composeSearch(String query, int k) {
		//Initialize SearchRequest and set query and k
		SearchRequest searchRequest = new SearchRequest(Parameters.INDEX_NAME);
		QueryBuilder simpleQuery = QueryBuilders.multiMatchQuery(query, Fields.IMG);
		SearchSourceBuilder sb = new SearchSourceBuilder(); 
		
		sb.size(k);
		sb.query(simpleQuery);
		searchRequest.types("doc");
		searchRequest.source(sb);
		
	    return searchRequest;
	}
	
	//TODO
	public List<ImgDescriptor> reorder(ImgDescriptor queryF, List<ImgDescriptor> res) throws IOException, ClassNotFoundException {
		//Optional Step!!!
		//LOOP
		//for each result evaluate the distance with the query, call  setDist to set the distance, then sort the results
		
		double dist;
		for(ImgDescriptor el:res) {

			dist = el.distance(queryF);
			el.setDist(dist);	
		}
		
		res.sort(ImgDescriptor::compareTo);
		
		return res;
	}
}
