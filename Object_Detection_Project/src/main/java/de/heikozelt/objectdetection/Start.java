package de.heikozelt.objectdetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class Start {
	private static Logger logger = LogManager.getLogger(Start.class);

	// Die static-Felder koennen unabhängig von der init()-methode gesetzt werden.
	// Das ist nützlich um Mocks zu injizieren.
	private static Engine engine = null;
	private static String backbone = null;
	private static Criteria<Image, DetectedObjects> criteria = null;
	private static ZooModel<Image, DetectedObjects> model = null;
	private static Predictor<Image, DetectedObjects> predictor = null;

	/**
	 * initialisiert benötigte Bild-Erkennungs-Engine
	 */
	public static void init() {
		logger.debug("DeepObjectDetection initialisation");
		engine = Engine.getInstance();
		if (engine == null) {
			logger.error("Keine Engine verfügbar!");
			return;
		}
		logger.debug("using engine " + engine.getEngineName());

		if ("TensorFlow".equals(engine.getEngineName())) {
			backbone = "mobilenet_v2";
		} else {
			backbone = "resnet50";
		}
		logger.debug("using backbone " + backbone);

		try {
			Criteria<Image, DetectedObjects> c0 = Criteria.builder().optApplication(Application.CV.OBJECT_DETECTION)
					.setTypes(Image.class, DetectedObjects.class).build();
			logger.debug("list all models -> " + ModelZoo.listModels(c0));

			criteria = Criteria.builder().optApplication(Application.CV.OBJECT_DETECTION)
					.setTypes(Image.class, DetectedObjects.class).build();
			logger.debug("listModels() -> " + ModelZoo.listModels(criteria));
			model = ModelZoo.loadModel(criteria);
			if (model == null) {
				logger.error("Kein passendes Model gefunden!");
				return;
			}

			predictor = model.newPredictor();
			if (predictor == null) {
				logger.error("Kein Predictor vorhanden!");
				return;
			}
		} catch (IOException | ModelException e) {
			e.printStackTrace();
		}
	}
	
	public static Result detect(String fileName) throws IOException, TranslateException {
    	logger.debug("reading image from file.");
    	File f = new File("collection" + File.separator + fileName);
		FileInputStream in = new FileInputStream(f);
		Image img = ImageFactory.getInstance().fromInputStream(in);
		DetectedObjects objects = predictor.predict(img);
		logger.debug("result: " + objects.getClass().getName());
		logger.debug("result.items(): " + objects.items().getClass().getName());
		Result result = new Result(fileName, img.getWidth(), img.getHeight(), objects);
		return result;
	}
	
	public static Result[] detectAll() throws IOException, TranslateException {
		String[] fileNames = new File("collection").list();
		Result[] results = new Result[fileNames.length];
		for(int i = 0; i < fileNames.length; i++) { 
		  results[i] = detect(fileNames[i]); 
		}
		return results;
	}
	
	public static void printAll(Result[] results) {
		for(Result r: results) {
		  System.out.println(r.getFilename());
		  DetectedObjects objects = r.getObjects();
		  for(int i = 0; i < objects.getNumberOfObjects(); i++) {
		    System.out.println(objects.item(i));
		  }
		}
	}
	
	public static void exportAll(Result[] results) throws IOException {
		System.out.println("export to do");
		StringBuilder str = new StringBuilder();
		str.append("<gmaf-collection xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"gmaf_schema.xsd\">");
		for(Result r: results) {
		  str.append(r.asXml());
		}
		str.append("</gmaf-collection>");
		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File("result.xml")));
		bwr.write(str.toString());
		bwr.close();
	}

	public static void main(String[] args) throws IOException, TranslateException {
		logger.info("Batch job started");
		init();
		Result[] results = detectAll();
		printAll(results);
		exportAll(results);
		logger.info("Batch job finished");
	}
}
