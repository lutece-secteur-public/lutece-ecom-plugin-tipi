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

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;

import fr.paris.lutece.plugins.tipi.util.Constants;
import fr.paris.lutece.plugins.tipi.business.Tipi;
import fr.paris.lutece.plugins.tipi.business.TipiProcessor;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * Classe pour les règlements Tipi
 */
public class TipiTask extends Daemon
{
    private TipiProcessor       processor   = SpringContextService.getBean( "tipiProcessor" );

    private static final String LOG_MESSAGE = "{0}: Traitement de {1} transactions [{2} succes, {3} refus, {4} annulation]\r\n";

    @Override
    public synchronized void run( )
    {

        List<String> idOps = processor.getPendingTransactions( );

        if ( idOps == null || idOps.isEmpty( ) )
        {
            return;
        }

        int success = 0;
        int denied = 0;
        int cancelled = 0;

        for ( String idOp : idOps )
        {
            try
            {
                final Tipi tipi = Tipi.read( idOp ).process( );

                if ( tipi.isPaymentSuccess( ) )
                {
                    success++;
                }
                if ( tipi.isPaymentDenied( ) )
                {
                    denied++;
                }
                if ( tipi.isPaymentCancelled( ) )
                {
                    cancelled++;
                }
            } catch ( RemoteException | ServiceException e )
            {
                AppLogService.error( "Erreur a la recuperation des informations de paiement pour l'ID: " + idOp, e );
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat( Constants.DATE_FORMAT );

        String message = MessageFormat.format( LOG_MESSAGE, dateFormat.format( new Date( ) ), success + denied + cancelled, success, denied, cancelled );

        if ( getLastRunLogs( ) != null )
        {
            setLastRunLogs( getLastRunLogs( ) + message );
        } else
        {
            setLastRunLogs( message );
        }
    }

}
