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
import com.apiomat.nativemodule.IModel;
import com.apiomat.nativemodule.IModelHooksTransient;
import com.apiomat.nativemodule.Request;

/**
 * Generated class for hooks on your ObjectPicture data model
 */

public class ObjectPictureHooksTransient<T extends ObjectPicture> implements IModelHooksTransient<ObjectPicture>
{
	protected ObjectPicture model;

	@Override
	public void setCallingModel( ObjectPicture model )
	{
		this.model = model;
	}

	/* do-Methods can be used if your data model is set to TRANSIENT */

	@Override
	public String doPost( ObjectPicture obj, Request r )
	{
		return String.valueOf( System.currentTimeMillis( ) );
	}

	@Override
	public void doPut( ObjectPicture obj, Request r )
	{
		String appName = r.getApplicationName( );
		CognitiveServices.AOM.log( appName, "foreign ID: " + obj.getForeignId( ), false );

		/* Take picture URL from PUT object */

		String picUrl = "https://studio.apiomat.enterprises" + obj.getContentURL( ) + ".img?apiKey=" + r.getApiKey( ) +
			"&system=" + r.getSystem( );
		CognitiveServices.AOM.log( appName, "pic url: " + picUrl, false );

		/* Read module configuration */

		String subKeyObject =
			( String ) CognitiveServices.APP_CONFIG_PROXY.getConfigValue( CognitiveServices.SUBSCRIPTION_KEY_OBJECT,
				appName,
				r.getSystem( ) );
		if ( subKeyObject == null || "".equals( subKeyObject ) )
		{
			CognitiveServices.AOM.throwException( appName,
				"No computer vision subscription key found in the module configuration" );
		}

		/* Get recognized objects / caption from MS Cognitive Services */
		String jsonResultObjectString = RequestHelper.objectRequest( subKeyObject, picUrl );
		CognitiveServices.AOM.log( appName, "jsonResultObjectString: " + jsonResultObjectString, false );

		JSONObject jsonResultObject = new JSONObject( jsonResultObjectString );
		JSONArray captionsJsonArray = jsonResultObject.getJSONObject( "description" ).getJSONArray( "captions" );
		if ( captionsJsonArray.length( ) > 0 )
		{
			JSONObject captionJsonObject = captionsJsonArray.getJSONObject( 0 );
			String captionString = captionJsonObject.getString( "text" );
			CognitiveServices.AOM.log( appName, "captionString: " + captionString, false );

			/* Create detection */
			TranslatedDetection translatedDetection = new TranslatedDetection( );
			List<String> detections = new ArrayList<>( );
			detections.add( captionString );
			translatedDetection.setDetections( detections );
			translatedDetection.setForeignId( obj.getForeignId( ) );

			/* Add translation */
			String subKeyTranslate =
				( String ) CognitiveServices.APP_CONFIG_PROXY.getConfigValue(
					CognitiveServices.SUBSCRIPTION_KEY_TRANSLATE, appName, r.getSystem( ) );
			if ( subKeyTranslate == null || "".equals( subKeyTranslate ) )
			{
				CognitiveServices.AOM.throwException( appName,
					"No translate subscription key found in the module configuration" );
			}
			/* get language to translate to from user */
			String translatedCaption = null;
			try
			{
				IModel<?>[ ] users =
					CognitiveServices.AOM.findByNames( appName,
						LanguageUser.MODULE_NAME, LanguageUser.MODEL_NAME, "userName == \"" + r.getUserEmail( ) + "\"",
						r );
				LanguageUser user = ( LanguageUser ) users[ 0 ];
				translatedCaption =
					RequestHelper.translationRequest( subKeyTranslate, captionString, user.getLearningLanguageCode( ) );
			}
			catch ( Exception ex )
			{
				CognitiveServices.AOM.logError( appName, "error while finding language user: " + ex.toString( ),
					false );
				translatedCaption =
					RequestHelper.translationRequest( subKeyTranslate, captionString, "de" );
			}

			CognitiveServices.AOM.log( appName, "translatedCaption: " + translatedCaption, false );
			List<String> translations = new ArrayList<>( );
			translations.add( translatedCaption );
			translatedDetection.setTranslations( translations );

			translatedDetection.save( );
		}
	}

	@Override
	public ObjectPicture doGet( String foreignId, Request r )
	{
		ObjectPicture temp = new ObjectPicture( );
		temp.setForeignId( foreignId );
		return temp;
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
	public List<ObjectPicture> doGetAll( String query, Request r )
	{
		List<ObjectPicture> pictures = new ArrayList<>( );
		ObjectPicture pic = new ObjectPicture( );
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
