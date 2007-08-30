/***************************************************************************
 *   Copyright (C) by Fabrizio Montesi                                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/

package jolie.net.http;

import java.io.IOException;
import java.io.InputStream;

import jolie.lang.parse.Scanner;

public class HTTPScanner extends Scanner
{
	public HTTPScanner( InputStream stream, String sourceName )
		throws IOException
	{
		super( stream, sourceName );
	}
	
	public Token getToken()
		throws IOException
	{
		state = 1;
		
		char prev;
		while ( currByte != -1 && isSeparator( ch ) ) {
			prev = ch;
			readChar();
			if ( isNewLine( prev ) && isNewLine( ch ) )
				return new Token( TokenType.EOF );
		}
		
		if ( currByte == -1 )
			return new Token( TokenType.EOF );
		
		boolean stopOneChar = false;
		Token retval = null;
		String str = new String();

		while ( currByte != -1 && retval == null ) {
			switch( state ) {
				/* When considering multi-characters tokens (states > 1),
				 * remember to read another character in case of a
				 * specific character (==) check.
				 */

				case 1:	// First character
					if ( Character.isLetter( ch ) )
						state = 2;
					else if ( Character.isDigit( ch ) )
						state = 3;
					else if ( ch == '"' )
						state = 4;
					else if ( ch == '+' )
						state = 5;
					else if ( ch == '=' )
						state = 6;
					else if ( ch == '|' )
						state = 7;
					else if ( ch == '&' )
						state = 8;
					else if ( ch == '<' )
						state = 9;
					else if ( ch == '>' )
						state = 10;
					else if ( ch == '!' )
						state = 11;
					else if ( ch == '/' )
						state = 12;
					else if ( ch == '-' )
						state = 14;
					else {	// ONE CHARACTER TOKEN
						if ( ch == '(' )							
							retval = new Token( TokenType.LPAREN );
						else if ( ch == ')' )
							retval = new Token( TokenType.RPAREN );
						else if ( ch == '[' )
							retval = new Token( TokenType.LSQUARE );
						else if ( ch == ']' )
							retval = new Token( TokenType.RSQUARE );
						else if ( ch == '{' )
							retval = new Token( TokenType.LCURLY );
						else if ( ch == '}' )
							retval = new Token( TokenType.RCURLY );
						else if ( ch == '*' )
							retval = new Token( TokenType.ASTERISK );
						else if ( ch == '@' )
							retval = new Token( TokenType.AT );
						else if ( ch == ':' )
							retval = new Token( TokenType.COLON );
						else if ( ch == ',' )
							retval = new Token( TokenType.COMMA );
						else if ( ch == ';' )
							retval = new Token( TokenType.SEQUENCE );
						else if ( ch == '.' )
							retval = new Token( TokenType.DOT );
						
						readChar();
					}
					
					break;
				case 2:	// ID
					if ( !Character.isLetterOrDigit( ch ) &&
							ch != '_' &&
							ch != '-' &&
							ch != '+' ) {
						/*if ( "OneWay".equals( str ) )
							retval = new Token( TokenType.OP_OW );
						else if ( "RequestResponse".equals( str ) )
							retval = new Token( TokenType.OP_RR );
						else if ( "Notification".equals( str ) )
							retval = new Token( TokenType.OP_N );
						else if ( "SolicitResponse".equals( str ) )
							retval = new Token( TokenType.OP_SR );
						else if ( "linkIn".equals( str ) )
							retval = new Token( TokenType.LINKIN );
						else if ( "linkOut".equals( str ) )
							retval = new Token( TokenType.LINKOUT );
						else if ( "if".equals( str ) )
							retval = new Token( TokenType.IF );
						else if ( "else".equals( str ) )
							retval = new Token( TokenType.ELSE );
						else if ( "in".equals( str ) )
							retval = new Token( TokenType.IN );
						else if ( "out".equals( str ) )
							retval = new Token( TokenType.OUT );
						else if ( "and".equals( str ) )
							retval = new Token( TokenType.AND );
						else if ( "or".equals( str ) )
							retval = new Token( TokenType.OR );
						else if ( "locations".equals( str ) )
							retval = new Token( TokenType.LOCATIONS );
						else if ( "operations".equals( str ) )
							retval = new Token( TokenType.OPERATIONS );
						else if ( "include".equals( str ) )
							retval = new Token( TokenType.INCLUDE );
						else if ( "main".equals( str ) )
							retval = new Token( TokenType.MAIN );
						else if ( "define".equals( str ) )
							retval = new Token( TokenType.DEFINE );
						else if ( "links".equals( str ) )
							retval = new Token( TokenType.LINKS );
						else if ( "nullProcess".equals( str ) )
							retval = new Token( TokenType.NULL_PROCESS );
						else if ( "while".equals( str ) )
							retval = new Token( TokenType.WHILE );
						else if ( "sleep".equals( str ) )
							retval = new Token( TokenType.SLEEP );
						else if ( "int".equals( str ) )
							retval = new Token( TokenType.VAR_TYPE_INT );
						else if ( "string".equals( str ) )
							retval = new Token( TokenType.VAR_TYPE_STRING );
						else if ( "variant".equals( str ) )
							retval = new Token( TokenType.VAR_TYPE_VARIANT );
						else if ( "cset".equals( str ) )
							retval = new Token( TokenType.CSET );
						else if ( "persistent".equals( str ) )
							retval = new Token( TokenType.PERSISTENT );
						else if ( "not_persistent".equals( str ) )
							retval = new Token( TokenType.NOT_PERSISTENT );
						else if ( "single".equals( str ) )
							retval = new Token( TokenType.SINGLE );
						else if ( "concurrent".equals( str ) )
							retval = new Token( TokenType.CONCURRENT );
						else if ( "sequential".equals( str ) )
							retval = new Token( TokenType.SEQUENTIAL );
						else if ( "state".equals( str ) )
							retval = new Token( TokenType.STATE );
						else if ( "execution".equals( str ) )
							retval = new Token( TokenType.EXECUTION );
						else if ( "installFH".equals( str ) )
							retval = new Token( TokenType.INSTALL_FAULT_HANDLER );
						else if ( "installComp".equals( str ) )
							retval = new Token( TokenType.INSTALL_COMPENSATION );
						else if ( "throw".equals( str ) )
							retval = new Token( TokenType.THROW );
						else if ( "scope".equals( str ) )
							retval = new Token( TokenType.SCOPE );
						else if ( "comp".equals( str ) )
							retval = new Token( TokenType.COMPENSATE );
						else if ( "exit".equals( str ) )
							retval = new Token( TokenType.EXIT );
						else if ( "constants".equals( str ) )
							retval = new Token( TokenType.CONSTANTS );
						else*/
						retval = new Token( TokenType.ID, str );
					}
					break;	
				case 3: // INT
					if ( !Character.isDigit( ch ) )
						retval = new Token( TokenType.INT, str );
					break;
				case 4:	// STRING
					if ( ch == '"' ) {
						retval = new Token( TokenType.STRING, str.substring( 1 ) );
						readChar();
					} else if ( ch == '\\' ) { // Parse special characters
						readChar();
						if ( ch == '\\' )
							str += '\\';
						else if ( ch == 'n' )
							str += '\n';
						else if ( ch == 't' )
							str += '\t';
						else if ( ch == '"' )
							str += '"';
						else
							throw new IOException( "malformed string: bad \\ usage" );
						
						stopOneChar = true;
						readChar();
					}
					break;		
				case 5:	// PLUS OR CHOICE
					if ( ch == '+' ) {
						retval = new Token( TokenType.CHOICE );
						readChar();
					} else
						retval = new Token( TokenType.PLUS );
					break;
				case 6:	// ASSIGN OR EQUAL
					if ( ch == '=' ) {
						retval = new Token( TokenType.EQUAL );
						readChar();
					} else
						retval = new Token( TokenType.ASSIGN );
					break;
				case 7:	// PARALLEL OR LOGICAL OR
					if ( ch == '|' ) {
						retval = new Token( TokenType.OR );
						readChar();
					} else
						retval = new Token( TokenType.PARALLEL );
					break;
				case 8:	// LOGICAL AND
					if ( ch == '&' ) {
						retval = new Token( TokenType.AND );
						readChar();
					}
					break;
				case 9: // LANGLE OR MINOR_OR_EQUAL
					if ( ch == '=' ) {
						retval = new Token( TokenType.MINOR_OR_EQUAL );
						readChar();
					} else
						retval = new Token( TokenType.LANGLE );
					break;
				case 10: // RANGLE OR MINOR_OR_EQUAL
					if ( ch == '=' ) {
						retval = new Token( TokenType.MAJOR_OR_EQUAL );
						readChar();
					} else
						retval = new Token( TokenType.RANGLE );
					break;
				case 11: // NOT OR NOT_EQUAL
					if ( ch == '=' ) {
						retval = new Token( TokenType.NOT_EQUAL );
						readChar();
					} else
						retval = new Token( TokenType.NOT );
					break;
				case 12: // DIVIDE OR BEGIN_COMMENT OR LINE_COMMENT
					if ( ch == '*' ) {
						state = 13;
						readChar();
					} else if ( ch == '/' )  {
						state = 15;
						readChar();
					} else
						retval = new Token( TokenType.DIVIDE );
					break;
				case 13: // WAITING FOR END_COMMENT
					if ( ch == '*' ) {
						readChar();
						stopOneChar = true;
						if ( ch == '/' ) {
							readChar();
							retval = getToken();
						}
					}
					break;
				case 14: // MINUS OR (negative) INT
					if ( Character.isDigit( ch ) )
						state = 3;
					else
						retval = new Token( TokenType.MINUS );
					break;
				case 15: // LINE_COMMENT: waiting for end of line
					if ( ch == '\n' ) {
						readChar();
						retval = getToken();
					}
					break;
				default:
					retval = new Token( TokenType.ERROR );
					break;
			}
			
			if ( retval == null ) {
				if ( stopOneChar )
					stopOneChar = false;
				else {
					str += ch;
					readChar();
				}
			}
		}

		if ( retval == null )
			retval = new Token( TokenType.ERROR );
		
		return retval;
	}
}

