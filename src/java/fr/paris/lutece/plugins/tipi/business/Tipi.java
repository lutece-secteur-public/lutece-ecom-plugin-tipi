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
package fr.paris.lutece.plugins.tipi.business;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.tipi.util.Constants;
import fr.paris.lutece.plugins.tipi.util.Utils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.tipi.generated.CreerPaiementSecuriseRequest;
import fr.paris.tipi.generated.RecupererDetailPaiementSecuriseRequest;
import fr.paris.vdp.tipi.create.url.utils.PaiementUtils;
import fr.paris.vdp.tipi.create.url.webservice.CreateURLWebService;
import fr.paris.vdp.tipi.create.url.webservice.ParametresPaiementTipi;

/**
 * Classe de gestion du paiement Tipi
 */
public class Tipi implements Serializable
{
    private static final long      serialVersionUID    = 7173528813310268296L;

    private static final String    TRUSTSTORE          = "tipi.ssl.truststore";
    private static final String    TRUSTSTORE_PASSWORD = "tipi.ssl.truststore.password";
    private static final String    KEYSTORE            = "tipi.ssl.keystore";
    private static final String    KEYSTORE_PASSWORD   = "tipi.ssl.keystore.password";

    private static final String    URLWSDL             = "tipi.urlwsdl";

    private static final String    CLIENT              = "client";
    private static final String    URL_TYPE            = "tipi.url";

    private String                 identifier;
    private ParametresPaiementTipi parameters;

    private TipiProcessor          processor           = SpringContextService.getBean( "tipiProcessor" );

    /**
     * Default constructor
     */
    private Tipi( )
    {
        /* Init (or re-init) certificate each time we use Tipi. */
        this.setCertificateValues( );
    }

    /**
     * Créer une nouvelle instance
     * @return
     */
    private static Tipi newInstance( )
    {
        return new Tipi( );
    }

    /**
     * Génération d'un nouveau paiement
     * 
     * @param email
     *            email
     * @param refDet
     *            référence
     * @param amount
     *            montant
     * @return l'instance de gestion de Tipi
     * @throws RemoteException
     * @throws ServiceException
     */
    public static Tipi create( String email, String refDet, BigDecimal amount ) throws RemoteException, ServiceException
    {
        Tipi tipi = newInstance( );

        final String urlWsdl = getWsdlUrl( );

        final CreerPaiementSecuriseRequest request = TipiRequestFactory.createRequest( email, refDet, amount );

        AppLogService.debug( "url du webservice : " + urlWsdl );
        AppLogService.debug( "parametre de la requete : " + Utils.object2String( request ) );

        tipi.identifier = new CreateURLWebService( ).appelWebServiceCreerPaiement( urlWsdl, request );

        return tipi;
    }

    /**
     * Récupère les informations d'un paiement
     * 
     * @param idOp
     *            l'identifiant du paiement
     * @return l'instance de gestion de Tipi
     * @throws ServiceException
     * @throws RemoteException
     */
    public static Tipi read( String idOp ) throws ServiceException, RemoteException
    {
        Tipi tipi = newInstance( );

        final String urlWsdl = getWsdlUrl( );

        RecupererDetailPaiementSecuriseRequest request = new RecupererDetailPaiementSecuriseRequest( );
        request.setIdOp( idOp );

        AppLogService.debug( "url du webservice : " + urlWsdl );
        AppLogService.debug( "parametre de la requete : " + Utils.object2String( request ) );

        tipi.identifier = idOp;
        tipi.parameters = new CreateURLWebService( ).appelWebserviceDetailPaiement( request, urlWsdl );

        return tipi;
    }

    /**
     * Récupère les informations d'un paiement
     * 
     * @param request
     *            la requete http contenant le paramètre idOp ou idop
     * @return l'instance de gestion de Tipi
     * @throws RemoteException
     * @throws ServiceException
     */
    public static Tipi read( HttpServletRequest request ) throws RemoteException, ServiceException
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

        return read( idOp );
    }

    /**
     * Retourne l'url du WSDL
     * @return String url
     */
    private static final String getWsdlUrl( )
    {
        return AppPropertiesService.getProperty( URLWSDL );
    }

    /**
     * Ajoute le certificat Tipi
     */
    private final void setCertificateValues( )
    {
        // Valorisation des proprietes systemes pour les echanges SSL avec le webservice
        if ( AppPropertiesService.getProperty( KEYSTORE ).isEmpty( ) )
        {
            File file = new File( getClass( ).getClassLoader( ).getResource( "security/cacerts" ).getFile( ) );
            System.setProperty( "javax.net.ssl.trustStore", file.getAbsolutePath( ) );
            System.setProperty( "javax.net.ssl.keyStore", file.getAbsolutePath( ) );
        } else
        {
            System.setProperty( "javax.net.ssl.trustStore", AppPropertiesService.getProperty( TRUSTSTORE ) );
            System.setProperty( "javax.net.ssl.keyStore", AppPropertiesService.getProperty( KEYSTORE ) );
        }

        System.setProperty( "javax.net.ssl.trustStorePassword", AppPropertiesService.getProperty( TRUSTSTORE_PASSWORD ) );
        System.setProperty( "javax.net.ssl.keyStorePassword", AppPropertiesService.getProperty( KEYSTORE_PASSWORD ) );
    }

    /**
     * Récupère les paramètres de la transaction
     * 
     * @return les paramètres de la transaction
     */
    public ParametresPaiementTipi getParameters( )
    {
        return parameters;
    }

    /**
     * Retourne la date du paiement
     * 
     * @return date
     */
    public Date getDate( )
    {
        Date date = null;

        if ( parameters != null )
        {
            try
            {
                date = PaiementUtils.constructDate( parameters.getDatTrans( ), parameters.getHeurTrans( ) );
            } catch ( ParseException e )
            {
                // Do nothing
            }
        }

        return date;
    }

    /**
     * Retourne les paramètres d'une transaction Tipi
     * 
     * @param idop
     * @return les paramètres de la transaction
     * @throws RemoteException
     * @throws ServiceException
     */
    public static ParametresPaiementTipi getParameters( String idop ) throws RemoteException, ServiceException
    {
        return read( idop ).getParameters( );
    }

    /**
     * Retourne un booléen indiquant si le paiement est un succès
     * 
     * @return vrai si le paiement est un succès
     */
    public boolean isPaymentSuccess( )
    {
        return Constants.PAYMENT_SUCCESS.equals( parameters.getResultrans( ) );
    }

    /**
     * Retourne un booléen indiquant si le paiement est refusé
     * 
     * @return vrai si le paiement est refusé
     */
    public boolean isPaymentDenied( )
    {
        return Constants.PAYMENT_DENIED.equals( parameters.getResultrans( ) );
    }

    /**
     * Retourne un booléen indiquant si le paiement est annulé
     * 
     * @return vrai si le paiement est annulé
     */
    public boolean isPaymentCancelled( )
    {
        return Constants.PAYMENT_CANCELLED.equals( parameters.getResultrans( ) );
    }

    /**
     * Gère le résultat dans le result handler
     * 
     * @return l'instance de gestion de Tipi
     */
    public Tipi process( )
    {
        if ( this.processor == null )
        {
            AppLogService.info( "Aucun handler n'a été défini pour gérer le retour du service Tipi" );
        } else
        {
            this.processor.process( this );
        }

        return this;
    }

    public String getLink( )
    {
        Boolean isClient = CLIENT.equals( AppPropertiesService.getProperty( URL_TYPE ) );
        return Utils.getUrlApplicationTipi( identifier, isClient );
    }

    /**
     * Récupère l'identifiant de transaction Tipi
     *
     * @return l'identifiant de transaction Tipi
     */
    public String getIdentifier( )
    {
        return identifier;
    }
}
