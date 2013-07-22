package gr.agroknow.metadata.identifier;


public class IdentifierException extends java.lang.Exception
{

	private String message ;

	public IdentifierException( String message )
	{
		super() ;
		this.message = message ;
	}

	public String getMessage()
	{
		return message ;
	}

}
