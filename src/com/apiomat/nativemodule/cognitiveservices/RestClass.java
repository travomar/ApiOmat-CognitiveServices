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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.apiomat.nativemodule.AbstractRestResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * REST class for your module
 */
@Api( value = "/CognitiveServices", tags="CognitiveServices" )
public class RestClass extends AbstractRestResource
{
    /**
     * Constructor, leave as is
     *
     * @param uriInfo
     * @param servletRequest
     * @param securityContext
     * @param wsRequest
     */
    public RestClass( UriInfo uriInfo, HttpServletRequest servletRequest, SecurityContext securityContext,
        Request wsRequest )
    {
        super( uriInfo, servletRequest, securityContext, wsRequest );
    }

    /**
     * A simple ping-like GET endpoint.
     * You can pass a <PARAM> to the following URL, which is contained in the response then.
     *
     * curl <BASEURL>/yambas/rest/modules/CognitiveServices/{appName}/spec/ping/<PARAM>
     *
     * The @ApiOperation and @ApiParam annotations are used to documnt the REST endpoint in the apidocs:
     * <BASEURL>/apidocs/index.html
     *
     * @param param arbitrary value which is returned in response
     * @return response
     */
    @ApiOperation( value = "A simple ping-like GET endpoint" )
    @GET
    @Path( "/ping/{param}" )
    public Response ping( @ApiParam( value = "param name" ) @PathParam( "param" ) String param )
    {
        final com.apiomat.nativemodule.Request request = this.getAOMRequest( );
        // extract auth information from the request object if needed
        System.out.println( request );

        if (param == null || "".equals( param.trim( ) ))
        {
            param = "pong";
        }

        return Response.ok( param ).type( MediaType.TEXT_PLAIN ).build( );
    }
}
