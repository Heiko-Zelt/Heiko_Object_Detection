package de.heikozelt.objectdetection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.translate.TranslateException;

/**
 * Tests für die Klasse BatchJob mit Hilfe von Mockito
 * @author Heiko Zelt
 */
@ExtendWith(MockitoExtension.class)
public class BatchJob_MockTest {
	private static Logger logger = LogManager.getLogger(BatchJob_MockTest.class);
	
	@Mock
	Predictor<Image, DetectedObjects> predictorMock;
	
	/**
	 * Fall: Auf einem weissen Bild wird eine Katze erkannt.
	 * Das ist möglich, da der "predictor" "gemockt" wird und der Mock immer eine Katze erkennt.
	 * Der Test läuft 10 Mal so schnell durch, da keine echte Objekt-Erkennung durchgeführt wird.
	 */
	@Test
	public void testDetectAll_WhiteImage() {
		assertNotNull(predictorMock);
		List<String> classNames = new ArrayList<String>();
		classNames.add("cat");
		List<Double> probabilities = new ArrayList<Double>();
		probabilities.add(0.5);
		List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
		boundingBoxes.add(new Rectangle(0.1d, 0.2d, 0.3d, 0.4d));
		DetectedObjects objects = new DetectedObjects(classNames, probabilities, boundingBoxes);

		try {
			when(predictorMock.predict(any(Image.class))).thenReturn(objects);
		} catch(TranslateException e) {
			logger.fatal(e);
	        fail("predict should not have thrown any exception");
		}
		
		// collection folder contains one complete white image
		BatchJob.setCollectionPath("src/test/resources/collection1");
		BatchJob.setSaveBoundingBoxImageEnabled(false);
		BatchJob.setPredictor(predictorMock);
		
		try {
			Result[] results = BatchJob.detectAll();
			assertEquals(1, results.length, "Just one image in collection should give exactly one result!");
			assertEquals(1, results[0].getObjects().getNumberOfObjects());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Should not have thrown any exception!");
		}
	}

}
