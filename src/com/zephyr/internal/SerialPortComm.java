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

import edu.wpi.first.wpilibj.SerialPort;

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
public class SerialPortComm implements CommMiddleMan{

	//The SerialPort object that we'll be using to communicate with the Pixy
	private SerialPort port;
	
	/**
	 * Creates a new SerialPortComm object to work with
	 * 
	 * @param port The SerialPort to communicate over
	 */
	public SerialPortComm(SerialPort port)
	{
		this.port = port;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Byte readByte() {
		//If there is not a byte in the receive buffer return null
		if(port.getBytesReceived() < 1)
		{
			return null;
		}
		
		//Otherwise read 1 byte and return
		return port.read(1)[0];
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Short readShort() {
		//If there is not two bytes in the buffer, return null
		if(port.getBytesReceived() < 2)
		{
			return null;
		}
		
		//Setup a temp array to read data into
		byte[] data = port.read(2);
		
		//Create a temporary return value short
		short retVal = 0;
		
		//Combine the two bytes read into one short
		retVal |= data[1] << 8;
		retVal |= data[0];
		
		//Return the short
		return retVal;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void write(byte[] bytes) {
		port.write(bytes, bytes.length);
		
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void write(ByteBuffer buffer) {
		port.write(buffer.array(), buffer.array().length);
		
	}
	
	


}
