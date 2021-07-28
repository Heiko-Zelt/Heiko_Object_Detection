package de.heikozelt.objectdetection;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;

/**
 * Ein Objekt dieser Klasse beinhaltet einen Dateiname, einer Bilddatei, die
 * Bildgröße und das Ergebnis der Objekterkennung. Es können mehrere Objekte pro
 * Bild erkannt werden. Zu jedem erkannten Objekt gibt es einen Klassennamen,
 * eine Bounding Box und eine Wahrscheinlichkeit.
 * 
 * @author Heiko Zelt
 * @see ai.djl.modality.cv.output.DetectedObjects.DetectedObject
 */
public class Result {
	private static Logger logger = LogManager.getLogger(Result.class);

	/**
	 * Dateiname der Bilddatei
	 */
	private String filename;
	/**
	 * Bildbreite in Pixel
	 */
	private int imgWidth;
	/**
	 * Bildhöhe in Pixel
	 */
	private int imgHeight;
	/**
	 * Uhrzeit/Datum der Istanziierung 
	 */
	private Date date = new Date();
	/**
	 * erkannte Objekte
	 */
	private DetectedObjects objects;

	/**
	 * @return Dateiname, der Bilddatei, für welche die Objekte erkannt wurden
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return alle erkannten Objekte, jeweils mit Klassenname, Bounding Box und
	 *         Wahrscheinlichkeit.
	 * @see ai.djl.modality.cv.output.DetectedObjects
	 */
	public DetectedObjects getObjects() {
		return objects;
	}

	/**
	 * einfacher Konstruktor
	 * 
	 * @param filename
	 * @param imgWidth
	 * @param imgHeight
	 * @param objects
	 */
	public Result(String filename, int imgWidth, int imgHeight, DetectedObjects objects) {
		this.filename = filename;
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.objects = objects;
	}

	/**
	 * Serialisiert die Daten eines erkannten Objekts als XML-Zeichenkette
	 * @return Beispiel
	 *         <pre>
	 * {@code
	 * <object>
	 *   <term>cat</term>
	 *   <bounding-box>
	 *     <x>320</x>
	 *     <y>121</y>
	 *     <width>423</width>
	 *     <height>522</height>
	 *   </bounding-box>
	 *   <probability>0.94</probability>
	 * </object>
	 * }
	 * </pre>
	 */
	public String objectAsXml(DetectedObject obj) {
		logger.debug("term: " + obj.getClassName());
		BoundingBox box = obj.getBoundingBox();
		logger.debug("bounding box: " + box.toString());
		Rectangle rect = box.getBounds();
		int xPx = (int) Math.round(rect.getX() * imgWidth);
		int yPx = (int) Math.round(rect.getY() * imgHeight);
		int widthPx = (int) Math.round(rect.getWidth() * imgWidth);
		int heightPx = (int) Math.round(rect.getHeight() * imgHeight);

		StringBuilder str = new StringBuilder();
		str.append("      <object>\n");
		str.append("        <term>").append(obj.getClassName()).append("</term>\n");
		str.append("        <bounding-box>\n");
		str.append("          <x>").append(xPx).append("</x>\n");
		str.append("          <y>").append(yPx).append("</y>\n");
		str.append("          <width>").append(widthPx).append("</width>\n");
		str.append("          <height>").append(heightPx).append("</height>\n");
		str.append("        </bounding-box>\n");
		str.append("        <probability>").append(obj.getProbability()).append("</probability>\n");
		str.append("      </object>\n");
		return str.toString();
	}

	/**
	 * Serialisiert die Daten aller erkannten Objekte eines Bildes als XML-Zeichenkette.
	 * @return Beispiel
	 * 
	 *         <pre>
	 * {@code
	 * <gmaf-data>
	 *   <file>IMG_0001.png</file>
	 *   <date>14.04.2021</date>
	 *   <objects>
	 *     <object>
	 *       <term>cat</term>
	 *       <bounding-box>
	 *         <x>320</x>
	 *         <y>121</y>
	 *         <width>423</width>
	 *         <height>522</height>
	 *       </bounding-box>
	 *       <probability>0.94</probability>
	 *      </object>
	 *    </objects>
	 * </gmaf-data>
	 * }
	 * </pre>
	 */
	public String asXml() {
		StringBuilder str = new StringBuilder();
		logger.debug("file: " + filename);
		str.append("  <gmaf-data>\n");
		str.append("    <file>").append(filename).append("</file>\n");
		str.append("    <date>").append(date).append("</date>\n");
		str.append("    <objects>\n");
		for (int i = 0; i < objects.getNumberOfObjects(); i++) {
			DetectedObject obj = (DetectedObject) (objects.item(i));
			str.append(objectAsXml(obj));
		}
		str.append("    </objects>\n");
		str.append("  </gmaf-data>\n");
		return str.toString();
	}

}
