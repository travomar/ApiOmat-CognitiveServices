/*
 * Copyright (c) 2011 - 2018, Apinauten GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.apiomat.nativemodule.cognitiveservices;

import org.json.JSONArray;
import org.json.JSONObject;

import com.apiomat.nativemodule.IStaticMethods;
import com.apiomat.nativemodule.Request;
import com.apiomat.nativemodule.helpers.AppBackendHelper;

/**
 * @author phimi
 */
public class CognitiveHelper
{
	private final AppBackendHelper helper;
	private final IStaticMethods methods;
	private final Request r;

	/**
	 * @param configProxy
	 * @param methods
	 */
	public CognitiveHelper( IStaticMethods methods, final Request request )
	{
		this.helper = new AppBackendHelper( CognitiveServices.APP_CONFIG_PROXY, request );
		this.methods = methods;
		this.r = request;
	}

	/**
	 * @param foreignId
	 * @param bytePics
	 */
	public Detection detectFace( final String foreignId, final byte[ ] bytePics )
	{
		Detection detection = null;
		final String appName = this.r.getApplicationName( );

		if ( bytePics.length > 0 )
		{
			/* Read module configuration */
			final String personGroupConfig =
				this.helper.getAppConfigValue( CognitiveServices.DEFAULT_PERSON_GROUP, String.class );

			String subKeyEmotion =
				this.helper.getAppConfigValue( CognitiveServices.SUBSCRIPTION_KEY_EMOTION, String.class );
			if ( subKeyEmotion == null || "".equals( subKeyEmotion ) )
			{
				this.methods.throwException( appName,
					"No emotion subscription key found in the module configuration" );
			}
			String subKeyFace =
				this.helper.getAppConfigValue( CognitiveServices.SUBSCRIPTION_KEY_FACE, String.class );
			if ( subKeyFace == null || "".equals( subKeyFace ) )
			{
				this.methods.throwException( appName,
					"No face subscription key found in the module configuration" );
			}

			/* Get emotion from MS Cognitive Services */
			String jsonResultEmotionString = RequestHelper.emotionRequest( subKeyEmotion, bytePics );
			this.methods.log( appName, "jsonResultEmotionString: " + jsonResultEmotionString, false );

			/* Get face IDs for recognition */
			String jsonResultFaceString = RequestHelper.faceRequest( subKeyFace, bytePics );
			this.methods.log( appName, "jsonResultFaceString: " + jsonResultFaceString, false );

			/* Go through all detections in the result */
			if ( jsonResultEmotionString != null && jsonResultEmotionString.length( ) > 0 )
			{

				JSONArray jsonResultEmotion = new JSONArray( jsonResultEmotionString );
				JSONArray jsonResultFace = new JSONArray( jsonResultFaceString );
				for ( int i = 0; i < jsonResultEmotion.length( ); i++ )
				{
					this.methods.log( appName,
						"got a detection from the MS API - turning into ApiOmat object...",
						false );
					JSONObject emotionJson = jsonResultEmotion.getJSONObject( i );

					JSONObject emotionFaceRectangleJson = emotionJson.getJSONObject( "faceRectangle" );
					Rectangle rectangle = new Rectangle( emotionFaceRectangleJson );

					JSONObject emotionScoresJson = emotionJson.getJSONObject( "scores" );
					Emotion emotion = new Emotion( emotionScoresJson );

					this.methods.log( appName, "emotion values: " + emotion.toString( ), false );
					this.methods.log( appName, "calculated happiness: " + emotion.calculateHappiness( ),
						false );

					/* Create detection object and set face rectangle and emotion */
					detection = ( Detection ) this.methods.createObject( appName,
						Detection.MODULE_NAME, Detection.MODEL_NAME, this.r );
					detection.setPicId( foreignId );
					detection.setForeignId( foreignId );
					detection.setTop( rectangle.getTop( ) );
					detection.setWidth( rectangle.getWidth( ) );
					detection.setLeft( rectangle.getLeft( ) );
					detection.setHeight( rectangle.getHeight( ) );
					detection.setHappiness( emotion.calculateHappiness( ) );

					/* check if face result has a result for the same rectangle */
					for ( int j = 0; j < jsonResultFace.length( ); j++ )
					{
						JSONObject faceJson = jsonResultFace.getJSONObject( j );
						JSONObject faceRectangleJson = faceJson.getJSONObject( "faceRectangle" );
						Rectangle rectangleFace = new Rectangle( faceRectangleJson );
						if ( rectangle.equals( rectangleFace ) )
						{
							/* set gender and age */
							JSONObject faceAttributes = faceJson.getJSONObject( "faceAttributes" );
							String gender = faceAttributes.getString( "gender" );
							this.methods.log( appName, "found gender: " + gender, false );
							long age = new Double( faceAttributes.getDouble( "age" ) ).longValue( );
							this.methods.log( appName, "found age: " + age, false );
							detection.setGender( gender );
							detection.setAge( age );

							String faceId = faceJson.getString( "faceId" );
							/* identify (for name) */
							String identifyRequest =
								RequestHelper.identifyRequest( subKeyFace, personGroupConfig, faceId );
							if ( identifyRequest != null && identifyRequest.length( ) > 0 &&
								identifyRequest.startsWith( "[" ) )
							{
								JSONArray identifyJsonArray = new JSONArray( identifyRequest );
								JSONObject identifyJsonObject = identifyJsonArray.getJSONObject( 0 );
								JSONArray candidatesJson = identifyJsonObject.getJSONArray( "candidates" );
								if ( candidatesJson.length( ) > 0 )
								{
									JSONObject candidate = candidatesJson.getJSONObject( 0 );
									String personId = candidate.getString( "personId" );

									/* get person with name */
									String personJsonString =
										RequestHelper.personRequest( subKeyFace, personGroupConfig, personId );
									JSONObject personJson = new JSONObject( personJsonString );
									String personName = personJson.getString( "name" );
									this.methods.log( appName, "found name: " + personName, false );

									/* set found name */
									detection.setName( personName );
								}
								else
								{
									detection.setName( "?" );
								}
							}
							else
							{
								detection.setName( "?" );
							}
						}
					}

					/* save */
					detection.save( );
				}
			}
			else
			{
				this.methods.log( appName, "no jsonResultEmotionString", false );
			}

		}

		return detection;
	}

}
