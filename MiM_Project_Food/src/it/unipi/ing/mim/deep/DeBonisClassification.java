package it.unipi.ing.mim.deep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_dnn.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class DeBonisClassification {
	
	private Net net;
	private Scalar meanValues;
	private Size imgSize;
	
	public DeBonisClassification() {		
		//Creates the importer of Caffe framework network
		net = readNetFromCaffe(new File(Parameters.DEEP_PROTO).getPath(), new File(Parameters.DEEP_MODEL).getPath());
        
        imgSize = new Size(Parameters.IMG_WIDTH, Parameters.IMG_HEIGHT);
        
        if (Parameters.MEAN_VALUES != null) {
			meanValues = new Scalar(Parameters.MEAN_VALUES[0], Parameters.MEAN_VALUES[1], Parameters.MEAN_VALUES[2], Parameters.MEAN_VALUES[3]);
        }
	}
	
	/* Find best class for the blob (i. e. class with maximal probability) */
    public static void getMaxClass(Mat probBlob, Point classId, double[] classProb) {
        Mat probMat = probBlob.reshape(1, 1); //reshape the blob to 1x1000 matrix
        minMaxLoc(probMat, null, classProb, null, classId, null);
    }
    
    public String getActualClass(File[] dir, int k) {
		// name of the actual_class		
		String actual_class = "" + dir[k];
		actual_class = (actual_class.split("test")[1]).substring(1);		
		return actual_class;
	}	

    public static void main(String[] args) throws Exception {
    	DeBonisClassification c = new DeBonisClassification();
    	
    	String actual_class = null, predicted_class = null;
    	
    	int total = 0;
    	double accuracy = 0.0;
    	
    	LinkedHashMap<String, Double> true_positives = new LinkedHashMap<String, Double>();
    	
    	File imgFolder = Parameters.SRC_TEST_FOLDER;	
		File[] dir = imgFolder.listFiles();
    	
    	List<String> classNames = new ArrayList<String>();

    	// initializes a list with all the names of the classes
    	for (int k = 0; k < dir.length; k++) {
    		classNames.add(c.getActualClass(dir, k));
    		true_positives.put(c.getActualClass(dir, k), 0.0);
    	}
			   	
    	// each class is examined
    	for (int k = 0; k < dir.length; k++) {
    		File[] files = dir[k].listFiles();
    		
    		actual_class = c.getActualClass(dir, k);
    		
    		// each image of each class is examined
			for(int i = 0; i < files.length; i++) {			
		    	File image = new File("" + files[i]);
		    	total++;
		    	
		    	Mat img = imread(image.getPath());
		    	
		    	resize(img, img, c.imgSize);
		    	
		    	// converts the image Mat to dnn::Blob image batch
		    	Mat inputBlob = blobFromImage(img);
		    			
		    	// sets the input of the network
		   		c.net.setInput(inputBlob, "data", 1.0, c.meanValues); //meanValues o null
		    		
		   		// traverses the CNN until the end (last layer)
				Mat prob = c.net.forward("softmax");
				
				Point classId = new Point();
		        double[] classProb = new double[1];
		        getMaxClass(prob, classId, classProb);
		        
		        predicted_class = classNames.get(classId.x());
		        
		        if(predicted_class.equals(actual_class)) {
					// if predicted_class is equal to actual_class, the image is a true positive
					true_positives.put(actual_class, true_positives.get(actual_class) + 1);	
				}
		        
		        System.out.println("Predicted class: " + classNames.get(classId.x()) + ", actual class: " +  actual_class);
			}
    	}
    	
    	for(int k = 0; k < dir.length; k++) {
			String tmp_class = c.getActualClass(dir, k);
			double tp = true_positives.get(tmp_class);
			accuracy = accuracy + tp;
		}
    	
    	accuracy = accuracy / total;
    	System.out.println("-- DEBUG -- Accuracy = " +  accuracy);
    } 
}
