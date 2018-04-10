/*
 * Copyright (c) 2011 - 2017, Apinauten GmbH
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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.apiomat.nativemodule.AbstractClientDataModel;
import com.apiomat.nativemodule.IModelHooksNonTransient;
import com.apiomat.nativemodule.Request;
import com.apiomat.nativemodule.helpers.AppBackendHelper;

/**
 * Generated class for hooks on your Picture data model
 */

public class PictureHooksNonTransient<T extends Picture> implements IModelHooksNonTransient<Picture>
{
	protected Picture model;

	@Override
	public void setCallingModel( Picture model )
	{
		this.model = model;
	}

	/* Following Methods can be used if your data model is NOT set to TRANSIENT */

	@Override
	public void beforePost( Picture obj, Request r )
	{}

	@Override
	public void afterPost( Picture obj, Request r )
	{}

	@Override
	public void beforeGet( String id, Request r )
	{}

	@Override
	public void afterGet( Picture obj, Request r )
	{}

	@Override
	public boolean beforePut( Picture objFromDB, Picture obj, Request r )
	{
		return false;
	}

	@Override
	public void afterPut( Picture obj, Request r )
	{
		String appName = r.getApplicationName( );
		CognitiveServices.AOM.log( appName, "start do put", false );
		CognitiveServices.AOM.log( appName, "foreign ID: " + obj.getForeignId( ), false );

		CognitiveServices.AOM.log( appName, "content url: " + obj.getContentURL( ), false );

		/* Take picture URL from PUT object */
		if ( obj.getContentURL( ) != null && obj.getContentURL( ).isEmpty( ) == false )
		{
			final byte[ ] bytePics = obj.loadContent( );

			/* Read module configuration */
			AppBackendHelper helper = new AppBackendHelper( CognitiveServices.APP_CONFIG_PROXY, r );
			final String personGroupConfig =
				helper.getAppConfigValue( CognitiveServices.DEFAULT_PERSON_GROUP, String.class );

			String subKeyEmotion =
				helper.getAppConfigValue( CognitiveServices.SUBSCRIPTION_KEY_EMOTION, String.class );
			if ( subKeyEmotion == null || "".equals( subKeyEmotion ) )
			{
				CognitiveServices.AOM.throwException( appName,
					"No emotion subscription key found in the module configuration" );
			}
			String subKeyFace =
				helper.getAppConfigValue( CognitiveServices.SUBSCRIPTION_KEY_FACE, String.class );
			if ( subKeyFace == null || "".equals( subKeyFace ) )
			{
				CognitiveServices.AOM.throwException( appName,
					"No face subscription key found in the module configuration" );
			}

			/* Get emotion from MS Cognitive Services */
			String jsonResultEmotionString = RequestHelper.emotionRequest( subKeyEmotion, bytePics );
			CognitiveServices.AOM.log( appName, "jsonResultEmotionString: " + jsonResultEmotionString, false );

			/* Get face IDs for recognition */
			String jsonResultFaceString = RequestHelper.faceRequest( subKeyFace, bytePics );
			CognitiveServices.AOM.log( appName, "jsonResultFaceString: " + jsonResultFaceString, false );

			/* Go through all detections in the result */
			if ( jsonResultEmotionString != null && jsonResultEmotionString.length( ) > 0 )
			{

				JSONArray jsonResultEmotion = new JSONArray( jsonResultEmotionString );
				JSONArray jsonResultFace = new JSONArray( jsonResultFaceString );
				for ( int i = 0; i < jsonResultEmotion.length( ); i++ )
				{
					CognitiveServices.AOM.log( appName,
						"got a detection from the MS API - turning into ApiOmat object...",
						false );
					JSONObject emotionJson = jsonResultEmotion.getJSONObject( i );

					JSONObject emotionFaceRectangleJson = emotionJson.getJSONObject( "faceRectangle" );
					Rectangle rectangle = new Rectangle( emotionFaceRectangleJson );

					JSONObject emotionScoresJson = emotionJson.getJSONObject( "scores" );
					Emotion emotion = new Emotion( emotionScoresJson );

					CognitiveServices.AOM.log( appName, "emotion values: " + emotion.toString( ), false );
					CognitiveServices.AOM.log( appName, "calculated happiness: " + emotion.calculateHappiness( ),
						false );

					/* Create detection object and set face rectangle and emotion */
					Detection detection = this.model.createObject( Detection.class, r );
					detection.setPicId( obj.getForeignId( ) );
					detection.setForeignId( obj.getForeignId( ) );
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
							CognitiveServices.AOM.log( appName, "found gender: " + gender, false );
							long age = new Double( faceAttributes.getDouble( "age" ) ).longValue( );
							CognitiveServices.AOM.log( appName, "found age: " + age, false );
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
									CognitiveServices.AOM.log( appName, "found name: " + personName, false );

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
				CognitiveServices.AOM.log( appName, "no jsonResultEmotionString", false );
			}

		}
	}

	@Override
	public boolean beforeDelete( Picture obj, Request r )
	{
		return false;
	}

	@Override
	public String beforeGetAll( String query, Request r )
	{
		/* NOTE that returning null or "" would ignore any query and always return any object of this class and
		 * backend */
		return query;
	}

	@Override
	public List<Picture> afterGetAll( List<Picture> objects, String query, Request r )
	{
		/* If you want to change the returned list of elements, you have to create a new list
		 * and add the elements to return to it. Because getting elements from the "objects"
		 * list will return a copy, changing values in this list does not have any effect.
		 *
		 * If NULL is returned, unnecessary conversions are omitted and result is taken from database. */
		return null;
	}

	@Override
	public boolean beforePostRef( Picture obj, Object referencedObject, String referenceName, Request r )
	{
		return false;
	}

	@Override
	public void afterPostRef( Picture obj, Object referencedObject, String referenceName, Request r )
	{}

	@Override
	public boolean beforeDeleteRef( Picture obj, Object referencedObject, String referenceName, Request r )
	{
		return false;
	}

	@Override
	public void afterDeleteRef( Picture obj, Object referencedObject, String referenceName, Request r )
	{}

	@Override
	public String beforeGetAllReferences( String query, String referenceName, Request r )
	{
		/* NOTE that returning null or "" would ignore any query and always return any referenced object */
		return query;
	}

	@Override
	public <Z extends AbstractClientDataModel> List<Z> afterGetAllReferences( List<Z> objects, String query,
		String referenceName, Request request )
	{
		return null; // return objects here if you changed sth; returning null prevents unnecessary conversions
	}
}
