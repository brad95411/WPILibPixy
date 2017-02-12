/*
 * Copyright (C) 2017 Bradley Bickford
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; 
 * either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.zephyr.pixy;

/**
 * An immutable representation of a Pixy object detection, every component transmitted is stored in one of these.
 * With the exclusion of the checksum that is used to verify the data transmitted. It's not really useful down the 
 * road, a least there's not an obvious reason why anyone would need it.
 * 
 * @author Bradley Bickford
 *
 */
public class PixyDetection {

	//The sync word used to represent the object type, either color code or not
	private short syncWord;
	
	//The signature number of this detection, this depends on the max number of detections you allow
	//your pixy to find
	private short signatureNumber;
	
	//The center X position of the object that was detected
	private short centerX;
	
	//The center Y position of the object that was detected
	private short centerY;
	
	//The object width of the object that was detected
	private short objectWidth;
	
	//The object height of the object that was detected
	private short objectHeight;
	
	//This is only used by color codes. It doesn't explicitly say in the documentation what angle it's referencing,
	//I assume its the calculated angle that the color code is being viewed at. But, without testing it's really hard to know. :p 
	private short angle;
	
	//A boolean that is set internally to indicate quickly whether or not this detection is color code based or not.
	private boolean isColorCode;
	
	/**
	 * Creates a new PixyDetection object
	 * 
	 * @param syncWord The sync word sent for this object (either normal or color code)
	 * @param signatureNumber The signature number of this detection
	 * @param centerX The center X of the object that was detected
	 * @param centerY The center Y of the object that was detected
	 * @param objectWidth The width of the object that was detected
	 * @param objectHeight The height of the object that was detected
	 * @param angle The angle that a color code object is viewed at?
	 */
	public PixyDetection(short syncWord, short signatureNumber, 
			short centerX, short centerY, short objectWidth, 
			short objectHeight, short angle)
	{
		//If the sync word is the color code sync word, then isColorCode should be true
		if(syncWord == 0xAA56)
		{
			isColorCode = true;
		}
		else //Otherwise it should be false
		{
			isColorCode = false;
		}
		
		//The remaining values are just applied to there respective values
		this.syncWord = syncWord;
		
		this.signatureNumber = signatureNumber;
		
		this.centerX = centerX;
		
		this.centerY = centerY;
		
		this.objectWidth = objectWidth;
		
		this.objectHeight = objectHeight;
		
		this.angle = angle;
	}
	
	/**
	 * Returns the sync word of this detection (either 0xAA55 for normal or 0xAA56 for color code)
	 * 
	 * @return The sync word for this detection
	 */
	public short getSyncWord()
	{
		return syncWord;
	}
	
	/**
	 * Returns whether or not this detection is a color code detection
	 * 
	 * @return True if a color code detection, false otherwise
	 */
	public boolean isColorCode()
	{
		return isColorCode;
	}
	
	/**
	 * Returns the signature number of this detection
	 * 
	 * @return The signature number of this detection
	 */
	public short getSignatureNumber()
	{
		return signatureNumber;
	}
	
	/**
	 * Returns the center X position of this detection
	 * 
	 * @return The center X position of this detection
	 */
	public short getCenterX()
	{
		return centerX;
	}
	
	/**
	 * Returns the center Y position of this detection
	 * 
	 * @return The center Y position of this detection
	 */
	public short getCenterY()
	{
		return centerY;
	}
	
	/**
	 * Returns the object width of this detection
	 * 
	 * @return The object width of this detection
	 */
	public short getObjectWidth()
	{
		return objectWidth;
	}
	
	/**
	 * Returns the object height of this detection
	 * 
	 * @return The object height of this detection
	 */
	public short getObjectHeight()
	{
		return objectHeight;
	}
	
	/**
	 * Returns the angle of the detection if it is a color code?
	 * 
	 * @return The angle of the detection if it is a color code, or zero if it's not a color code
	 */
	public short getAngle()
	{
		return angle;
	}
	
	/**
	 * Returns the signature number, the center X and Y position, and the object width and height as a String
	 * 
	 * @Return signature number, center X and Y position, and width and height as a String
	 */
	public String toString()
	{
		return "Sig#" + signatureNumber + " cX:" + centerX + 
				" cY:" + centerY + " w:" + objectWidth + " h:" + 
				objectHeight;			
	}
}
