/**
 * 
 */
package fr.paris.lutece.plugins.tipi.utils;

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
