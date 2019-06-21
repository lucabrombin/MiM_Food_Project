package it.unipi.ing.mim.img.elasticsearch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;

import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.Parameters;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;

// This class creates the index on Elasticsearch using the previously selected pivots 

public class ElasticImgIndexing implements AutoCloseable {
	
	private Pivots pivots;
	
	private List<ImgDescriptor> imgDescDataset;
	private int topKIdx;
	
	private RestHighLevelClient client;
		
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		try (ElasticImgIndexing esImgIdx = new ElasticImgIndexing(Parameters.PIVOTS_FILE, Parameters.STORAGE_FILE, Parameters.TOP_K_IDX)) {
			
			long startTime = System.currentTimeMillis();
			
			esImgIdx.createIndex();
			esImgIdx.index();	
			
			long endTime = System.currentTimeMillis();
			System.out.println("-- DEBUG -- Time required to index (milliseconds): " + (endTime - startTime));
		}		
	}
	
	// constructor
	public ElasticImgIndexing(File pivotsFile, File datasetFile, int k) throws IOException, ClassNotFoundException {
		// loads pivots 
		pivots = new Pivots(pivotsFile);
		
		// loads descriptors of images
		imgDescDataset = FeaturesStorage.load(datasetFile);
		topKIdx = k;
		
		// initializes the REST client to interact with Elasticsearch
		RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
		client = new RestHighLevelClient(builder);
	}
	
	// closes the REST client
	public void close() throws IOException {
		client.close();
	}
	
	// creates the Elasticsearch index
	public void createIndex() throws IOException {
		IndicesClient idx = client.indices();
		CreateIndexRequest request = new CreateIndexRequest(Parameters.INDEX_NAME);
		
		// index has 1 shard, 0 replica and uses the whitespace analyzer
		Builder s = Settings.builder()
		  .put("index.number_of_shards", 1)
		  .put("index.number_of_replicas", 0)
		  .put("analysis.analyzer.first.type", "whitespace");
	
		request.settings(s);
		idx.create(request, RequestOptions.DEFAULT);
		
		System.out.println("-- DEBUG -- Index created");
	}
	
	// indices all the extracted features from images in a dataset into the Elasticsearch index
	public void index() throws IOException {
		IndexRequest request;
		
		// each request contains:
		// - the id of the image
		// - the text corresponding to the image, computed using features2text method of Pivots class
		// - the class of the image
		for(ImgDescriptor el: imgDescDataset) {
			//System.out.println("-- DEBUG -- Indexing image " + el.getId());
			request = composeRequest(el.getId(), pivots.features2Text(el, topKIdx), el.getFoodClass());
			client.index(request, RequestOptions.DEFAULT);
		}
	}
	
	// initializes and fills IndexRequest object with the id, the text format and the class
	private IndexRequest composeRequest(String id, String imgTxt, String foodClass){			
		IndexRequest request = new IndexRequest(Parameters.INDEX_NAME, "doc");
		
		Map<String,Object> jsonMap = new HashMap<>();
		jsonMap.put(Fields.ID, id);
		jsonMap.put(Fields.IMG, imgTxt);
		jsonMap.put(Fields.FOOD_CLASS, foodClass);
		request.source(jsonMap);
		
		return request;
	}
}
