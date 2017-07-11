/*
 * Copyright (c) 2002-2015, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.tipi.web;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.tipi.service.Tipi;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.vdp.tipi.create.url.utils.PaiementUtils;

/**
 * Used for special solr queries
 *
 *
 */
public abstract class TipiServlet extends HttpServlet
{
    private static final long serialVersionUID = -7065654487722361439L;

    /**
     * Action en cas de succès du paiement
     * 
     * @param tipiHandler
     *            les paramètres Tipi
     */
    public abstract void paymentSuccess( Tipi tipiHandler );

    /**
     * Action en cas de refus du paiement
     * 
     * @param tipiHandler
     *            les paramètres Tipi
     */
    public abstract void paymentDenied( Tipi tipiHandler );

    /**
     * Action en cas d'annulation du paiement
     * 
     * @param tipiHandler
     *            les paramètres Tipi
     */
    public abstract void paymentCancelled( Tipi tipiHandler );

    /**
     * Returns poster image
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    protected final void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            handlePayment( getParameters( request ) );
        } catch ( ParseException e )
        {
            throw new ServletException( "erreur traitement Tipi : " + e.getMessage( ) );
        }
    }

    /**
     * Returns poster image
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    protected final void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        try
        {
            handlePayment( getParameters( request ) );
        } catch ( ParseException e )
        {
            throw new ServletException( "erreur traitement Tipi : " + e.getMessage( ) );
        }
    }

    /**
     * Valorise les informations de paiement
     *
     * @param request
     * @return
     * @throws EvacException
     */
    private Tipi getParameters( HttpServletRequest request ) throws ServletException
    {
        String idOp = request.getParameter( "idOp" );

        if ( StringUtils.isBlank( idOp ) )
        {
            idOp = String.valueOf( request.getParameter( "idop" ) );
        }

        if ( StringUtils.isBlank( idOp ) )
        {
            AppLogService.error( "echec traitement servlet, idOp is null" );
            return null;
        }

        try
        {
            return new Tipi( idOp );
        } catch ( RemoteException | ServiceException e )
        {
            AppLogService.error( "servlet, idOp = " + idOp + ", echec lors de l'acces au webservice pour recuperer les informations de paiement : " + e.getMessage( ) );
            throw new ServletException( e );
        }
    }

    /**
     * Apres le paiement sur tipi, on enregistre en base le resultat de la transaction.
     *
     * @param tipiHandler
     *            contient les donnees de la transaction
     * @throws ParseException
     *             en cas d'erreur sur la date
     * @throws EvacException
     *             en cas de paramètre invalide
     */
    public final void handlePayment( Tipi tipiHandler ) throws ParseException
    {
        // On vérifie que la requete contient bien les données nécessaires
        String invalidReqParamMessage = PaiementUtils.requestParamsInvalid( tipiHandler.getParameters( ) );

        if ( !StringUtils.isEmpty( invalidReqParamMessage ) )
        {
            AppLogService.error( invalidReqParamMessage );
            throw new InvalidParameterException( "Erreur lors de la récupération des paramètres Tipi : " + invalidReqParamMessage );
        }

        // Si Resultr
        if ( tipiHandler.isPaymentSuccess( ) )
        {
            // Paiement validé
            paymentSuccess( tipiHandler );

        } else if ( tipiHandler.isPaymentDenied( ) )
        {
            // Paiement refusé
            paymentDenied( tipiHandler );
        } else
        {
            // Paiement abandonné
            paymentCancelled( tipiHandler );
        }
    }

}
