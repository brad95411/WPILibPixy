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
package com.zephyr.internal;

import java.nio.ByteBuffer;

/**
 * This interface acts as a middle man between the 3 communication protocols 
 * that we can use to communicate with the Pixy cam. This is done to "abstractify" 
 * the process of communicating with the Camera without to much work involved.
 * 
 * @author Bradley Bickford
 *
 */
public interface CommMiddleMan {
	
	/**
	 * Reads a byte from the communication device type. For the most part,
	 * the only thing that this is used for is re-synchronizing the 
	 * the bytes coming from the Pixy should we start pulling data out of 
	 * order
	 * 
	 * @return The byte that was read, returns null if the read fails
	 */
	public Byte readByte();
	
	/**
	 * Reads a short (2 bytes) from the communication device type. This gets
	 * used the most, as the data that comes from the Pixy is all shorts.
	 * 
	 * @return The short that was read, returns null if the read fails
	 */
	public Short readShort();
	
	/**
	 * Writes a series of bytes to the communication device type.
	 * 
	 * @param bytes The bytes to write
	 */
	public void write(byte[] bytes);
	
	/**
	 * Writes an NIO ByteBuffer object to the communication device type.
	 * This is used the most because it makes it easier to break down the 
	 * bytes in a short a little easier, the only issue being that ByteBuffers
	 * do require slightly more memory and processing overhead to work with 
	 * because they are Objects and not primitive types
	 * 
	 * @param buffer The ByteBuffer to write
	 */
	public void write(ByteBuffer buffer);

}
