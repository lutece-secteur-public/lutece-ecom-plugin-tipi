package fr.paris.lutece.plugins.tipi.task;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;

import fr.paris.lutece.plugins.tipi.constant.Constants;
import fr.paris.lutece.plugins.tipi.service.Tipi;
import fr.paris.lutece.plugins.tipi.service.TipiProcessor;
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
