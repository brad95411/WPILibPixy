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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.zephyr.internal.CommMiddleMan;
import com.zephyr.internal.I2CComm;
import com.zephyr.internal.SPIComm;
import com.zephyr.internal.SerialPortComm;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SerialPort;

/**
 * This is the main class that manages it all. It allows you to create a reference to a Pixy using 3 of 
 * the possible communication types available on the Pixy and retrieve the object detections from the Pixy.
 * 
 * @author Bradley Bickford
 *
 */
public class Pixy {
	
	/**
	 * The default I2C address of the Pixy
	 */
	public static final int DEFAULT_I2C_ADDR = 0x54;
	
	/**
	 * The default UART baud rate for the Pixy
	 */
	public static final int DEFAULT_UART_BAUD = 19200;
	
	/**
	 * The default maximum number of objects that can be detected by the Pixy at one time
	 */
	public static final int DEFAULT_MAX_OBJECTS = 1000;
	
	/**
	 * The value of a normal detection sync word
	 */
	public static final short NORMAL_SYNC_WORD = -21931;
	
	/**
	 * The value of a color code detection sync word
	 */
	public static final short CC_SYNC_WORD = -21930;
	
	/**
	 * The value of a sync word that is out of sync, we've read too many bytes or missed a byte somewhere
	 * and this can be used to get us back in sync when looking for the values we need.
	 */
	public static final short OUT_OF_SYNC_WORD = -10838;

	//The middle man between the communication protocol we're using and the rest of the code
	private CommMiddleMan comms;
	
	//The array of PixyDetection objects that represent our current detections
	private PixyDetection[] detections;
	
	//The maximum number of objects that can be detected by the Pixy
	private int maxAllowedObjects;
	
	/**
	 * Create a new Pixy object with a specific I2C port and a maximum number of allowable objects.
	 * The default I2C address for the Pixy is used with this constructor
	 * 
	 * @param port The I2C port to communicate on
	 * @param maxAllowedObjects The maximum number of objects
	 */
	public Pixy(I2C.Port port, int maxAllowedObjects)
	{
		this(port, DEFAULT_I2C_ADDR, maxAllowedObjects);
	}
	
	/**
	 * Create a new Pixy object with a specific I2C port, an I2C address, and a maximum number of allowable objects
	 * 
	 * @param port The I2C port to communicate on
	 * @param address The I2C address to talk to
	 * @param maxAllowedObjects The maximum number of objects
	 */
	public Pixy(I2C.Port port, int address, int maxAllowedObjects)
	{
		comms = new I2CComm(new I2C(port, address));
		
		genericPixyInit(maxAllowedObjects);
	}
	
	/**
	 * Create a new Pixy object with a specific SerialPort port and a maximum number of allowable objects
	 * The default SerialPort buad rate for the Pixy is used with this constructor
	 * 
	 * @param port The SerialPort port to communicate on
	 * @param maxAllowedObjects The maximum number of objects
	 */
	public Pixy(SerialPort.Port port, int maxAllowedObjects)
	{
		this(port, DEFAULT_UART_BAUD, maxAllowedObjects);
	}
	
	/**
	 * Create a new Pixy object with a specific SerialPort port, a baud rate, and a maximum number of allowable objects
	 * 
	 * @param port The SerialPort port to communicate on
	 * @param baudRate The baud rate to use for this connections
	 * @param maxAllowedObjects The maximum number of objects
	 */
	public Pixy(SerialPort.Port port, int baudRate, int maxAllowedObjects)
	{
		comms = new SerialPortComm(new SerialPort(baudRate, port));
		
		genericPixyInit(maxAllowedObjects);
	}
	
	/**
	 * Create a new Pixy object with a specific SPI port and a maximum number of allowable objects
	 * 
	 * @param port The SPI port to communicate on
	 * @param maxAllowedObjects The maximum number of objects
	 */
	public Pixy(SPI.Port port, int maxAllowedObjects)
	{
		comms = new SPIComm(new SPI(port));
		
		genericPixyInit(maxAllowedObjects);
	}
	
	/**
	 * Updates the entire set of detection objects and returns the one that you specifically want. 
	 * This method uses array indexing notation, so detection numbers start from 0, and range to maxAllowedObjects - 1
	 * 
	 * @param detectionNum The number of the detection that you want to retrieve
	 * @return Returns the detection that is associated with the detectionNum array position, or null if that position doesn't exist or isn't filled in
	 */
	public PixyDetection getDetectedObject(int detectionNum)
	{
		//If out of range return null
		if(detectionNum < 0 || detectionNum > maxAllowedObjects - 1)
		{
			return null;
		}
		
		//Update the Pixy object detections
		updatePixyDetections();
		
		//Return the detection the user actually wanted
		return detections[detectionNum];
	}
	
	/**
	 * Updates the entire set of detection objects and returns the entire array of objects
	 * 
	 * @return The entire array of PixyDetection objects 
	 */
	public PixyDetection[] getAllDetectedObjects()
	{
		//Update the Pixy object detections
		updatePixyDetections();
		
		//Return the array of detections
		return detections;
	}
	
	/**
	 * Sends information to the Pixy to adjust the pan and tilt of the Pixy using the servo mount attachment
	 * 
	 * TODO Verify this works with SPI specifically, while it needs to be verified with all communication types, SPI might have to ordered Big Endian not little Endian
	 * 
	 * @param pan The pan of the Pixy camera, ranging from 0 to 1000
	 * @param tilt The tilt of the Pixy camera, ranging from 0 to 1000
	 */
	public void setPanTilt(Short pan, Short tilt)
	{
		//Verify data ranges, otherwise return
		if(pan > 1000 || pan < 0 || tilt > 1000 || tilt < 0)
		{
			return;
		}
		
		//Create a ByteBuffer and order it little endian
		ByteBuffer buffer = ByteBuffer.allocateDirect(6);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		//Put the sync word into the buffer that indicates that this data is for the servos
		buffer.put((byte) 0x00);
		buffer.put((byte) 0xFF);
		
		//Put the pan and tilt shorts into the buffer
		buffer.putShort(pan);
		buffer.putShort(tilt);
		
		//Write to the communication hardware
		comms.write(buffer);
	}
	
	/**
	 * Sends information to the Pixy to adjust the brightness (exposure) of the camera itself, note that the Short value is converted to 
	 * an 8 bit value ranging from 0-255. This is to circumvent the fact that Java has no nice way of specifying a parameter as unsigned
	 * 
	 * TODO Verify this works with SPI specifically, while it needs to be verified with all communication types, SPI might have to be ordered Big Endian not little Endian
	 * 
	 * @param brightness The new brightness
	 */
	public void setBrightness(Short brightness)
	{
		//Verify data range, otherwise return
		if(brightness > 255 || brightness < 0)
		{
			return;
		}
		
		//Create a ByteBuffer to store stuff in
		ByteBuffer buffer = ByteBuffer.allocateDirect(3);
		
		//Put the sync word into the buffer that indicates that this data is for the brightness of the camera (exposure)
		buffer.put((byte) 0x00);
		buffer.put((byte) 0xFE);
		
		//Convert our Short value to a byte that we can send to the Pixy
		byte byteBrightness = (byte) (brightness & 0xFF);
		
		//Put the byte version of our brightness into the buffer
		buffer.put(byteBrightness);
		
		//Write to the communication hardware
		comms.write(buffer);
	}
	
	/**
	 * Sends information to the Pixy to adjust the color of the LED. Note that the Short values are converted to 
	 * and 8 bit value ranging from 0-255. This is to circumvent the fact that Java has no nice way of specifiying a parameter as unsigned
	 * 
	 * TODO Verify this works with SPI specifically, while it needs to be verified with all communication types, SPI might have to be ordered Big Endian not little Endian
	 * 
	 * @param red The red component for the LED
	 * @param green The green component for the LED
	 * @param blue The blue component for the LED
	 */
	public void setLED(Short red, Short green, Short blue)
	{
		//Verify data range, otherwise return
		if(red > 255 || red < 0 || green > 255 || green < 0 || blue > 255 || blue < 0)
		{
			return;
		}
		
		//Create a ByteBuffer to store stuff in
		ByteBuffer buffer = ByteBuffer.allocateDirect(5);
		
		//Put the sync word into the buffer that indicates that this data is for the LED on the camera
		buffer.put((byte) 0x00);
		buffer.put((byte) 0xFD);
		
		//Create a byte to store the converted shorts in, first convert red and add it to the buffer
		byte tempComponent = (byte) (red & 0xFF);
		buffer.put(tempComponent);
		
		//Then convert green and add it to the buffer
		tempComponent = (byte) (green & 0xFF);
		buffer.put(tempComponent);
		
		//Then convert blue and add it to the buffer
		tempComponent = (byte) (blue & 0xFF);
		buffer.put(tempComponent);
		
		//Write to the communication hardware
		comms.write(buffer);
	}
	
	/**
	 * Updates all of the current Pixy object detections, this is where most of the grunt work is done. I'm a little confused by the documentation 
	 * at this point as to whether or not the Pixy sends generic empty object detections up to the maximum number of detections you specify in PixyMon. 
	 * While it shouldn't affect how the code works, it will just create a bunch of junk that doesn't line up with the checksum, I'd prefer the RIO not have 
	 * to be doing any extra work that it doesn't have to be doing.
	 * 
	 * TODO Test to see if the Pixy chokes out empty objects up to the number of objects you specify that it can detect
	 */
	private void updatePixyDetections()
	{
		//Create two shorts to work with, the first thing we need to look for is the frame sync, which is either two 0xAA55's or one 0xAA55
		//and one 0xAA56
		Short currentWord = 0;
		Short lastWord = -1;
		
		//The current block type of the object that we are dealing with
		Short blockType = null;
		
		//Whether or not we've already detected a block type, this boolean is used to indicate that the frame beginning search has been performed
		//and we already know what the first detected object is for a object type (either normal or a color code)
		boolean preBlockDetect = true;
		
		//While true makes me nervous...
		//TODO Apply a timer to this that returns if it takes to long to find the start frame in the buffer, maybe even throw a proper Exception
		while(true)
		{
			//Read a word
			currentWord = comms.readShort();
			
			//If both this word and the last were 0, something's wrong, and wasting time reading an empty buffer isn't going to do any good
			//so return
			if(currentWord == 0 && lastWord == 0)
			{
				return;
			}
			else if(currentWord == NORMAL_SYNC_WORD && lastWord == NORMAL_SYNC_WORD) //Otherwise if two sync words (0xAA55)
			{
				//Then set the appropriate block type and break
				blockType = NORMAL_SYNC_WORD;
				break;
			}
			else if(currentWord == CC_SYNC_WORD && lastWord == NORMAL_SYNC_WORD) //Otherwise if two different sync words (0xAA55 (start frame) and 0xAA56 (color code block)
			{
				//Then set the appropriate block type and break
				blockType = CC_SYNC_WORD;
				break;
			}
			else if(currentWord == OUT_OF_SYNC_WORD) //Otherwise if the current word is out of sync (0x55AA)
			{
				//Notify through the RIOlog that we're out of sync and read a byte to try to get us back in sync
				//Otherwise we'll never find the right sync words, we'll always be one byte off
				System.out.println("PIXY DATA OUT OF SYNC!");
				comms.readByte();
			}
			
			//Set the current word to the last word and repeat
			lastWord = currentWord;
		}
		
		//For the maximum number of allowed object detections
		for(int i = 0; i < maxAllowedObjects; i++)
		{
			//If we just did the while loop above, we don't need to try to look up a block type, we already know it.
			if(preBlockDetect)
			{
				preBlockDetect = false;
			}
			else //Otherwise if this is an object other than the first one
			{
				//Grab a short from the communication hardware
				Short tempWord = comms.readShort();
				
				//And switch
				switch(tempWord)
				{
					//If it's either of our two sync words, just apply that sync word to our block type
					case NORMAL_SYNC_WORD:
					case CC_SYNC_WORD:
						blockType = tempWord;
						break;
					default: //Otherwise notify that the data received doesn't make any sense and return
						System.out.println("SYNC WORD NOT FOUND WHEN PROCESSING!");
						return;
				}
			}
			
			//Grab all the other values that the Pixy sends
			Short checksum = comms.readShort();
			
			Short signatureNumber = comms.readShort();
			
			Short xCenter = comms.readShort();
			
			Short yCenter = comms.readShort();
			
			Short objectWidth = comms.readShort();
			
			Short objectHeight = comms.readShort();
			
			//Remember that angle is only for color codes. If we're using a color code block, then read another short, otherwise the value is just 0
			Short angle = blockType == CC_SYNC_WORD ? comms.readShort() : 0;
			
			//My kingdom for uint16_t in Java
			Short sum = (short) ((short) signatureNumber + (short) xCenter + 
					(short) yCenter + (short) objectWidth + (short) objectHeight + 
					(short) angle);
			
			//Validate the checksum, if it's ok, then setup that value with a new PixyDetection object
			if(checksum == sum)
			{
				detections[i] = new PixyDetection(blockType, 
					signatureNumber, xCenter, yCenter, objectWidth, objectHeight, angle);
			}
			else //Otherwise display a warning in the RIOlog
			{
				System.out.println("CHECKSUM FAULT SIG#" + signatureNumber);
			}
		}
	}
	
	/**
	 * Used by the constructors to commonize the process of creating the array that stores
	 * the PixyDetection objects
	 * 
	 * @param maxAllowedObjects The maximum number of objects that you specified the Pixy can detect in PixyMon
	 */
	private void genericPixyInit(int maxAllowedObjects)
	{
		//Set the max number of allowed objects value
		this.maxAllowedObjects = maxAllowedObjects;
		
		//Create a new array of PixyDetection objects that is the maximum number of allowed objects
		detections = new PixyDetection[maxAllowedObjects];
		
		//Set every value in the array to null initially
		for(int i = 0; i < detections.length; i++)
		{
			detections[i] = null;
		}
	}
	
}
