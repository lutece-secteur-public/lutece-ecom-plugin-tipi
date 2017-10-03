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

package fr.paris.lutece.plugins.tipi.business;

import java.math.BigDecimal;
import java.util.Calendar;

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.tipi.generated.CreerPaiementSecuriseRequest;
import fr.paris.vdp.tipi.create.url.enumeration.PaymentType;

/**
 * Cette classe permet de mapper les donnees issues de l'application dans la request du webservice.
 */
public final class TipiRequestFactory
{
    private static final String     X                = "X";
    private static final String     W                = "W";
    private static final String     REFERENCE_CLIENT = "tipi.numcli";
    private static final String     TIPI_OBJET       = "tipi.objet";
    private static final String     URL_NOTIF        = "tipi.urlnotif";
    private static final String     URL_REDIRECT     = "tipi.urlredirect";
    private static final String     PAYMENT_TYPE     = "tipi.saisie";
    private static final BigDecimal CENT             = new BigDecimal( "100" );

    private TipiRequestFactory( )
    {
    }

    /**
     * Creation de la requete envoyee au webservice.
     *
     * @param email
     *            mail de l'usager
     * @param refDet
     *            identifiant
     * @param amount
     *            montant de la transaction en euros
     *
     * @return la requete pour le webservice
     */
    public static final CreerPaiementSecuriseRequest createRequest( String email, String refDet, BigDecimal amount )
    {
        CreerPaiementSecuriseRequest request = new CreerPaiementSecuriseRequest( );
        Calendar calendar = Calendar.getInstance( );

        request.setMel( email );

        // Passage de Euros en Centimes
        request.setMontant( String.valueOf( Integer.valueOf( amount.multiply( CENT ).intValueExact( ) ) ) );
        request.setRefdet( refDet );
        request.setNumcli( AppPropertiesService.getProperty( REFERENCE_CLIENT ) );
        request.setUrlnotif( AppPropertiesService.getProperty( URL_NOTIF ) );
        request.setUrlredirect( AppPropertiesService.getProperty( URL_REDIRECT ) );
        request.setExer( String.valueOf( calendar.get( Calendar.YEAR ) ) );
        request.setObjet( AppPropertiesService.getProperty( TIPI_OBJET ) );

        String saisie = AppPropertiesService.getProperty( PAYMENT_TYPE );

        if ( W.equalsIgnoreCase( saisie ) )
        {
            request.setSaisie( PaymentType.PRODUCTION_WS.getStringValues( ) );
        } else if ( X.equalsIgnoreCase( saisie ) )
        {
            request.setSaisie( PaymentType.ACTIVATION.getStringValues( ) );
        } else
        {
            request.setSaisie( PaymentType.TEST.getStringValues( ) );
        }

        return request;
    }

}
