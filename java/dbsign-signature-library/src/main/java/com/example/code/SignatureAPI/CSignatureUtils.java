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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Class with various DBsign specific utility functions.
 */
public class CSignatureUtils
{

  public static final String DEFAULT_STRING = "";
  public static final int    DEFAULT_INT    = -1;

  private CSignatureUtils()
  {
  }

  public static Map<String, String> doValidateCertRequest( String strDBsignServerUrl,
                                                           String strInstanceName,
                                                           String strCertB64,
                                                           Date dtValidate )
  {
    Map<String, String> mapReturn = null;

    Map<String, String> mapFormData = new HashMap<>();

    mapFormData.put( "CONTENT_TYPE", "VALIDATE_CERT" );
    mapFormData.put( "INSTANCE_NAME", strInstanceName );
    mapFormData.put( "DBS_CERT_TO_VALIDATE", strCertB64 );
    mapFormData.put( "DBS_CERT_FORMAT", "BASE64" );

    if ( (dtValidate != null) )
    {
      DateFormat fmtDateDBsign = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
      fmtDateDBsign.setCalendar( new GregorianCalendar( TimeZone.getTimeZone( "UTC" ) ) );
      mapFormData.put( "DBS_VALIDATE_DATE",
                       fmtDateDBsign.format( dtValidate ) );
      mapFormData.put( "DBS_VALIDATE_DATE_GMT_FLAG", "true" );
    }

    mapReturn = CSignatureUtils.doSignatureRequest( strDBsignServerUrl,
                                                    mapFormData );

    return mapReturn;
  }

  public static Map<String, String> doAppVerifyRequest( String strDBsignServerUrl,
                                                        String strInstanceName,
                                                        String strSignatureB64,
                                                        String strDtbs,
                                                        String strDtbsFmt )
  {
    Map<String, String> mapReturn = null;

    Map<String, String> mapFormData = new HashMap<>();

    mapFormData.put( "CONTENT_TYPE", "APP_VERIFY" );
    mapFormData.put( "INSTANCE_NAME", strInstanceName );
    mapFormData.put( "DBS_DTBS", strDtbs );
    mapFormData.put( "DBS_DTBS_FMT", strDtbsFmt );
    mapFormData.put( "DBS_SIGNATURE", strSignatureB64 );
    mapFormData.put( "DBS_SIGNATURE_FMT", "BASE64" );
    mapFormData.put( "DBS_RETURN_SIGNER_INFO", "true" );

    mapReturn = CSignatureUtils.doSignatureRequest( strDBsignServerUrl,
                                                    mapFormData );

    return mapReturn;
  }

  public static Map<String, String> doNotaryAppSignRequest( String strDBsignServerUrl,
                                                            String strInstanceName,
                                                            String strDtbs,
                                                            String strDtbsFmt )
  {
    Map<String, String> mapReturn = null;

    Map<String, String> mapFormData = new HashMap<>();

    mapFormData.put( "CONTENT_TYPE", "APP_SERVER_SIGN" );
    mapFormData.put( "INSTANCE_NAME", strInstanceName );
    mapFormData.put( "DBS_DTBS", strDtbs );
    mapFormData.put( "DBS_DTBS_FMT", strDtbsFmt );
    mapFormData.put( "DBS_RETURN_SIGNER_INFO", "true" );

    mapReturn = CSignatureUtils.doSignatureRequest( strDBsignServerUrl,
                                                    mapFormData );

    return mapReturn;
  }

  /**
   * Does an HTTP transaction to the DBsign Server
   *
   * @param strDBsignServerUrl
   *          The DBsign Server URL
   * @param mapFormData
   *          The POST parameters
   * @return
   */
  public static Map<String, String> doSignatureRequest( String strDBsignServerUrl,
                                                        Map<String, String> mapFormData )
  {
    Map<String, String> mapReturn = null;

    try
    {
      String strResponse = CHttpUtils.doHttpTransactionString( strDBsignServerUrl,
                                                               mapFormData );
      mapReturn = CMiscUtils.formDecode( strResponse );
    }
    catch ( Exception e )
    {
      mapReturn = new HashMap<>();
      mapReturn.put( "DBS_ERROR_VAL", "305" );
      mapReturn.put( "DBS_ERROR_MSG",
                     "Could not communicate with security server: "
                                      + e.toString() );
      mapReturn.put( "ERROR_DESCRIPTION",
                     CSignatureUtils.getStringField( mapReturn,
                                                     "DBS_ERROR_MSG" ) );
    }

    return mapReturn;
  }

  /**
   * Gets an integer value from a string valued map.
   *
   * @param map
   * @param strKey
   * @return
   */
  public static int getIntField( Map<String, String> map, String strKey )
  {
    int nReturn = CSignatureUtils.DEFAULT_INT;

    if ( map != null )
    {
      String strVal = CSignatureUtils.getStringField( map, strKey );
      if ( !CMiscUtils.isStringEmpty( strVal ) )
      {
        try
        {
          nReturn = Integer.parseInt( strVal );
        }
        catch ( Exception e )
        {
          nReturn = CSignatureUtils.DEFAULT_INT;
        }
      }
    }

    return nReturn;
  }

  /**
   * Gets a string value from a map. Just because we have a function for an int.
   *
   * @param map
   * @param strKey
   * @return
   */
  public static String getStringField( Map<String, String> map, String strKey )
  {
    String strReturn = CSignatureUtils.DEFAULT_STRING;

    if ( map != null )
    {
      strReturn = map.get( strKey );
    }

    if ( strReturn == null )
    {
      strReturn = CSignatureUtils.DEFAULT_STRING;
    }

    return strReturn;
  }

  /**
   * Gets an integer error code from a DBsign Server response map.
   *
   * @param mapResponse
   * @return
   */
  public static int getDbsErrorVal( Map<String, String> mapResponse )
  {
    return CSignatureUtils.getIntField( mapResponse, "DBS_ERROR_VAL" );
  }

  /**
   * Gets a error message from a DBsign Server response map.
   *
   * @param mapResponse
   * @return
   */
  public static String getDbsErrorMsg( Map<String, String> mapResponse )
  {
    return CSignatureUtils.getStringField( mapResponse, "DBS_ERROR_MSG" );

  }

  /**
   * Gets an native integer error code from a DBsign Server response map.
   *
   * @param mapResponse
   * @return
   */
  public static int getNativeErrorVal( Map<String, String> mapResponse )
  {
    return CSignatureUtils.getIntField( mapResponse, "NATIVE_ERROR_VAL" );
  }

  /**
   * Gets a native error message from a DBsign Server response map.
   *
   * @param mapResponse
   * @return
   */
  public static String getNativeErrorMsg( Map<String, String> mapResponse )
  {
    return CSignatureUtils.getStringField( mapResponse, "NATIVE_ERROR_MSG" );

  }

  /**
   * Gets a error description from a DBsign Server response map.
   *
   * @param mapResponse
   * @return
   */
  public static String getErrorDesc( Map<String, String> mapResponse )
  {
    return CSignatureUtils.getStringField( mapResponse, "ERROR_DESCRIPTION" );
  }

  /**
   * Gets all the error fields in a string to object map because it parses the Integer fields.
   *
   * @param mapResponse
   * @return
   */
  public static Map<String, Object> getErrorFields( Map<String, String> mapResponse )
  {
    Map<String, Object> mapReturn = new HashMap<>();

    if ( (mapResponse != null) && !mapResponse.isEmpty() )
    {
      for ( String strField: Arrays.asList( "DBS_ERROR_VAL",
                                            "NATIVE_ERROR_VAL" ) )
      {
        int nVal = CSignatureUtils.getIntField( mapResponse, strField );
        if ( nVal != -1 )
        {
          mapReturn.put( strField, nVal );
        }
      }

      for ( String strField: Arrays.asList( "DBS_ERROR_MSG",
                                            "NATIVE_ERROR_MSG",
                                            "ERROR_DESCRIPTION" ) )
      {
        String strVal = CSignatureUtils.getStringField( mapResponse, strField );
        if ( !CMiscUtils.isStringEmpty( strVal ) )
        {
          mapReturn.put( strField, strVal );
        }
      }
    }

    return mapReturn;
  }

  /**
   * Helper function to determines if DBsign request was successful.
   *
   * @param mapResponse
   * @return
   */
  public static boolean isDBsignResponseSuccessful( Map<String, String> mapResponse )
  {
    return CSignatureUtils.getDbsErrorVal( mapResponse ) == 0;
  }

  /**
   * Helper function to get DTBS_ID from DBsign Server response
   *
   * @param mapResponse
   * @return
   */
  public static String getDtbsId( Map<String, String> mapResponse )
  {
    String strReturn = "";

    if ( mapResponse != null )
    {
      strReturn = CSignatureUtils.getStringField( mapResponse, "DBS_DTBS_ID" );
    }

    return strReturn;
  }

  /**
   * Helper function to get DBS_SIGNATURE from DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static String getDbsSignature( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      strReturn = CSignatureUtils.getStringField( mapResponse,
                                                  "DBS_SIGNATURE" );
    }

    return strReturn;
  }

  /**
   * Helper function to get signer userid DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static String getSignerUserId( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo, "USERID" );
      }
    }

    return strReturn;
  }

  /**
   * Helper function to get signer cert from DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static String getSignerCert( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo, "CERT" );
      }
    }

    return strReturn;
  }

  /**
   * Helper function to get signer subject DN from verify signature DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static String getSignerSubjectDn( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "SUBJECT_DN" );
      }
    }

    return strReturn;
  }

  public static String getSignerIssuerDn( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "ISSUER_DN" );
      }
    }

    return strReturn;
  }

  public static String getSignerSerialNum( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "SERIAL_NUM" );
      }
    }

    return strReturn;
  }

  public static String getSignerValidFrom( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "VALID_FROM" );
      }
    }

    return strReturn;
  }

  public static String getSignerValidTo( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo, "VALID_TO" );
      }
    }

    return strReturn;
  }

  /**
   * Helper function to get signer subject DN from verify signature DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static String getNotaryCert( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getNotaryInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo, "CERT" );
      }
    }

    return strReturn;
  }

  /**
   * Helper function to get signer subject DN from verify signature DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static String getNotarySubjectDn( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getNotaryInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "SUBJECT_DN" );
      }
    }

    return strReturn;
  }

  public static String getNotaryIssuerDn( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getNotaryInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "ISSUER_DN" );
      }
    }

    return strReturn;
  }

  public static String getNotarySerialNum( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getNotaryInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "SERIAL_NUM" );
      }
    }

    return strReturn;
  }

  public static String getNotaryValidFrom( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getNotaryInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "VALID_FROM" );
      }
    }

    return strReturn;
  }

  public static String getNotaryValidTo( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      Map<String, String> mapSignerInfo = CSignatureUtils.getNotaryInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo, "VALID_TO" );
      }
    }

    return strReturn;
  }

  public static String getCnFromDn( String strDn )
  {
    String strCn = "";

    if ( !CMiscUtils.isStringEmpty( strDn ) )
    {
      String[] arrDn = strDn.split( "," );

      if ( arrDn != null )
      {
        for ( String str: arrDn )
        {
          if ( (str != null) && str.toLowerCase().startsWith( "cn=" ) )
          {
            String[] arrCn = str.split( "=" );
            strCn = arrCn[1];
            break;
          }
        }
      }
    }

    return strCn;
  }

  public static String getEdipiFromCn( String strCn )
  {
    String strReturn = null;

    if ( CMiscUtils.isStringEmpty( strCn ) )
    {
      String[] arrParts = strCn.split( "\\." );
      if ( arrParts.length > 0 )
      {
        String strLastPart = arrParts[arrParts.length - 1];
        if ( strLastPart.matches( "\\d+" ) )
        {
          strReturn = strLastPart;
        }
      }
    }

    return strReturn;
  }

  /**
   * Helper function to get singing time from verify signature DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static String getSigningTimeGmt( Map<String, String> mapResponse )
  {
    String strReturn = null;

    if ( mapResponse != null )
    {
      // Note, this corrects for a historical bug in the signer info SIGNING_TIME. If
      // DBS_SIGN_DATE and SIGNING_TIME are different, use DBS_SIGN_DATE.
      String strDbsSignDate = mapResponse.get( "DBS_SIGN_DATE" );
      Map<String, String> mapSignerInfo = CSignatureUtils.getSignerInfo( mapResponse );
      if ( mapSignerInfo != null )
      {
        strReturn = CSignatureUtils.getStringField( mapSignerInfo,
                                                    "SIGNING_TIME" );
      }
      if ( !CMiscUtils.isStringEmpty( strDbsSignDate )
           && !CMiscUtils.isStringEmpty( strReturn )
           && (!strReturn.equalsIgnoreCase( strDbsSignDate )) )
      {
        strReturn = strDbsSignDate;
      }
    }

    return strReturn;
  }

  /**
   * Little helper function to extract fields from map minus the tag (key format: "tag_field").
   * This is used when parsing a DBsign Server.
   *
   * @param mapIn
   * @param mapOut
   * @param strTag
   * @param strField
   */
  private static void mapSetIfSetNoTag( Map<String, String> mapIn,
                                        Map<String, String> mapOut,
                                        String strTag,
                                        String strField )
  {
    if ( (mapIn != null) && (mapOut != null) && (!CMiscUtils.isStringEmpty(
                                                                            strTag ))
         && (!CMiscUtils.isStringEmpty( strField )) )
    {

      String strVal = CSignatureUtils.getStringField( mapIn,
                                                      strTag + "_" + strField );

      if ( !CMiscUtils.isStringEmpty( strVal ) )
      {
        mapOut.put( strField, strVal );
      }
    }
  }

  /**
   * Helper function to get signer info from DBsign Server response.
   *
   * @param mapResponse
   * @return
   */
  public static Map<String, String> getSignerInfo( Map<String, String> mapResponse )
  {
    Map<String, String> mapReturn = null;

    if ( mapResponse != null )
    {
      mapReturn = new HashMap<>();
      String strTagSignerInfo = CSignatureUtils.getStringField( mapResponse,
                                                                "DBS_SIGNER_INFO" );
      if ( !CMiscUtils.isStringEmpty( strTagSignerInfo ) )
      {
        CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                          mapReturn,
                                          strTagSignerInfo,
                                          "SIGNING_TIME" );
        CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                          mapReturn,
                                          strTagSignerInfo,
                                          "SIGNING_TIME_GMT" );
        CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                          mapReturn,
                                          strTagSignerInfo,
                                          "USERID" );
        String strTagSignerCert = CSignatureUtils.getStringField( mapResponse,
                                                                  strTagSignerInfo
                                                                               + "_CERT" );
        if ( !CMiscUtils.isStringEmpty( strTagSignerCert ) )
        {
          String strSignerCert = CSignatureUtils.getStringField( mapResponse,
                                                                 strTagSignerCert );

          if ( !CMiscUtils.isStringEmpty( strSignerCert ) )
          {
            mapReturn.put( "CERT", strSignerCert );
          }

          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "SUBJECT_DN" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "ISSUER_DN" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "SERIAL_NUM" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_FROM" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_FROM_GMT" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_TO" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_TO_GMT" );
        }
      }
    }

    return mapReturn;
  }

  public static Map<String, String> getNotaryInfo( Map<String, String> mapResponse )
  {
    Map<String, String> mapReturn = null;

    if ( mapResponse != null )
    {
      mapReturn = new HashMap<>();
      String strTagSignerInfo = CSignatureUtils.getStringField( mapResponse,
                                                                "DBS_SIGNER_INFO" );
      if ( !CMiscUtils.isStringEmpty( strTagSignerInfo ) )
      {
        CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                          mapReturn,
                                          strTagSignerInfo,
                                          "SIGNING_TIME" );
        CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                          mapReturn,
                                          strTagSignerInfo,
                                          "SIGNING_TIME_GMT" );
        String strTagSignerCert = CSignatureUtils.getStringField( mapResponse,
                                                                  strTagSignerInfo
                                                                               + "_CERT_NOTARY" );
        if ( !CMiscUtils.isStringEmpty( strTagSignerCert ) )
        {
          String strSignerCert = CSignatureUtils.getStringField( mapResponse,
                                                                 strTagSignerCert );

          if ( !CMiscUtils.isStringEmpty( strSignerCert ) )
          {
            mapReturn.put( "CERT", strSignerCert );
          }

          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "SUBJECT_DN" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "ISSUER_DN" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "SERIAL_NUM" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_FROM" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_FROM_GMT" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_TO" );
          CSignatureUtils.mapSetIfSetNoTag( mapResponse,
                                            mapReturn,
                                            strTagSignerCert,
                                            "VALID_TO_GMT" );
        }
      }
    }

    return mapReturn;
  }

  public static CSignatureResult parseResponse( Map<String, String> mapResponse )
  {
    return new CSignatureResult( mapResponse );
  }

  /**
   * Puts a string value into a map if the string value is not null or empty.
   *
   * @param map
   * @param strKey
   * @param strVal
   */
  public static void putIfNotEmpty( Map<String, String> map,
                                    String strKey,
                                    String strVal )
  {
    if ( !CMiscUtils.isStringEmpty( strVal ) )
    {
      map.put( strKey, strVal );
    }
  }

  // public static void main( String[] args )
  // {
  // }
}
