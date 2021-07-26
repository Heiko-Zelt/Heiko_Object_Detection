package de.heikozelt.objectdetection;

import ai.djl.modality.cv.output.DetectedObjects;

public class Result {
	private String filename;
	private DetectedObjects objects;
	
	public String getFilename() {
		return filename;
	}

	public DetectedObjects getObjects() {
		return objects;
	}

	public Result(String filename, DetectedObjects objects) {
		this.filename = filename;
		this.objects = objects;
	}

}
