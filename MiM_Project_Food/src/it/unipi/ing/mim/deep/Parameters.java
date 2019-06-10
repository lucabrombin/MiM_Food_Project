package it.unipi.ing.mim.deep;

import java.io.File;

public class Parameters {
	
	//PER BVLC
	//public static final String DEEP_PROTO = "data/caffe/train_val.prototxt";
	//public static final String DEEP_MODEL = "data/caffe/bvlc_reference_caffenet.caffemodel";
	
	//PER VGG 16
	//public static final String DEEP_PROTO = "data/VGG/VGG_ILSVRC_16_layers_deploy.prototxt";
	//public static final String DEEP_MODEL = "data/VGG/VGG_ILSVRC_16_layers.caffemodel";
	
	//PER GOOGLE NET
	public static final String DEEP_PROTO = "data/google/train_val.prototxt";
	public static final String DEEP_MODEL = "data/google/bvlc_googlenet.caffemodel";
	public static final double[] MEAN_VALUES = {104, 117, 123, 0};
	
	//PER GOOGLE NET
	public static final String DEEP_LAYER = "pool5/7x7_s1";
	//public static final String DEEP_LAYER = "loss3/classifier"; 
	
	//PER VGG 16 E PER BVLC
	//public static final String DEEP_LAYER = "fc7"; 					
	
	//PER BVLC
	//public static final int IMG_WIDTH = 224;
	//public static final int IMG_HEIGHT = 224;
	
	//PER VGG e GOOGLENET
	public static final int IMG_WIDTH = 224;
	public static final int IMG_HEIGHT = 224;
	
	
	//Image Source Folder
	public static final File SRC_FOLDER = new File("data/img");
	public static final File FOLDER_QUERY = new File("data/img/bruschetta");
	
	//Features Storage File
	public static final File STORAGE_FILE = new File("data/google_features/deep.seq.dat");
	//public static final File STORAGE_FILE = new File("data/vgg_features/deep.seq.dat");
	//public static final File STORAGE_FILE = new File("data/bvlc_features/deep.seq.dat");
	
	//k-Nearest Neighbors
	public static final int K = 30;
	
	//Pivots File
	public static final File  PIVOTS_FILE_BVLC = new File("out/deep.bvlc.pivots.dat");
	public static final File  PIVOTS_FILE_GOOGLENET = new File("out/deep.googlenet.pivots.dat");
	public static final File  PIVOTS_FILE_VGG = new File("out/deep.vgg.pivots.dat");
	
	//Number Of Pivots
	public static final int NUM_PIVOTS = 100;

	//Top K pivots For Indexing
	public static final int TOP_K_IDX = 50; //DA VEDEREEEE
	
	//Top K pivots For Searching
	public static final int TOP_K_QUERY = 10;
	
	//Lucene Index
	public static final String INDEX_NAME = "fooddeep_googlenet";
	
	//public static final String INDEX_NAME = "fooddeep";
	
	//HTML Output Parameters
	public static final  String BASE_URI = "file:///" + Parameters.FOLDER_QUERY.getAbsolutePath() + "/";
	public static final File RESULTS_HTML = new File("out/deep.seq.html");
	public static final File RESULTS_HTML_ELASTIC = new File("out/deep.elastic.html");
	public static final File RESULTS_HTML_REORDERED = new File("out/deep.reordered.html");

}
