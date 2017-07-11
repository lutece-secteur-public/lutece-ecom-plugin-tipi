package fr.paris.lutece.plugins.tipi.service;

import java.math.BigDecimal;
import java.util.Calendar;

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.tipi.generated.CreerPaiementSecuriseRequest;
import fr.paris.vdp.tipi.create.url.enumeration.PaymentType;

/**
 * Cette classe permet de mapper les donnees issues de l'application dans la request du webservice.
 */
public class TipiRequestFactory
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
    public static CreerPaiementSecuriseRequest createRequest( String email, String refDet, BigDecimal amount )
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
