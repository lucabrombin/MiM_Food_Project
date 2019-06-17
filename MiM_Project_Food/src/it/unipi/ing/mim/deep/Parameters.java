package it.unipi.ing.mim.deep;

import java.io.File;

public class Parameters {
	
	/*
	// Parameters for BVLC
	public static final String DEEP_PROTO = "data/caffe/train_val.prototxt";
	public static final String DEEP_MODEL = "data/caffe/bvlc_reference_caffenet.caffemodel";
	public static final int IMG_WIDTH = 227;
	public static final int IMG_HEIGHT = 227;
	public static final String DEEP_LAYER = "fc7";	
	// Features storage file 	
	public static final File STORAGE_FILE = new File("data/bvlc_features/deep.seq.dat");
	*/
	
	
	/*
	// Parameters for VGG 16
	public static final String DEEP_PROTO = "data/VGG/VGG_ILSVRC_16_layers_deploy.prototxt";
	public static final String DEEP_MODEL = "data/VGG/VGG_ILSVRC_16_layers.caffemodel";
	public static final int IMG_WIDTH = 224;
	public static final int IMG_HEIGHT = 224;
	public static final String DEEP_LAYER = "fc7"; 		
	// Features storage file
	public static final File STORAGE_FILE = new File("data/vgg_features/deep.seq.dat");
	*/
	
	
	// Parameters for GOOGLE NET
	public static final String DEEP_PROTO = "data/google/deploy.prototxt";
	public static final String DEEP_MODEL = "data/google/bvlc_googlenet.caffemodel";
	public static final int IMG_WIDTH = 224;
	public static final int IMG_HEIGHT = 224;
	public static final String DEEP_LAYER = "pool5/7x7_s1";
	//public static final String DEEP_LAYER = "loss3/classifier"; 
	// Features storage file
	public static final File STORAGE_FILE = new File("data/google_features/deep.seq.dat");
	
	public static final double[] MEAN_VALUES = {104, 117, 123, 0};				
	
	//Image Source Folder
	public static final File SRC_FOLDER = new File("data/img");
	public static final File FOLDER_QUERY = new File("data/img/bruschetta");
	
	//Pivots files
	//public static final File  PIVOTS_FILE_BVLC = new File("out/deep.bvlc.pivots.dat");
	public static final File  PIVOTS_FILE_VGG = new File("out/deep.vgg.pivots.dat");
	public static final File  PIVOTS_FILE_GOOGLENET = new File("out/deep.googlenet.pivots.dat");

	//Number of pivots
	public static final int NUM_PIVOTS = 1000;
		
	//Lucene Index
	public static final String INDEX_NAME = "fooddeep_googlenet";
	//public static final String INDEX_NAME = "fooddeep";
	
	//Top K pivots for indexing
	public static final int TOP_K_IDX = 100; //DA VEDEREEEE

	//k-Nearest Neighbors
	public static final int K = 30;
	
	//Top K pivots for searching
	public static final int TOP_K_QUERY = 50;
	
	//HTML Output Parameters
	public static final  String BASE_URI = "file:///" + Parameters.FOLDER_QUERY.getAbsolutePath() + "/";
	public static final File RESULTS_HTML = new File("out/deep.seq.html");
	public static final File RESULTS_HTML_ELASTIC = new File("out/deep.elastic.html");
	public static final File RESULTS_HTML_REORDERED = new File("out/deep.reordered.html");

}
