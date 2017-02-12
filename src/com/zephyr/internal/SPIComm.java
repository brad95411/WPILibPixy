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

import edu.wpi.first.wpilibj.SPI;

/**
 * Not ready for prime time yet, the Pixy does
 * weird stuff to deal with the simultaneous send/receive
 * that SPI is capable of. You're welcome to try SPI, 
 * the FIFO buffers on the SPI bus for the RIO may be 
 * able to compensate, but don't count on it. It may 
 * just come down to making a perfect replication 
 * of how the Arduino library handles things, buffering 
 * manually and doing simultaneous send/receive under my
 * own power.
 * 
 * This class acts as a bridge between the SPI communication profile
 * and the rest of the code. It implements the CommMiddleMan interface to 
 * make it so that it can be used in conjunction with the other two communication
 * styles dynamically without having to have code specific to each communication 
 * type 
 * 
 * @author Bradley Bickford
 *
 */
public class SPIComm implements CommMiddleMan{

	//The SPI object that we'll be using to communicate with the Pixy
	private SPI spi;
	
	/**
	 * Creates a new SPIComm object to work with
	 * 
	 * @param spi The SPI port to communicate over
	 */
	public SPIComm(SPI spi)
	{
		this.spi = spi;
		
		//SPI requires a lot more setup that the other interfaces, but 
		//it's significantly faster as a result. Achieving speeds fast enough 
		//that it code capture every single update that comes from the Pixy
		
		//Set the clock rate of the SPI bus (the default is 1MHz)
		//This cannot currently be changed in code
		//TODO Make this changeable
		this.spi.setClockRate(1000000);
		
		//Sets the SPI clock to be active High, meaning the high pulse of the 
		//clock denotes when the clock is beginning its next cycle
		this.spi.setClockActiveHigh();
		
		//Sets the most significant bit as what comes out or goes into the SPI
		//bus first. This is different from the other two communication types, which 
		//are little endian, SPI is big endian
		this.spi.setMSBFirst();
		
		//This makes it so when the clock is rising that is when the SPI bus
		//reads the data line to figure out what the next bit is
		this.spi.setSampleDataOnRising();
		
		//This makes it so that when the chip select line is pulled low that 
		//means that the Pixy should listen to whatever it is that we have to say to
		//it
		this.spi.setChipSelectActiveLow();
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Byte readByte() {
		byte[] data = new byte[1];
		
		//Read one byte of data from the SPI buffer and
		//return it, this will prompt for data and should (emphasis on 
		//should) wait for the one byte to appear
		spi.read(true, data, 1);
		
		return data[0];
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Short readShort() {
		byte[] data = new byte[2];
		
		//Reads two bytes of data from the SPI buffer and
		//returns it as a short, this will prompt for data and should (emphasis on 
		//should) wait for the one byte to appear
		spi.read(true, data, 2);
		
		short retVal = 0;
		
		//SPI is Big Endian not Little Endian
		retVal |= data[0] << 8;
		retVal |= data[1];
		
		return retVal;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void write(byte[] bytes) {
		spi.write(bytes, bytes.length);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void write(ByteBuffer buffer) {
		spi.write(buffer, buffer.capacity());
	}

}
