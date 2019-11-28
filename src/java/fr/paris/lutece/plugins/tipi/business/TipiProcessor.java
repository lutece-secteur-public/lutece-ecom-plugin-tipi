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

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.vdp.tipi.create.url.utils.PaiementUtils;

/**
 * @author stephane.raynaud
 *
 */
public abstract class TipiProcessor implements Serializable
{
    private static final long serialVersionUID = 547069111354940576L;

    /**
     * Default constructor
     */
    public TipiProcessor( )
    {
        // Empty constructor
    }

    /**
     * Action en cas de succès du paiement
     * 
     * @param tipi
     *            les paramètres Tipi
     */
    public abstract void paymentSuccess( Tipi tipi );

    /**
     * Action en cas de refus du paiement
     * 
     * @param tipi
     *            les paramètres Tipi
     */
    public abstract void paymentDenied( Tipi tipi );

    /**
     * Action en cas d'annulation du paiement
     * 
     * @param tipi
     *            les paramètres Tipi
     */
    public abstract void paymentCancelled( Tipi tipi );

    /**
     * Récupère la liste des transactions en cours
     * 
     * @return la liste des idop en cours
     */
    public abstract List<String> getPendingTransactions( );

    /**
     * Gère le résultat dans le result handler
     * 
     * @param tipi
     *            la connexion Tipi
     * @return l'instance de gestion de Tipi
     */
    public Tipi process( Tipi tipi )
    {
        // On vérifie que la requete contient bien les données nécessaires
        final String invalidReqParamMessage = PaiementUtils.requestParamsInvalid( tipi.getParameters( ), tipi.isPaymentSuccess() );

        if ( !StringUtils.isEmpty( invalidReqParamMessage ) )
        {
            AppLogService.error( invalidReqParamMessage );
            throw new InvalidParameterException( "Erreur lors de la récupération des paramètres Tipi : " + invalidReqParamMessage );
        }

        // Si Resultat
        if ( tipi.isPaymentSuccess( ) )
        {
            // Paiement validé
            this.paymentSuccess( tipi );

        } else if ( tipi.isPaymentDenied( ) )
        {
            // Paiement refusé
            this.paymentDenied( tipi );
        } else
        {
            // Paiement abandonné
            this.paymentCancelled( tipi );
        }

        return tipi;
    }
}
