package it.unipi.ing.mim.deep;

import java.io.File;

public class Parameters {
	
	/*
	// Parameters for GOOGLE NET
	public static final String DEEP_PROTO = "data/google/deploy.prototxt";
	public static final String DEEP_MODEL = "data/google/bvlc_googlenet.caffemodel";
	public static final int IMG_WIDTH = 224;
	public static final int IMG_HEIGHT = 224;
	public static final String DEEP_LAYER = "pool5/7x7_s1";
	//public static final String DEEP_LAYER = "loss3/classifier"; 
	// Features storage file
	// Dataset food101
	//public static final File STORAGE_FILE = new File("data/google_features/food101.deep.seq.dat");
	// Dataset UMPC
	public static final File STORAGE_FILE = new File("data/google_features/deep.seq.dat");
	*/

	/**/
	// Parameters for VGG 16
	public static final String DEEP_PROTO = "data/VGG/VGG_ILSVRC_16_layers_deploy.prototxt";
	public static final String DEEP_MODEL = "data/VGG/VGG_ILSVRC_16_layers.caffemodel";
	public static final int IMG_WIDTH = 224;
	public static final int IMG_HEIGHT = 224;
	public static final String DEEP_LAYER = "fc7"; 		
	// Features storage file
	// Dataset food101
	//public static final File STORAGE_FILE = new File("data/vgg_features/food101.deep.seq.dat");
	// Dataset UMPC
	public static final File STORAGE_FILE = new File("data/vgg_features/deep.seq.dat");
	/**/
	
	public static final double[] MEAN_VALUES = {104, 117, 123, 0};				
	
	// Images folder
	// Dataset food101
	//public static final File SRC_FOLDER = new File("data/img/food101");
	// Dataset UMPC
	public static final File SRC_FOLDER = new File("data/img/UMPC");
	
	// Images for test folder
	public static final File SRC_TEST_FOLDER = new File("data/img/test");
	
	// Pivots files
	// Dataset food101
	//public static final File  PIVOTS_FILE = new File("out/deep.googlenet_food101.pivots.dat");
	//public static final File  PIVOTS_FILE = new File("out/deep.vgg_food101.pivots.dat");
	// Dataset UMPC
	//public static final File  PIVOTS_FILE = new File("out/deep.googlenet_UMPC.pivots.dat");
	public static final File  PIVOTS_FILE = new File("out/deep.vgg_UMPC.pivots.dat");

	// Number of pivots
	public static final int NUM_PIVOTS = 1000;
		
	// Lucene Index
	//public static final String INDEX_NAME = "fooddeep_googlenet_food101";
	//public static final String INDEX_NAME = "fooddeep_vgg_food101";
	//public static final String INDEX_NAME = "fooddeep_googlenet_umpc";
	public static final String INDEX_NAME = "fooddeep_vgg_umpc";
	
	// Top K pivots for indexing
	public static final int TOP_K_IDX = 100; 

	// Top K pivots for searching
	public static final int TOP_K_QUERY = 20;

	// k-Nearest Neighbors
	public static final int K = 30;
	
	/*
	// HTML Output Parameters
	public static final  String BASE_URI = "file:///" + Parameters.FOLDER_QUERY.getAbsolutePath() + "/";
	public static final File RESULTS_HTML = new File("out/deep.seq.html");
	public static final File RESULTS_HTML_ELASTIC = new File("out/deep.elastic.html");
	public static final File RESULTS_HTML_REORDERED = new File("out/deep.reordered.html");
	*/
}
