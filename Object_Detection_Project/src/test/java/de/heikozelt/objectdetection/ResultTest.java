package de.heikozelt.objectdetection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import ai.djl.modality.Classifications.Classification;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import ai.djl.modality.cv.output.Rectangle;

/**
 * JUni-Tests für Klasse Result
 * @author Heiko Zelt
 */
public class ResultTest {
	private static Logger logger = LogManager.getLogger(ResultTest.class);

	/**
	 * Fall: keine Objekte detektiert
	 */
	@Test
	public void testConstructorAndGetters1() {
		List<String> classNames = new ArrayList<String>();
		List<Double> probabilities = new ArrayList<Double>();
		List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
		DetectedObjects objects = new DetectedObjects(classNames, probabilities, boundingBoxes);
		Result r = new Result("img001.png", 100, 200, objects, 300);
		assertEquals("img001.png", r.getFilename());
		assertEquals(100, r.getImgWidth());
		assertEquals(200, r.getImgHeight());
		assertEquals(0, r.getObjects().getNumberOfObjects());
	}

	/**
	 * Fall: ein Objekt detektiert
	 */
	@Test
	public void testConstructorAndGetters2() {
		List<String> classNames = new ArrayList<String>();
		classNames.add("cat");
		List<Double> probabilities = new ArrayList<Double>();
		probabilities.add(0.5);
		List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
		boundingBoxes.add(new Rectangle(0.1d, 0.2d, 0.3d, 0.4d));
		DetectedObjects objects = new DetectedObjects(classNames, probabilities, boundingBoxes);
		Result r = new Result("img001.png", 100, 200, objects, 300);
		assertEquals("img001.png", r.getFilename());
		assertEquals(100, r.getImgWidth());
		assertEquals(200, r.getImgHeight());
		DetectedObjects objects2 = r.getObjects();
		assertEquals(1, objects2.getNumberOfObjects());
		Classification classi = objects2.item(0);
		assertTrue(classi instanceof DetectedObject);
		DetectedObject obj = (DetectedObject) classi;
		assertEquals("cat", obj.getClassName());
	}

	/**
	 * Testet Serialisierung eines Objektes als XML-Zeichenkette
	 */
	@Test
	public void testObjectAsXml() {
		String expectedXml = "      <object>\n" + "        <term>cat</term>\n" + "        <bounding-box>\n"
				+ "          <x>10</x>\n" + "          <y>40</y>\n" + "          <width>30</width>\n"
				+ "          <height>80</height>\n" + "        </bounding-box>\n"
				+ "        <probability>0.5</probability>\n" + "      </object>\n";
		Pattern pattern = Pattern.compile(expectedXml, Pattern.MULTILINE);
		List<String> classNames = new ArrayList<String>();
		classNames.add("cat");
		List<Double> probabilities = new ArrayList<Double>();
		probabilities.add(0.5);
		List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
		boundingBoxes.add(new Rectangle(0.1d, 0.2d, 0.3d, 0.4d));
		DetectedObjects objects = new DetectedObjects(classNames, probabilities, boundingBoxes);
		Result r = new Result("img001.png", 100, 200, objects, 300);
		String xml = r.objectAsXml(objects.item(0));
		logger.debug(xml);
		assertTrue(pattern.matcher(xml).matches());
	}

	/**
	 * Testet Serialisierung eines Bildes mit einem Objekt als XML-Zeichenkette
	 */
	@Test
	public void testAsXml() {
		String expectedXml = "  <gmaf-data>\n" + "    <file>img001.png</file>\n" + "    <date>.*</date>\n"
				+ "    <objects>\n" + "      <object>\n" + "        <term>cat</term>\n" + "        <bounding-box>\n"
				+ "          <x>10</x>\n" + "          <y>40</y>\n" + "          <width>30</width>\n"
				+ "          <height>80</height>\n" + "        </bounding-box>\n"
				+ "        <probability>0.5</probability>\n" + "      </object>\n" + "    </objects>\n"
				+ "  </gmaf-data>\n";
		Pattern pattern = Pattern.compile(expectedXml, Pattern.MULTILINE);
		List<String> classNames = new ArrayList<String>();
		classNames.add("cat");
		List<Double> probabilities = new ArrayList<Double>();
		probabilities.add(0.5);
		List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
		boundingBoxes.add(new Rectangle(0.1d, 0.2d, 0.3d, 0.4d));
		DetectedObjects objects = new DetectedObjects(classNames, probabilities, boundingBoxes);
		Result r = new Result("img001.png", 100, 200, objects, 300);
		String xml = r.asXml();
		logger.debug(xml);
		assertTrue(pattern.matcher(xml).matches());
	}
}
