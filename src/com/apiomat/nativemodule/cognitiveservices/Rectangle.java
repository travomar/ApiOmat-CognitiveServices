/* Copyright (c) 2011 - 2017 All Rights Reserved, http://www.apiomat.com/
 *
 * This source is property of apiomat.com. You are not allowed to use or distribute this code without a contract
 * explicitly giving you these permissions. Usage of this code includes but is not limited to running it on a server or
 * copying parts from it.
 *
 * Apinauten GmbH, Hainstrasse 4, 04109 Leipzig, Germany
 *
 * Jun 24, 2017
 * Philipp Gille */
package com.apiomat.nativemodule.cognitiveservices;

import org.json.JSONObject;

/**
 * @author Philipp Gillé
 */
public class Rectangle
{
	private long top;
	private long width;
	private long left;
	private long height;

	/**
	 * @param top
	 * @param width
	 * @param left
	 * @param height
	 */
	public Rectangle( JSONObject emotionFaceRectangleJson )
	{
		super( );
		this.top = emotionFaceRectangleJson.getLong( "top" );
		this.width = emotionFaceRectangleJson.getLong( "width" );
		this.left = emotionFaceRectangleJson.getLong( "left" );
		this.height = emotionFaceRectangleJson.getLong( "height" );
	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode() */
	@Override
	public int hashCode( )
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( int ) ( this.height ^ ( this.height >>> 32 ) );
		result = prime * result + ( int ) ( this.left ^ ( this.left >>> 32 ) );
		result = prime * result + ( int ) ( this.top ^ ( this.top >>> 32 ) );
		result = prime * result + ( int ) ( this.width ^ ( this.width >>> 32 ) );
		return result;
	}

	/* (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals( Object obj )
	{
		if ( this == obj )
		{
			return true;
		}
		if ( obj == null )
		{
			return false;
		}
		if ( getClass( ) != obj.getClass( ) )
		{
			return false;
		}
		Rectangle other = ( Rectangle ) obj;
		if ( this.height != other.height )
		{
			return false;
		}
		if ( this.left != other.left )
		{
			return false;
		}
		if ( this.top != other.top )
		{
			return false;
		}
		if ( this.width != other.width )
		{
			return false;
		}
		return true;
	}

	/**
	 * @return the top
	 */
	public long getTop( )
	{
		return this.top;
	}

	/**
	 * @return the width
	 */
	public long getWidth( )
	{
		return this.width;
	}

	/**
	 * @return the left
	 */
	public long getLeft( )
	{
		return this.left;
	}

	/**
	 * @return the height
	 */
	public long getHeight( )
	{
		return this.height;
	}

}
