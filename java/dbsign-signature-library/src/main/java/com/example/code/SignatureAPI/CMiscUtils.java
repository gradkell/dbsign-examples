/* ----------------------------------------------------------------------------
 * DO NOT REMOVE THIS DISCLAIMER:
 *
 * This software is provided as example code only! It is not part of the DBsign
 * Security Suite, nor is it supported or maintained by Gradkell Systems, Inc.
 * This software may be published as example code to help other people.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ---------------------------------------------------------------------------- */

package com.example.code.SignatureAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * This is a catch all for various utility functions. Everything from string
 * operations, to base64 encoding, to hash functions.
 */
public class CMiscUtils
{
  private CMiscUtils()
  {
  }

  /**
   * Joins a list of strings into a delimited list string.
   *
   * @param listStrings
   * @param strDelim
   * @return
   */
  public static String join( List<String> listStrings, String strDelim )
  {
    return CMiscUtils.joinPrefix( listStrings, strDelim, null );
  }

  /**
   * Joins a list of strings into a delimited list with am optional prefix for each list item.
   *
   * @param listStrings
   * @param strDelim
   * @param strPrefix
   * @return
   */
  public static String joinPrefix( List<String> listStrings,
                                   String strDelim,
                                   String strPrefix )
  {
    String strReturn = null;
    if ( (listStrings != null) && (strDelim != null) )
    {
      if ( listStrings.size() == 1 )
      {
        strReturn = listStrings.get( 0 );
      }
      else
      {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for ( String item: listStrings )
        {
          if ( first )
          {
            first = false;
          }
          else
          {
            sb.append( strDelim );
          }
          if ( !CMiscUtils.isStringEmpty( strPrefix ) )
          {
            sb.append( strPrefix );
          }
          sb.append( item );
        }
        strReturn = sb.toString();
      }
    }
    return strReturn;

  }

  /**
   * Base64 decode a base64 encoded string to a byte array.
   *
   * @param strB64
   * @return
   */
  public static byte[] base64Decode( String strB64 )
  {
    byte[] baReturn = null;

    if ( strB64 != null )
    {
      // DatatypeConverter works in Java 7. For Java 8 and higher use Base64 class
      // baReturn = DatatypeConverter.parseBase64Binary( strB64 );
      baReturn = Base64.getDecoder().decode( strB64 );
    }

    return baReturn;
  }

  /**
   * Base64 decode a base64 encoded string to a byte array.
   *
   * @param strB64
   * @return
   */
  public static String base64DecodeString( String strB64 )
  {
    String strReturn = null;

    if ( strB64 != null )
    {
      // DatatypeConverter works in Java 7. For Java 8 and higher use Base64 class
      // baReturn = DatatypeConverter.parseBase64Binary( strB64 );
      byte[] baDecoded = Base64.getDecoder().decode( strB64 );
      strReturn = new String( baDecoded );
    }

    return strReturn;
  }

  /**
   * Base64 encode a byte array
   *
   * @param baBytes
   * @return
   */
  public static String base64Encode( byte[] baBytes )
  {
    String strReturn = null;

    if ( baBytes != null )
    {
      // DatatypeConverter works in Java 7. For Java 8 and higher use Base64 class
      // strReturn = DatatypeConverter.printBase64Binary( baBytes );
      strReturn = Base64.getEncoder().encodeToString( baBytes );
    }

    return strReturn;
  }

  /**
   * Base64 encode a String
   *
   * @param strIn
   * @return
   */
  public static String base64Encode( String strIn )
  {
    String strReturn = null;

    if ( strIn != null )
    {
      strReturn = CMiscUtils.base64Encode( strIn.getBytes() );
    }

    return strReturn;
  }

  /**
   * Decode a form-url-encoded string to a map
   *
   * @param strEncoded
   * @return
   */
  public static Map<String, String> formDecode( String strEncoded )
  {
    Map<String, String> mapReturn = null;

    if ( strEncoded != null )
    {
      mapReturn = new HashMap<>();
      String strPairDelim = "&";
      String strValueDelim = "=";

      mapReturn.clear();

      // to take care of netscape
      if ( strEncoded.endsWith( "\n" ) )
      {
        strEncoded = strEncoded.substring( 0, strEncoded.lastIndexOf( "\n" ) );
      }

      if ( strEncoded.endsWith( "\r" ) )
      {
        strEncoded = strEncoded.substring( 0, strEncoded.lastIndexOf( "\r" ) );
      }

      // This tokenizer gets "name=value" strings from "name=value&name=value&..."
      // Young Dogs: Sure, StringTokenizer is old and frumpy, but it is
      // actually faster than String.split()!
      StringTokenizer nvpTokenizer = new StringTokenizer( strEncoded,
                                                          strPairDelim );

      while ( nvpTokenizer.hasMoreTokens() )
      {
        String strNameValPair = nvpTokenizer.nextToken();

        // This tokenizer gets "name" and "value" from "name=value"
        StringTokenizer nvTokenizer = new StringTokenizer( strNameValPair,
                                                           strValueDelim );

        /* if ( nvTokenizer.countTokens() != 2 ) { continue; }
         *
         * String strName = nvTokenizer.nextToken(); String strValue = nvTokenizer.nextToken(); */
        String strName = null;
        String strValue = null;

        if ( nvTokenizer.hasMoreTokens() )
        {
          strName = nvTokenizer.nextToken();
        }
        if ( nvTokenizer.hasMoreTokens() )
        {
          strValue = nvTokenizer.nextToken();
        }

        if ( strValue == null )
        {
          strValue = "";
        }

        if ( strName != null )
        {
          try
          {
            mapReturn.put( URLDecoder.decode( strName,
                                              StandardCharsets.UTF_8.name() ),
                           URLDecoder.decode( strValue,
                                              StandardCharsets.UTF_8.name() ) );
          }
          catch ( UnsupportedEncodingException e )
          {

          }
        }
      } // end while
    }

    return mapReturn;
  }

  /**
   * Encode a map to a form-url-encoded string
   *
   * @param mapFormData
   * @return
   */
  public static String formEncode( Map<String, String> mapFormData )
  {
    String strReturn = null;

    if ( mapFormData != null )
    {
      StringBuffer bufReturn = new StringBuffer();
      for ( Entry<String, String> entry: mapFormData.entrySet() )
      {
        if ( bufReturn.length() > 0 )
        {
          bufReturn.append( '&' );
        }

        try
        {
          bufReturn.append( URLEncoder.encode( entry.getKey(),
                                               StandardCharsets.UTF_8.name() ) );
          bufReturn.append( '=' );
          bufReturn.append( URLEncoder.encode( entry.getValue(),
                                               StandardCharsets.UTF_8.name() ) );
        }
        catch ( UnsupportedEncodingException e )
        {

        }
      }
      strReturn = bufReturn.toString();
    }

    return strReturn;
  }

  /**
   * Helper function to get signer subject DN from verify signature DBsign Server response
   *
   * @param strDn
   * @return
   */
  public static String getCnFromDn( String strDn )
  {
    String strReturn = null;

    if ( strDn != null )
    {
      String[] arrParts = strDn.split( "," );
      for ( String strPart: arrParts )
      {
        if ( strPart.toLowerCase().startsWith( "cn=" ) )
        {
          String[] arrCnSplit = strPart.split( "=", 2 );
          if ( arrCnSplit.length != 2 )
          {
            strReturn = strPart;
            break;
          }
          else
          {
            strReturn = arrCnSplit[1];
            break;
          }
        }
      }
    }

    return strReturn;
  }

  /**
   * Helper function to get the DoD EDIPI from a DoD EE subject DN
   *
   * @param strDn
   * @return
   */
  public static String getEdipiFromDn( String strDn )
  {
    String strReturn = null;

    if ( strDn != null )
    {
      String strCn = CMiscUtils.getCnFromDn( strDn );
      strReturn = "";
      if ( strCn != null )
      {
        String[] arrParts = strCn.split( "\\." );
        if ( arrParts.length > 1 )
        {
          strReturn = arrParts[arrParts.length - 1];
        }
      }
    }

    return strReturn;
  }

  /**
   * Helper function to get a regular name from a DoD EE subject DN
   *
   * @param strDn
   * @return
   */
  public static String getNameFromDn( String strDn )
  {
    String strReturn = null;

    if ( strDn != null )
    {
      String strCn = CMiscUtils.getCnFromDn( strDn );
      strReturn = strCn;
      if ( strCn != null )
      {
        String[] arrParts = strCn.split( "\\." );
        if ( arrParts.length > 1 )
        {
          // Check to make sure last part is an integer like an EDIPI
          String strLastPart = arrParts[arrParts.length - 1];
          boolean bIsInt = false;
          try
          {
            Integer.parseInt( strLastPart );
            bIsInt = true;
          }
          catch ( Exception e )
          {
            bIsInt = false;
          }
          if ( bIsInt )
          {
            List<String> listParts = new ArrayList<>( Arrays.asList( arrParts ) );
            listParts.remove( listParts.size() - 1 );
            strReturn = CMiscUtils.join( listParts, "." );
          }
        }
      }
    }

    return strReturn;
  }

  public static String makeRandomId()
  {
    byte[] baRand = CMiscUtils.hashBytes( ("" + Math.random()).getBytes(),
                                          "SHA-256" );
    byte[] baTime = CMiscUtils.hashBytes( (""
                                           + System.currentTimeMillis()).getBytes(),
                                          "SHA-256" );

    byte[] baXor = new BigInteger( baRand ).xor( new BigInteger( baTime ) ).toByteArray();

    return CMiscUtils.hashBytesB64( baXor, "MD5" );
  }

  /**
   * Hash a byte array with specified algorithm and return a byte array hash.
   *
   * Algorithms: MD5, SHA-1, SHA-256, SHA-384, SHA-512
   *
   * @param baInput
   * @param strDigestAlgorithm
   * @return
   */
  public static byte[] hashBytes( byte[] baInput, String strDigestAlgorithm )
  {
    byte[] baDigest = null;
    try
    {
      if ( baInput != null )
      {
        MessageDigest digester = null;
        digester = MessageDigest.getInstance( strDigestAlgorithm );
        baDigest = digester.digest( baInput );
      }
    }
    catch ( NoSuchAlgorithmException e )
    {
      baDigest = null;
    }

    return baDigest;
  }

  /**
   * Hash a byte array with specified algorithm and return hash as a base64 encoded String.
   *
   * Algorithms: MD5, SHA-1, SHA-256, SHA-384, SHA-512
   *
   * @param baInput
   * @param strDigestAlgorithm
   * @return
   */
  public static String hashBytesB64( byte[] baInput, String strDigestAlgorithm )
  {
    return CMiscUtils.base64Encode( CMiscUtils.hashBytes( baInput,
                                                          strDigestAlgorithm ) );
  }

  /**
   * Hash a byte array with SHA-1 and return hash as a base64 encoded String
   *
   * @param baInput
   * @return
   */
  public static String hashSha1B64( byte[] baInput )
  {
    return CMiscUtils.base64Encode( CMiscUtils.hashBytes( baInput, "SHA-1" ) );
  }

  /**
   * Hash a String with SHA-1 and return hash as a base64 encoded String
   *
   * @param strInput
   * @return
   */
  public static String hashSha1B64( String strInput )
  {
    return CMiscUtils.base64Encode( CMiscUtils.hashSha1B64( strInput == null ? null
                                                                             : strInput.getBytes() ) );
  }

  /**
   * Hash a byte array with SHA-256 and return hash as a base64 encoded String
   *
   * @param baInput
   * @return
   */
  public static String hashSha256B64( byte[] baInput )
  {
    return CMiscUtils.base64Encode( CMiscUtils.hashBytes( baInput,
                                                          "SHA-256" ) );
  }

  /**
   * Hash a String with SHA-256 and return hash as a base64 encoded String
   *
   * @param strInput
   * @return
   */
  public static String hashSha256B64( String strInput )
  {
    return CMiscUtils.base64Encode( CMiscUtils.hashSha256B64( strInput == null ? null
                                                                               : strInput.getBytes() ) );
  }

  /**
   * Hash a String with specified algorithm and return hash as a base64 encoded String
   *
   * Algorithms: MD5, SHA-1, SHA-256, SHA-384, SHA-512
   *
   * @param strInput
   * @param strDigestAlgorithm
   * @return
   */
  public static String hashStringB64( String strInput,
                                      String strDigestAlgorithm )
  {
    return CMiscUtils.base64Encode( CMiscUtils.hashBytes( strInput == null ? null
                                                                           : strInput.getBytes(),
                                                          strDigestAlgorithm ) );
  }

  /**
   * Returns true if input string is null or the empty string.
   *
   * @param strVal
   * @return
   */
  public static boolean isStringEmpty( String strVal )
  {
    return (strVal == null) || (strVal.length() <= 0);
  }

  public static boolean stringToBool( String strBool, boolean bDefault )
  {
    boolean bReturn = bDefault;

    if ( !CMiscUtils.isStringEmpty( strBool ) )
    {
      bReturn = (strBool.equalsIgnoreCase( "true" ) || strBool.equalsIgnoreCase(
                                                                                 "Y" )
                 || strBool.equalsIgnoreCase( "Yes" ));
    }

    return bReturn;
  }

  public static byte[] stringToBytes( String strVal )
  {
    byte[] baReturn = null;

    if ( !CMiscUtils.isStringEmpty( strVal ) )
    {
      try
      {
        baReturn = strVal.getBytes( StandardCharsets.UTF_8.name() );
      }
      catch ( UnsupportedEncodingException e )
      {
        baReturn = null;
      }
    }

    return baReturn;
  }

  public static String bytesToString( byte[] baVal )
  {
    String strReturn = null;

    if ( (baVal != null) && (baVal.length > 0) )
    {
      try
      {
        strReturn = new String( baVal, StandardCharsets.UTF_8.name() );
      }
      catch ( UnsupportedEncodingException e )
      {
        strReturn = null;
      }
    }

    return strReturn;
  }

  /**
   * Gets the contents of a File into a byte array.
   *
   * @param file
   * @return
   * @throws IOException
   */
  public static byte[] getBytesFromFile( File file ) throws IOException
  {
    byte[] arrReturn = null;
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream( file );
      byte[] baFile = new byte[(int) file.length()];
      int nRead = fis.read( baFile );
      assert (nRead == file.length());
      arrReturn = baFile;
    }
    finally
    {
      if ( fis != null )
      {
        fis.close();
      }
    }

    return arrReturn;
  }

  /**
   * Writes a byte array to a File.
   *
   * @param file
   * @param baFile
   * @throws IOException
   */
  public static void writeBytesToFile( File file,
                                       byte[] baFile ) throws IOException
  {
    FileOutputStream fos = null;
    try
    {
      fos = new FileOutputStream( file );
      fos.write( baFile );
      fos.flush();
    }
    finally
    {
      if ( fos != null )
      {
        fos.close();
      }
    }
  }

  public static String readFileB64( File file ) throws IOException
  {
    return CMiscUtils.base64Encode( CMiscUtils.getBytesFromFile( file ) );
  }

  public static String readFileText( File file ) throws IOException
  {
    return CMiscUtils.bytesToString( CMiscUtils.getBytesFromFile( file ) );
  }

  public static void sleep( long nMillis )
  {
    try
    {
      Thread.sleep( nMillis );
    }
    catch ( InterruptedException e )
    {
    }
  }

  /**
   * Adapted from https://mkyong.com/java/java-time-elapsed-in-days-hours-minutes-seconds/
   *
   * @param dtStart
   * @param dtStop
   */
  public static String timeDifference( long nStartMs, long nStopMs )
  {
    String strReturn = "";
    // 1 minute = 60 seconds
    // 1 hour = 60 x 60 = 3600
    // 1 day = 3600 x 24 = 86400

    // milliseconds
    long different = nStopMs - nStartMs;

    long secondsInMilli = 1000;
    long minutesInMilli = secondsInMilli * 60;
    long hoursInMilli = minutesInMilli * 60;
    long daysInMilli = hoursInMilli * 24;

    long elapsedDays = different / daysInMilli;
    different = different % daysInMilli;

    long elapsedHours = different / hoursInMilli;
    different = different % hoursInMilli;

    long elapsedMinutes = different / minutesInMilli;
    different = different % minutesInMilli;

    long elapsedSeconds = different / secondsInMilli;
    different = different % secondsInMilli;

    strReturn = String.format( "%d days, %d hours, %d minutes, %d.%d seconds%n",
                               elapsedDays,
                               elapsedHours,
                               elapsedMinutes,
                               elapsedSeconds,
                               different );
    return strReturn;

  }

  public static String getUriPath( String strURI )
  {
    String strReturn = null;

    if ( strURI != null )
    {
      try
      {
        URI uri = new URI( strURI );
        strReturn = uri.getPath();
      }
      catch ( URISyntaxException e )
      {
        strReturn = null;
      }
    }
    return strReturn;
  }

  public static String getFileExt( String strFile )
  {
    String strReturn = null;

    if ( strFile != null )
    {
      String strName = new File( strFile ).getName();

      int nNdxLastDot = strName.lastIndexOf( "." );
      if ( nNdxLastDot >= 0 )
      {
        strReturn = strName.substring( nNdxLastDot + 1 );
      }
    }

    return strReturn;
  }

  public static void main( String[] argv )
  {
    System.out.println( StandardCharsets.UTF_8.name() );
    System.out.println( CMiscUtils.timeDifference( (long) (Math.random()
                                                           * 100000.0),
                                                   (long) (Math.random()
                                                           * 1000000000.0) ) );

    long nStart = System.currentTimeMillis();
    String strRandomId = CMiscUtils.makeRandomId();
    System.out.println( "CMiscUtils.makeRandomId() time "
                        + (System.currentTimeMillis() - nStart) );
    System.out.println( strRandomId );

  }

}
