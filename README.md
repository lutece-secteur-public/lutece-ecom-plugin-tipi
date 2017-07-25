# Plugin tipi

## Introduction
The Lutece Tipi plugin is made to simplify the use of Tipi. It offers a simple interface to read, create and handle transactions.

## Getting started
To use the plugin, first add the plugin to your pom.xml :
```xml
    <dependency>
        <groupId>fr.paris.lutece.plugins</groupId>
        <artifactId>plugin-tipi</artifactId>
        <version>1.0.0</version>
        <type>lutece-plugin</type>
    </dependency>
```
Then implement a service named `tipiProcessor` which extends the `TipiProcessor` abstract class:
```java
import java.util.List;
import org.springframework.stereotype.Service;
import fr.paris.lutece.plugins.tipi.service.Tipi;
import fr.paris.lutece.plugins.tipi.service.TipiProcessor;

@Service("tipiProcessor")
public class SampleTipiProcessor extends TipiProcessor
{
    @Override
    public void paymentSuccess( Tipi tipi ) { /* To be implemented */ }

    @Override
    public void paymentDenied( Tipi tipi ) { /* To be implemented */ }

    @Override
    public void paymentCancelled( Tipi tipi ) { /* To be implemented */ }

    @Override
    public List<String> getPendingTransactions( ) { /* To be implemented */ }
}
```
> Note : Ensure your service is detected by Spring annotations in the xml context file.

Now, your Tipi plugin is ready to use:

```java
Tipi tipi = Tipi.create( "email@paris.fr", "REFERENCE", new BigDecimal(12.5) );
```
```java
Tipi tipi = Tipi.read( httpRequest );
Tipi tipi = Tipi.read( idTransaction );
```
Then process the transaction via the TipiProcessor:
```java
tipi.process();
```

## Daemon
Tipi plugin comes with a deamon that will check all payments and treat them with the TipiProcessor.

It calls the `List<String> getPendingTransactions( )` method to get a list of transaction ids, then checks each transaction and calls the `void paymentSuccess( Tipi tipi )` or `void paymentDenied( Tipi tipi )` or `void paymentCancelled( Tipi tipi )` methods of the same TipiProcessor instance.

## Tipi class
Static method | Returns | Description
--------------|---------|------------
`Tipi.create(String email, String reference, BigDecimal amount)` | `Tipi` | Creates a Tipi transaction
`Tipi.read(String transactionId)` | `Tipi` | Finds a Tipi transaction via its identifier
`Tipi.read(HttpServletRequest request)` | `Tipi` | Finds a Tipi transaction via an HttpServletRequest containing an idop parameter 
`Tipi.getParameters(String transactionId)` | `ParametresPaiementTipi` | Gets the parameters of a transaction via its identifier. Shortcut for `Tipi.read(transactionId).getParameters()`

Instance method | Returns | Description
----------------|---------|------------
`tipiInstance.getDate()` | `java.util.Date` | Returns the transaction date
`tipiInstance.getIdentifier()` | `String` | Returns the transaction id
`tipiInstance.getLink()` | `String` | Returns the url to tipi service to perform the transaction
`tipi.getParameters()` | `ParametresPaiementTipi` | Returns the transaction parameters
`tipi.isPaymentSuccess()` | `Boolean` | Returns true if the transaction succeeded
`tipi.isPaymentDenied()` | `Boolean` | Returns true if the transaction has been denied
`tipi.isPaymentCancelled()` | `Boolean` | Returns true if the transaction has been cancelled
`tipi.process()` | `Tipi` | Calls the TipiProcessor instance to handle transaction status

## TipiProcessor class
Method | Description
-------|------------
`void paymentSuccess( Tipi tipi )` | Called by `tipiInstance.process()` method (or daemon) if the transaction succeeded
`void paymentDenied( Tipi tipi )` | Called by `tipiInstance.process()` method (or daemon) if the transaction has been denied
`void paymentCancelled( Tipi tipi )` | Called by `tipiInstance.process()` method (or daemon) if the transaction has been cancelled
`List<String> getPendingTransactions( )` | Called by the daemon to get a list of pending transactions identifiers. This method must return a list of identifiers that was previously retrieved by the `tipiInstance.getIdentifier()` method.
