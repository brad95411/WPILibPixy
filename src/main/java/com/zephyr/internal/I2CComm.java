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

import edu.wpi.first.wpilibj.I2C;

/**
 * This class acts as a bridge between the SerialPort communication profile
 * and the rest of the code. It implements the CommMiddleMan interface to 
 * make it so that it can be used in conjunction with the other two communication
 * styles dynamically without having to have code specific to each communication 
 * type
 * 
 * @author Bradley Bickford
 *
 */
public class I2CComm implements CommMiddleMan{

	//The I2C object that we'll be using to communicate with the Pixy
	private I2C i2c;
	
	/**
	 * Creates a new I2CComm object to work with
	 * 
	 * @param i2c The I2C port to work with
	 */
	public I2CComm(I2C i2c)
	{
		this.i2c = i2c;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Byte readByte() {
		byte[] data = new byte[1];
		
		//Read 1 byte from the I2C bus and return it
		i2c.readOnly(data, 1);
		
		return data[0];
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Short readShort() {
		byte[] data = new byte[2];
		
		//Read two bytes from the I2C buffer and convert it to a short
		i2c.readOnly(data, 2);
		
		short retVal = 0;
		
		retVal |= data[1] << 8;
		retVal |= data[0];
		
		return retVal;
		
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void write(byte[] bytes) {
		i2c.writeBulk(bytes);
		
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void write(ByteBuffer buffer) {
		i2c.writeBulk(buffer, buffer.capacity());
		
	}

}
