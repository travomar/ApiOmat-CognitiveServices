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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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

		String picUrl = "http://poc.apiomat.enterprises" + obj.getContentURL( ) + ".img?apiKey=" + r.getApiKey( );
		CognitiveServices.AOM.log( appName, "pic url: " + picUrl, false );

		String subKey =
			( String ) CognitiveServices.APP_CONFIG_PROXY.getConfigValue( CognitiveServices.SUBSCRIPTION_KEY, appName,
				r.getSystem( ) );
		if ( subKey == null || "".equals( subKey ) )
		{
			CognitiveServices.AOM.throwException( appName, "No subscription key found in the module configuration" );
		}
		String jsonResult = faceRequest( subKey, picUrl );
		CognitiveServices.AOM.log( appName, "jsonResult: " + jsonResult, false );
	}

	private static String faceRequest( String subscriptionKey, String picUrl )
	{
		String result = null;
		HttpClient httpclient = new DefaultHttpClient( );

		try
		{
			URIBuilder builder = new URIBuilder( "https://westeurope.api.cognitive.microsoft.com/face/v1.0/detect" );

			// Request parameters. All of them are optional.
			builder.setParameter( "returnFaceId", "true" );
			builder.setParameter( "returnFaceLandmarks", "false" );
			builder.setParameter( "returnFaceAttributes",
				"age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise" );

			// Prepare the URI for the REST API call.
			URI uri = builder.build( );
			HttpPost request = new HttpPost( uri );

			// Request headers.
			request.setHeader( "Content-Type", "application/json" );
			request.setHeader( "Ocp-Apim-Subscription-Key", subscriptionKey );

			// Request body.
			StringEntity reqEntity = new StringEntity(
				"{\"url\":\"" + picUrl + "\"}" );
			request.setEntity( reqEntity );

			// Execute the REST API call and get the response entity.
			HttpResponse response = httpclient.execute( request );
			HttpEntity entity = response.getEntity( );

			if ( entity != null )
			{
				// Format and display the JSON response.
				System.out.println( "REST Response:\n" );

				result = EntityUtils.toString( entity ).trim( );
			}
		}
		catch ( Exception e )
		{
			// Display error message.
			System.out.println( e.getMessage( ) );
		}
		return result;
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
