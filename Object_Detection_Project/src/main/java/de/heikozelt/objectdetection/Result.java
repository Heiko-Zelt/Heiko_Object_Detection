package de.heikozelt.objectdetection;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;

public class Result {
	private static Logger logger = LogManager.getLogger(Result.class);
	
	private String filename;
	private int imgWidth;
	private int imgHeight;
	private Date date = new Date();
	private DetectedObjects objects;

	public String getFilename() {
		return filename;
	}

	public DetectedObjects getObjects() {
		return objects;
	}

	public Result(String filename, int imgWidth, int imgHeight, DetectedObjects objects) {
		this.filename = filename;
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.objects = objects;
	}

	/**
	 * 
	 * @return
	 * 
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
		BoundingBox box = obj.getBoundingBox();
		logger.info("BB: " + box.toString());
		Rectangle rect = box.getBounds();
		logger.info("rect: " + rect);
		int xPx = (int)Math.round(rect.getX() * imgWidth);
		int yPx = (int)Math.round(rect.getY() * imgHeight);
		int widthPx = (int)Math.round(rect.getWidth() * imgWidth);
		int heightPx = (int)Math.round(rect.getHeight() * imgHeight);		
		
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
	 * 
	 * @return
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
		str.append("  <gmaf-data>\n");
		str.append("    <file>").append(filename).append("</file>\n");
		str.append("    <date>").append(date).append("</date>\n");
		str.append("    <objects>\n");
		for(int i = 0; i < objects.getNumberOfObjects(); i++) {
			DetectedObject obj = (DetectedObject)(objects.item(i));
			str.append(objectAsXml(obj));
		}
		str.append("    </objects>\n");
		str.append("  </gmaf-data>\n");
		return str.toString();
	}

}
