package de.heikozelt.objectdetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	private static String COLLECTION_PATH = "collection";
	private static String RESULT_XML_FILENAME = "result.xml";
	private static String BOUNDING_BOXES_PATH = "boxes";
	
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

	/**
	 * Speichert eine Kopie des Bild mit eingezeichneten Bounding Boxes. Das ist
	 * sehr anschaulich und nützlich Zwecks Debugging und Evaluation.
	 * 
	 * @param img Original-Bild
	 * @param detection erkannte Objekte
	 * @param bbFileName Dateiname, unter dem die Kopie gespeichert wird
	 * @throws IOException
	 */
	public static void saveBoundingBoxImage(Image img, DetectedObjects detection, String bbFileName)
			throws IOException {
		Path imagePath = Paths.get(bbFileName);

		// Make image copy with alpha channel because original image was jpg
		Image newImage = img.duplicate(Image.Type.TYPE_INT_ARGB);
		newImage.drawBoundingBoxes(detection);

		// OpenJDK can't save jpg with alpha channel
		newImage.save(Files.newOutputStream(imagePath), "png");
		logger.info("Detected objects image has been saved in: {}", imagePath);
	}

	/**
	 * liest Bild aus Datei und startet die Objekt-Erkennung.
	 * Nebenbei wird eine Kopie mit Bounding Boxes gespeichert.
	 * @param fileName
	 * @return erkannte Objekte und weitere Infos
	 * @throws IOException
	 * @throws TranslateException
	 */
	public static Result detect(String fileName) throws IOException, TranslateException {
		String path = COLLECTION_PATH + File.separator + fileName;
		logger.debug("reading image from file: " + path);
		File f = new File(path);
		FileInputStream in = new FileInputStream(f);
		Image img = ImageFactory.getInstance().fromInputStream(in);
		DetectedObjects objects = predictor.predict(img);
		logger.debug("result: " + objects.getClass().getName());
		logger.debug("result.items(): " + objects.items().getClass().getName());
		Result result = new Result(fileName, img.getWidth(), img.getHeight(), objects);
		String bbFilename = BOUNDING_BOXES_PATH + File.separator + f.getName() + ".boxes.png";
		saveBoundingBoxImage(img, objects, bbFilename);
		return result;
	}

	/**
	 * Führt die Objekt-Ekennung für alle Bilder im "collection"-Verzeichnis durch.
	 * @return
	 * @throws IOException
	 * @throws TranslateException
	 */
	public static Result[] detectAll() throws IOException, TranslateException {
		String[] fileNames = new File(COLLECTION_PATH).list();
		Result[] results = new Result[fileNames.length];
		for (int i = 0; i < fileNames.length; i++) {
			results[i] = detect(fileNames[i]);
		}
		return results;
	}

	/**
	 * Schreibt das Ergebnis der Objekt-Erkennung ins Log. 
	 * @param results
	 */
	public static void printAll(Result[] results) {
		for (Result r : results) {
			logger.info("Dateiname: " + r.getFilename());
			DetectedObjects objects = r.getObjects();
			for (int i = 0; i < objects.getNumberOfObjects(); i++) {
				logger.info("erkannt: " + objects.item(i));
			}
		}
	}

	
	/**
	 * Speichert das Ergebnis der Objekt-Erkennung als eine "große" XML-Datei.
	 * @param results
	 * @throws IOException
	 */
	public static void exportAll(Result[] results) throws IOException {
		logger.info("exportiere Ergebnisse im XML-Format");
		StringBuilder str = new StringBuilder();
		str.append(
				"<gmaf-collection xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"gmaf_schema.xsd\">");
		for (Result r : results) {
			str.append(r.asXml());
		}
		str.append("</gmaf-collection>");
		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(RESULT_XML_FILENAME)));
		bwr.write(str.toString());
		bwr.close();
	}

	/**
	 * Hauptprogramm.
	 * In Form einer Batch-Verarbeitung werden alle Bilder gelesen, Objekte erkannt und das Ergebnis gespeichert.
	 * Danach beendet sich das Programm.
	 * @param args
	 * @throws IOException
	 * @throws TranslateException
	 */
	public static void main(String[] args) throws IOException, TranslateException {
		logger.info("Batch job started");
		init();
		Result[] results = detectAll();
		printAll(results);
		exportAll(results);
		logger.info("Batch job finished");
	}
}
