/* Copyright (c) 2011 - 2017, Apinauten GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. */
package com.apiomat.nativemodule.cognitiveservices;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.apiomat.nativemodule.AbstractClientDataModel;
import com.apiomat.nativemodule.IModelHooksTransient;
import com.apiomat.nativemodule.Request;

/**
 * Generated class for hooks on your Picture data model
 */

public class PictureHooksTransient<T extends Picture> implements IModelHooksTransient<Picture>
{
	protected Picture model;

	@Override
	public void setCallingModel( Picture model )
	{
		this.model = model;
	}

	/* do-Methods can be used if your data model is set to TRANSIENT */

	@Override
	public String doPost( Picture obj, Request r )
	{
		return String.valueOf( System.currentTimeMillis( ) );
	}

	@Override
	public void doPut( Picture obj, Request r )
	{
		String appName = r.getApplicationName( );
		CognitiveServices.AOM.log( appName, "foreign ID: " + obj.getForeignId( ), false );

		/* Take picture URL from PUT object */
		String picUrl = "http://poc.apiomat.enterprises" + obj.getContentURL( ) + ".img?apiKey=" + r.getApiKey( );
		CognitiveServices.AOM.log( appName, "pic url: " + picUrl, false );

		/* Read module configuration */

		String subKeyEmotion =
			( String ) CognitiveServices.APP_CONFIG_PROXY.getConfigValue( CognitiveServices.SUBSCRIPTION_KEY_EMOTION,
				appName,
				r.getSystem( ) );
		if ( subKeyEmotion == null || "".equals( subKeyEmotion ) )
		{
			CognitiveServices.AOM.throwException( appName,
				"No emotion subscription key found in the module configuration" );
		}

		String subKeyFace =
			( String ) CognitiveServices.APP_CONFIG_PROXY.getConfigValue( CognitiveServices.SUBSCRIPTION_KEY_EMOTION,
				appName,
				r.getSystem( ) );
		if ( subKeyFace == null || "".equals( subKeyFace ) )
		{
			CognitiveServices.AOM.throwException( appName,
				"No face subscription key found in the module configuration" );
		}

		/* Get emotion from MS Cognitive Services */
		String jsonResultString = RequestHelper.emotionRequest( subKeyEmotion, picUrl );
		CognitiveServices.AOM.log( appName, "jsonResult: " + jsonResultString, false );

		/* Go through all detections in the result */
		JSONArray jsonResult = new JSONArray( jsonResultString );
		for ( int i = 0; i < jsonResult.length( ); i++ )
		{
			CognitiveServices.AOM.log( appName, "got a detection from the MS API - turning into ApiOmat object...",
				false );
			JSONObject detectionJson = jsonResult.getJSONObject( i );

			JSONObject faceRectangleJson = detectionJson.getJSONObject( "faceRectangle" );
			JSONObject scoresJson = detectionJson.getJSONObject( "scores" );
			Emotion emotion = new Emotion( scoresJson );
			CognitiveServices.AOM.log( appName, "emotion values: " + emotion.toString( ), false );
			CognitiveServices.AOM.log( appName, "calculated happiness: " + emotion.calculateHappiness( ), false );
			/* Create and save detection object */
			Detection detection = new Detection( );
			detection.setPicId( obj.getForeignId( ) );
			detection.setTop( faceRectangleJson.getLong( "top" ) );
			detection.setWidth( faceRectangleJson.getLong( "width" ) );
			detection.setLeft( faceRectangleJson.getLong( "left" ) );
			detection.setHeight( faceRectangleJson.getLong( "height" ) );
			detection.setHappiness( emotion.calculateHappiness( ) );
			detection.save( );
		}
	}

	@Override
	public Picture doGet( String foreignId, Request r )
	{
		return null;
	}

	@Override
	public boolean doDelete( String foreignId, Request r )
	{
		return false;
	}

	@Override
	public boolean doDeleteAll( String query, Request r )
	{
		return false;
	}

	@Override
	public List<Picture> doGetAll( String query, Request r )
	{
		List<Picture> pictures = new ArrayList<>( );
		Picture pic = new Picture( );
		pic.setForeignId( String.valueOf( System.currentTimeMillis( ) ) );
		pictures.add( pic );
		return pictures;
	}

	@Override
	public long doCountAll( String query, Request r )
	{
		return 0;
	}

	/* Please note: Before doPostRef gets called, doGet gets called internally,
	 * so that this.model can be populated with attribute values. */
	@Override
	public void doPostRef( Object referencedObject, String referenceName, Request r )
	{}

	/* Please note: Before doDeleteRef gets called, doGet gets called internally,
	 * so that this.model can be populated with attribute values. */
	@Override
	public void doDeleteRef( String refName, String refForeignId, Request r )
	{}

	/* Please note: Before doGetRef gets called, doGet gets called internally,
	 * so that this.model can be populated with attribute values. */
	@Override
	public <Z extends AbstractClientDataModel> List<Z> doGetRef( String refName, String query, Request r )
	{
		return null;
	}
}
