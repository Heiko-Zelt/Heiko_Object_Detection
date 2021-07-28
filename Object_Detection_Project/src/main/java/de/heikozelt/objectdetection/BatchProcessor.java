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

/**
 * Liest Bild-Dateien aus einem Verzeichnis, erkennt Objekte und speichert das
 * Ergbnis als XML-Datei. Zwecks Objekt-Erkennung wird die Deep Java Library
 * verwendet.
 * 
 * @author Heiko Zelt
 */
public class BatchProcessor {
	private static String collectionPath = "collection";
	private static String resultXmlFilename = "result.xml";
	private static String boundingBoxesPath = "boxes";
	private static Boolean isSaveBoundingBoxImageEnabled = true;

	/**
	 * Es können Kopien der Bilder mit eingezeichneten Bounding Boxes abgespeichert
	 * werden. Mit dieser Methode kann diese Funktionalität ein- oder ausgeschaltet
	 * werden.
	 * 
	 * @param enabled
	 */
	public static void setSaveBoundingBoxImageEnabled(Boolean enabled) {
		BatchProcessor.isSaveBoundingBoxImageEnabled = enabled;
	}

	private static Logger logger = LogManager.getLogger(BatchProcessor.class);

	// Die static-Felder koennen unabhängig von der init()-methode gesetzt werden.
	// Das ist nützlich um Mocks zu injizieren.
	private static Engine engine = null;
	private static String backbone = null;
	private static Criteria<Image, DetectedObjects> criteria = null;
	private static ZooModel<Image, DetectedObjects> model = null;
	private static Predictor<Image, DetectedObjects> predictor = null;

	/**
	 * Mit dieser Methode kann ein vom Standartwert abweichender Pfad für das
	 * Verzeichnis mit den Bildern gesetzt werden. (z.B. Zwecks Unit-Test mit
	 * Test-Bildern.)
	 * 
	 * @param path Beispiel: "collection" oder "/home/heiko/Pictures"
	 */
	public static void setCollectionPath(String path) {
		BatchProcessor.collectionPath = path;
	}

	/**
	 * Mit dieser Methode kann ein vom Standartwert abweichender Dateiname für die
	 * Ergebnisdatei gesetzt werden.
	 * 
	 * @param resultXmlFilename Beispiel: "result.xml" oder "/tmp/export.xml"
	 */
	public static void setResultXmlFilename(String resultXmlFilename) {
		BatchProcessor.resultXmlFilename = resultXmlFilename;
	}

	/**
	 * Mit dieser Methode kann ein vom Standartwert abweichender Pfad für das
	 * Verzeichnis mit den Bild-Kopien mit eingezeichneten Bounding Boxes gesetzt
	 * werden.
	 * 
	 * @param boundingBoxesPath Beispiel: "boxes" oder "/tmp/bounding_boxes"
	 */
	public static void setBoundingBoxesPath(String boundingBoxesPath) {
		BatchProcessor.boundingBoxesPath = boundingBoxesPath;
	}

	/**
	 * initialisiert benötigte Bild-Erkennungs-Engine und Modell
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
	 * @param img        Original-Bild @see ai.djl.modality.cv.Image
	 * @param detection  erkannte Objekte @see
	 *                   ai.djl.modality.cv.output.DetectedObjects
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
	 * Liest ein Bild aus einer Datei und startet die Objekt-Erkennung. Nebenbei
	 * wird eine Kopie mit Bounding Boxes gespeichert.
	 * 
	 * @param fileName
	 * @return erkannte Objekte und weitere Infos
	 * @throws IOException
	 * @throws TranslateException
	 */
	public static Result detect(String fileName) throws IOException, TranslateException {
		String path = collectionPath + File.separator + fileName;
		logger.debug("reading image from file: " + path);
		File f = new File(path);
		FileInputStream in = new FileInputStream(f);
		Image img = ImageFactory.getInstance().fromInputStream(in);
		DetectedObjects objects = predictor.predict(img);
		logger.debug("result: " + objects.getClass().getName());
		logger.debug("result.items(): " + objects.items().getClass().getName());
		Result result = new Result(fileName, img.getWidth(), img.getHeight(), objects);
		String bbFilename = boundingBoxesPath + File.separator + f.getName() + ".boxes.png";
		if (isSaveBoundingBoxImageEnabled) {
			saveBoundingBoxImage(img, objects, bbFilename);
		}
		return result;
	}

	/**
	 * Führt die Objekt-Ekennung für alle Bilder im "collection"-Verzeichnis durch.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Result[] detectAll() throws Exception {
		String[] fileNames = new File(collectionPath).list();
		if (fileNames == null) {
			throw new Exception("Collection files not found!");
		}
		Result[] results = new Result[fileNames.length];
		for (int i = 0; i < fileNames.length; i++) {
			results[i] = detect(fileNames[i]);
		}
		return results;
	}

	/**
	 * Schreibt das Ergebnis der Objekt-Erkennung ins Log.
	 * 
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
	 * Speichert/Serialisiert das Ergebnis der Objekt-Erkennung als eine "große" XML-Datei.
	 * 
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
		BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(resultXmlFilename)));
		bwr.write(str.toString());
		bwr.close();
	}

	/**
	 * Hauptprogramm. In Form einer Batch-Verarbeitung werden alle Bilder in einem
	 * Verzeichnis gelesen, Objekte auf den Bildern erkannt und das Ergebnis als
	 * XML-Datei gespeichert. Danach beendet sich das Programm. Die Ausgabe erfolgt
	 * als besagte XML-Datei. Der Fortschritt der Batch-Verarbeitung und ggf.
	 * Fehlermeldungen werden via Log4J protokolliert.
	 * 
	 * @param args Es werden keine Programm-spezifischen Kommandozeilen-Parameter
	 *             erwartet/ausgewertet.
	 */
	public static void main(String[] args) {
		try {
			logger.info("Batch job started");
			init();
			Result[] results = detectAll();
			printAll(results);
			exportAll(results);
			logger.info("Batch job finished");
		} catch (Exception e) {
			logger.fatal(e);
		}
	}

}
