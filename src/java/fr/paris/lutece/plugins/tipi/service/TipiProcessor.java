/**
 * 
 */
package fr.paris.lutece.plugins.tipi.service;

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
        final String invalidReqParamMessage = PaiementUtils.requestParamsInvalid( tipi.getParameters( ) );

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
