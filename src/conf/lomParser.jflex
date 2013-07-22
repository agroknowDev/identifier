package net.zettadata.lomParser ;

import java.util.HashSet ;
import java.lang.StringBuilder ;

%%

%class LOMParser
%standalone
%unicode

%{ 
	////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////
	// VARIABLES
	////////////////////////////////////////////////////////////////////////////////
	
	private SimpleLOM simpleLOM ;
	private StringBuilder tmp ;

	
	 	
 	////////////////////////////////////////////////////////////////////////////////
	// SETTERS AND GETTERS
	////////////////////////////////////////////////////////////////////////////////
  	
  	public SimpleLOM getSimpleLOM()
  	{
  		return simpleLOM ;
  	}
	
	////////////////////////////////////////////////////////////////////////////////
	// UTILITY METHODS
	////////////////////////////////////////////////////////////////////////////////


	private String extract( String element )
	{	
		return element.substring(element.indexOf(">") + 1 , element.indexOf("</") );
	}


                                           
%}

%state LOM
%state GENERAL
%state IDENTIFIER
%state TECHNICAL
%%

"<lom"	{
		yybegin(LOM) ;
		simpleLOM = new SimpleLOM() ;
		simpleLOM.setLocations( new HashSet<String>() ) ;
		simpleLOM.setIdentifiers( new HashSet<String>() ) ;
	}

<LOM>	{
			"<general"	
			{
				yybegin( GENERAL ) ;	
			}
			"<technical"
			{
				yybegin( TECHNICAL ) ;	
			}
		}

<GENERAL>	{
				"<identifier"
				{
					yybegin( IDENTIFIER ) ;
					tmp = new StringBuilder() ;
				}
				"</general>"
				{
					yybegin( LOM ) ;
				}
			}

<IDENTIFIER>	{
					"<catalog>".+"</catalog>"
					{
						tmp.append( extract( yytext() ).trim() ) ;
					}
				    "<entry>".+"</entry>"
				    {
				    	tmp.append( extract( yytext() ).trim() ) ;
				    }
				    "</identifier>"
				    {
				    	simpleLOM.getIdentifiers().add( tmp.toString() ) ;
				    	yybegin( GENERAL ) ;
				    }
				}
				
<TECHNICAL>	{
				"<location>".+"</location>"
				{
					simpleLOM.getLocations().add( extract( yytext() ).trim() ) ;
				}
				"</technical>"
				{
					yybegin( LOM ) ;
				}
			}

.|\n {}
