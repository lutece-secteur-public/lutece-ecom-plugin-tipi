/*
 *
 *  * Copyright (c) 2002-2017, Mairie de Paris
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions
 *  * are met:
 *  *
 *  *  1. Redistributions of source code must retain the above copyright notice
 *  *     and the following disclaimer.
 *  *
 *  *  2. Redistributions in binary form must reproduce the above copyright notice
 *  *     and the following disclaimer in the documentation and/or other materials
 *  *     provided with the distribution.
 *  *
 *  *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *  *     contributors may be used to endorse or promote products derived from
 *  *     this software without specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *  *
 *  * License 1.0
 *
 */

/**
 * 
 */
package fr.paris.lutece.plugins.tipi.util;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.rpc.ServiceException;

import org.codehaus.jackson.map.ObjectMapper;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.vdp.tipi.create.url.webservice.CreateURLWebService;

/**
 * @author stephane.raynaud
 *
 */
public class Utils
{
    private Utils( )
    {
    }

    /**
     * Permet de transformer un objet en String pour l'affichage dans la log.
     *
     * @param obj
     *            L'objet à transformer
     * @return unString a afficher dans la log
     * @throws ServiceException
     */
    public static String object2String( Object obj ) throws ServiceException
    {
        ObjectMapper objectMapper = new ObjectMapper( );
        StringWriter stringEmp = new StringWriter( );

        try
        {
            objectMapper.writeValue( stringEmp, obj );
        } catch ( IOException e )
        {
            AppLogService.error( "erreur lors de la conversion : " + e.getMessage( ) );
            throw new ServiceException( e );
        }
        return stringEmp.toString( );
    }

    /**
     * Fait appel a CreateUrlWebService qui genere l'url a utiliser.
     *
     * @param idOp
     * @param isTest
     * @return une url
     */
    public static String getUrlApplicationTipi( String idOp, Boolean isTest )
    {

        return CreateURLWebService.getUrlApplicationTipiWebService( idOp, isTest );

    }

}
