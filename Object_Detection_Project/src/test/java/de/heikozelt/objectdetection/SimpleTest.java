package de.heikozelt.objectdetection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class SimpleTest {
	@Test
	public void testCollectionNotFound() {
		Start.setCollectionPath("Diesen/Pfad/gibt/es/nicht");
		String[] args = new String[0];
		Exception e = assertThrows(Exception.class, () -> {
			Start.main(args);
		});
        assertEquals(e.getMessage(), "Collection files not found!");
	}
	
	@Test
	public void testWhiteImage() {
		// collection folder contains one complete white image
		Start.setCollectionPath("src/test/resources/collection1");
		Start.setSaveBoundingBoxImageEnabled(false);
		Start.init();
		try {
		  Result[] results = Start.detectAll();
		  assertEquals(results.length, 1, "Just one image in collection should give exactly one result!");
		  assertEquals(results[0].getObjects().getNumberOfObjects(), 0, "Should not detect anything at all in complete white image!");
		} catch(Exception e) {
			e.printStackTrace();
			fail("Should not have thrown any exception!");
		}
	}	
	
}
