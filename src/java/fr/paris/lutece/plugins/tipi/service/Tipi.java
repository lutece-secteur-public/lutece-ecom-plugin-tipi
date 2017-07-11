/**
 * 
 */
package fr.paris.lutece.plugins.tipi.service;

import java.math.BigDecimal;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import fr.paris.lutece.plugins.tipi.constant.Constants;
import fr.paris.lutece.plugins.tipi.utils.Utils;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.tipi.generated.CreerPaiementSecuriseRequest;
import fr.paris.tipi.generated.RecupererDetailPaiementSecuriseRequest;
import fr.paris.vdp.tipi.create.url.webservice.CreateURLWebService;
import fr.paris.vdp.tipi.create.url.webservice.ParametresPaiementTipi;

/**
 * @author stephane.raynaud
 *
 */
public class Tipi
{
    private static final long      serialVersionUID    = 7173528813310268296L;

    private static final Logger    logger              = Logger.getLogger( "lutece" );

    private static final String    TRUSTSTORE          = "tipi.ssl.truststore";
    private static final String    TRUSTSTORE_PASSWORD = "tipi.ssl.truststore.password"; // NOSONAR
    private static final String    KEYSTORE            = "tipi.ssl.keystore";
    private static final String    KEYSTORE_PASSWORD   = "tipi.ssl.keystore.password";   // NOSONAR

    private static final String    URLWSDL             = "tipi.urlwsdl";

    private static final String    CLIENT              = "client";
    private static final String    URL_TYPE            = "tipi.url";

    private String                 identifier;
    private ParametresPaiementTipi parameters;

    /**
     * Génération d'une nouvelle
     * 
     * @param email
     * @param numeroDossier
     * @param amount
     * @throws ServiceException
     * @throws RemoteException
     */
    public Tipi( String email, String numeroDossier, BigDecimal amount ) throws RemoteException, ServiceException
    {
        setCertificateValues( );

        final String urlWsdl = getWsdlUrl( );

        final CreerPaiementSecuriseRequest request = TipiRequestFactory.createRequest( email, numeroDossier, amount );

        logger.debug( "url du webservice : " + urlWsdl );
        logger.debug( "parametre de la requete : " + Utils.object2String( request ) );

        this.identifier = new CreateURLWebService( ).appelWebServiceCreerPaiement( urlWsdl, request );
    }

    /**
     * @param idOp
     * @throws ServiceException
     * @throws RemoteException
     */
    public Tipi( String idOp ) throws ServiceException, RemoteException
    {
        setCertificateValues( );

        final String urlWsdl = getWsdlUrl( );

        RecupererDetailPaiementSecuriseRequest request = new RecupererDetailPaiementSecuriseRequest( );
        request.setIdOp( idOp );

        logger.debug( "url du webservice : " + urlWsdl );
        logger.debug( "parametre de la requete : " + Utils.object2String( request ) );

        this.identifier = idOp;
        this.parameters = new CreateURLWebService( ).appelWebserviceDetailPaiement( request, urlWsdl );
    }

    private String getWsdlUrl( )
    {
        return AppPropertiesService.getProperty( URLWSDL );
    }

    private final void setCertificateValues( )
    {
        // Valorisation des proprietes systemes pour les echanges SSL avec le webservice
        System.setProperty( "javax.net.ssl.trustStore", AppPropertiesService.getProperty( TRUSTSTORE ) );
        System.setProperty( "javax.net.ssl.trustStorePassword", AppPropertiesService.getProperty( TRUSTSTORE_PASSWORD ) );
        System.setProperty( "javax.net.ssl.keyStore", AppPropertiesService.getProperty( KEYSTORE ) );
        System.setProperty( "javax.net.ssl.keyStorePassword", AppPropertiesService.getProperty( KEYSTORE_PASSWORD ) );
    }

    public String getLink( )
    {
        Boolean isClient = CLIENT.equals( AppPropertiesService.getProperty( URL_TYPE ) );
        return Utils.getUrlApplicationTipi( identifier, isClient );
    }

    /**
     * @return the identifier
     */
    public String getIdentifier( )
    {
        return identifier;
    }

    /**
     * @return the parameters
     */
    public ParametresPaiementTipi getParameters( )
    {
        return parameters;
    }

    /**
     * Retourne les paramètres d'un paiement Tipi directement
     * 
     * @param idop
     * @return
     * @throws RemoteException
     * @throws ServiceException
     */
    public static ParametresPaiementTipi getParameters( String idop ) throws RemoteException, ServiceException
    {
        return new Tipi( idop ).getParameters( );
    }

    /**
     * @return
     */
    public boolean isPaymentSuccess( )
    {
        return Constants.PAYMENT_SUCCESS.equals( parameters.getResultrans( ) );
    }

    /**
     * @return
     */
    public boolean isPaymentDenied( )
    {
        return Constants.PAYMENT_DENIED.equals( parameters.getResultrans( ) );
    }

    /**
     * @return
     */
    public boolean isPaymentCancelled( )
    {
        return Constants.PAYMENT_CANCELLED.equals( parameters.getResultrans( ) );
    }
}
