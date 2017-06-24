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
public class Emotion
{
	private double anger;
	private double contempt;
	private double disgust;
	private double fear;
	private double happiness;
	private double neutral;
	private double sadness;
	private double surprise;

	/**
	 * @param anger
	 * @param contempt
	 * @param disgust
	 * @param fear
	 * @param happiness
	 * @param neutral
	 * @param sadness
	 * @param surprise
	 */
	public Emotion( JSONObject emotionJson )
	{
		super( );
		this.anger = emotionJson.getDouble( "anger" );
		this.contempt = emotionJson.getDouble( "contempt" );
		this.disgust = emotionJson.getDouble( "disgust" );
		this.fear = emotionJson.getDouble( "fear" );
		this.happiness = emotionJson.getDouble( "happiness" );
		this.neutral = emotionJson.getDouble( "neutral" );
		this.sadness = emotionJson.getDouble( "sadness" );
		this.surprise = emotionJson.getDouble( "surprise" );
	}

	/**
	 * @return calculated happiness
	 */
	public long calculateHappiness( )
	{
		long result = 2L; // default neutral

		double positive = this.happiness + this.surprise;
		double neutral = this.neutral;
		double negative = this.anger + this.contempt + this.disgust + this.fear + this.sadness;

		if ( positive > neutral && positive > negative )
		{
			if ( positive > 66d )
			{
				result = 4L;
			}
			else
			{
				result = 3L;
			}
		}
		else if ( neutral > positive && neutral > negative )
		{
			result = 2L;
		}
		else if ( negative > positive && negative > neutral )
		{
			if ( negative > 66d )
			{
				result = 0L;
			}
			else
			{
				result = 1L;
			}
		}
		return result;
	}
}
