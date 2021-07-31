package de.heikozelt.objectdetection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;

/**
 * Tests für die Klasse BatchJob ohne Mocks
 * @author Heiko Zelt
 */
public class BatchJob_SimpleTest {
	private static Logger logger = LogManager.getLogger(BatchJob_SimpleTest.class);
	
	@Test
	public void testDetectAll_CollectionNotFound() {
		BatchJob.setCollectionPath("Diesen/Pfad/gibt/es/nicht");
		BatchJob.init();
		Exception e = assertThrows(Exception.class, () -> {
			BatchJob.detectAll();
		});
		assertEquals(e.getMessage(), "Collection files not found!");
	}

	@Test
	public void testDetectAll_WhiteImage() {
		// collection folder contains one complete white image
		BatchJob.setCollectionPath("src/test/resources/collection1");
		BatchJob.setSaveBoundingBoxImageEnabled(false);
		BatchJob.init();
		try {
			Result[] results = BatchJob.detectAll();
			assertEquals(results.length, 1, "Just one image in collection should give exactly one result!");
			assertEquals(results[0].getObjects().getNumberOfObjects(), 0,
					"Should not detect anything at all in complete white image!");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Should not have thrown any exception!");
		}
	}

	/**
	 * Testet Serialisierung eines Bildes mit einem Objekt als XML-Zeichenkette
	 */
	@Test
	public void testAsXml() {
		String expectedXml = "<gmaf-collection xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"gmaf_schema.xsd\">\n"
				+ "  <gmaf-data>\n" + "    <file>img001.png</file>\n" + "    <date>.*</date>\n" + "    <objects>\n"
				+ "      <object>\n" + "        <term>cat</term>\n" + "        <bounding-box>\n"
				+ "          <x>10</x>\n" + "          <y>40</y>\n" + "          <width>30</width>\n"
				+ "          <height>80</height>\n" + "        </bounding-box>\n"
				+ "        <probability>0.5</probability>\n" + "      </object>\n" + "    </objects>\n"
				+ "  </gmaf-data>\n" + "</gmaf-collection>\n";
		Pattern pattern = Pattern.compile(expectedXml, Pattern.MULTILINE);
		List<String> classNames = new ArrayList<String>();
		classNames.add("cat");
		List<Double> probabilities = new ArrayList<Double>();
		probabilities.add(0.5);
		List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
		boundingBoxes.add(new Rectangle(0.1d, 0.2d, 0.3d, 0.4d));
		DetectedObjects objects = new DetectedObjects(classNames, probabilities, boundingBoxes);
		Result r = new Result("img001.png", 100, 200, objects, 300);
		Result[] results = { r };  
		String xml = BatchJob.resultsAsXml(results);
		logger.debug(xml);
		assertTrue(pattern.matcher(xml).matches());
	}

}
