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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

/**
 * Utility class to parse a response from the DBsign Server
 * and make access to the info more easily obtained.
 */
public class CSignatureResult
{
  @SuppressWarnings( "unused" )
  private static final long serialVersionUID         = 1L;
  Map<String, String>       mapResponse              = null;

  int                       nDbsErrorVal             = CSignatureUtils.DEFAULT_INT;
  String                    nDbsErrorMsg             = CSignatureUtils.DEFAULT_STRING;
  int                       nNativeErrorVal          = CSignatureUtils.DEFAULT_INT;
  String                    nNativeErrorMsg          = CSignatureUtils.DEFAULT_STRING;
  String                    strErrorDesc             = CSignatureUtils.DEFAULT_STRING;

  String                    strSignature             = CSignatureUtils.DEFAULT_STRING;

  String                    strSignerUserId          = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerCert            = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerSubjectDn       = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerSubjectCn       = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerEdipi           = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerValidFrom       = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerValidTo         = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerIssuerSubjectDn = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerIssuerSubjectCn = CSignatureUtils.DEFAULT_STRING;
  String                    strSignerSerialNum       = CSignatureUtils.DEFAULT_STRING;

  String                    strNotaryCert            = CSignatureUtils.DEFAULT_STRING;
  String                    strNotarySubjectDn       = CSignatureUtils.DEFAULT_STRING;
  String                    strNotarySubjectCn       = CSignatureUtils.DEFAULT_STRING;
  String                    strNotaryEdipi           = CSignatureUtils.DEFAULT_STRING;
  String                    strNotaryValidFrom       = CSignatureUtils.DEFAULT_STRING;
  String                    strNotaryValidTo         = CSignatureUtils.DEFAULT_STRING;
  String                    strNotaryIssuerSubjectDn = CSignatureUtils.DEFAULT_STRING;
  String                    strNotaryIssuerSubjectCn = CSignatureUtils.DEFAULT_STRING;
  String                    strNotarySerialNum       = CSignatureUtils.DEFAULT_STRING;

  Date                      dtSignDate               = null;

  // Used to parse dates returned by the DBsign Server
  DateFormat                fmtDbsDateGmt            = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

  public CSignatureResult( Map<String, String> mapResponse )
  {
    super();
    this.fmtDbsDateGmt.setCalendar( new GregorianCalendar( TimeZone.getTimeZone( "UTC" ) ) );

    if ( (mapResponse != null) && (!mapResponse.isEmpty()) )
    {
      this.mapResponse = mapResponse;
      this.parseResponseMap();
    }
  }

  public Map<String, String> getMapResponse()
  {
    return this.mapResponse;
  }

  private void parseResponseMap()
  {
    if ( (this.mapResponse == null) || (this.mapResponse.size() <= 0) )
    {
      throw new RuntimeException( "Could not parse null or empty response map" );
    }

    this.nDbsErrorVal = CSignatureUtils.getDbsErrorVal( this.mapResponse );
    this.nDbsErrorMsg = CSignatureUtils.getDbsErrorMsg( this.mapResponse );
    this.nNativeErrorVal = CSignatureUtils.getNativeErrorVal( this.mapResponse );
    this.nNativeErrorMsg = CSignatureUtils.getNativeErrorMsg( this.mapResponse );
    this.strErrorDesc = CSignatureUtils.getErrorDesc( this.mapResponse );

    this.strSignature = CSignatureUtils.getDbsSignature( this.mapResponse );

    this.strSignerUserId = CSignatureUtils.getSignerUserId( this.mapResponse );
    this.strSignerCert = CSignatureUtils.getSignerCert( this.mapResponse );
    this.strSignerSubjectDn = CSignatureUtils.getSignerSubjectDn( this.mapResponse );
    this.strSignerSubjectCn = CSignatureUtils.getCnFromDn( this.strSignerSubjectDn );
    this.strSignerEdipi = CSignatureUtils.getEdipiFromCn( this.strSignerSubjectCn );
    this.strSignerIssuerSubjectDn = CSignatureUtils.getSignerIssuerDn( this.mapResponse );
    this.strSignerIssuerSubjectCn = CSignatureUtils.getCnFromDn( this.strSignerIssuerSubjectDn );
    this.strSignerSerialNum = CSignatureUtils.getSignerSerialNum( this.mapResponse );
    this.strSignerValidFrom = CSignatureUtils.getSignerValidFrom( this.mapResponse );
    this.strSignerValidTo = CSignatureUtils.getSignerValidTo( this.mapResponse );

    this.strNotaryCert = CSignatureUtils.getNotaryCert( this.mapResponse );
    this.strNotarySubjectDn = CSignatureUtils.getNotarySubjectDn( this.mapResponse );
    this.strNotarySubjectCn = CSignatureUtils.getCnFromDn( this.strNotarySubjectDn );
    this.strNotaryEdipi = CSignatureUtils.getEdipiFromCn( this.strNotarySubjectCn );
    this.strNotaryIssuerSubjectDn = CSignatureUtils.getNotaryIssuerDn( this.mapResponse );
    this.strNotaryIssuerSubjectCn = CSignatureUtils.getCnFromDn( this.strNotaryIssuerSubjectDn );
    this.strNotarySerialNum = CSignatureUtils.getNotarySerialNum( this.mapResponse );
    this.strNotaryValidFrom = CSignatureUtils.getNotaryValidFrom( this.mapResponse );
    this.strNotaryValidTo = CSignatureUtils.getNotaryValidTo( this.mapResponse );

    String strSignDateGmt = CSignatureUtils.getSigningTimeGmt( this.mapResponse );
    try
    {
      if ( this.dtSignDate == null )
      {
        this.dtSignDate = this.fmtDbsDateGmt.parse( strSignDateGmt );
      }
    }
    catch ( Exception e )
    {
    }
  }

  public boolean isError()
  {
    return this.nDbsErrorVal != 0;
  }

  public Map<String, Object> getErrorFields()
  {
    return CSignatureUtils.getErrorFields( this.getMapResponse() );
  }

  public boolean isDataOk()
  {
    return (this.nDbsErrorVal >= 0) && (this.nDbsErrorVal != 135);
  }

  public int getDbsErrorVal()
  {
    return this.nDbsErrorVal;
  }

  public String getDbsErrorMsg()
  {
    return this.nDbsErrorMsg;
  }

  public int getNativeErrorVal()
  {
    return this.nNativeErrorVal;
  }

  public String getNativeErrorMsg()
  {
    return this.nNativeErrorMsg;
  }

  public String getErrorDescOrMsg()
  {
    return CMiscUtils.isStringEmpty( this.getErrorDesc() ) ? this.getDbsErrorMsg()
                                                           : this.getErrorDesc();
  }

  public String getErrorDesc()
  {
    return this.strErrorDesc;
  }

  public String getSignature()
  {
    return this.strSignature;
  }

  public void setSignature( String strSignature )
  {
    this.strSignature = strSignature;
  }

  public String getSignerUserId()
  {
    return this.strSignerUserId;
  }

  public String getSignerCert()
  {
    return this.strSignerCert;
  }

  public String getSignerSubjectDn()
  {
    return this.strSignerSubjectDn;
  }

  public String getSignerSubjectCn()
  {
    return this.strSignerSubjectCn;
  }

  public String getSignerEdipi()
  {
    return this.strSignerEdipi;
  }

  public String getSignerValidFrom()
  {
    return this.strSignerValidFrom;
  }

  public String getSignerValidTo()
  {
    return this.strSignerValidTo;
  }

  public String getSignerIssuerSubjectDn()
  {
    return this.strSignerIssuerSubjectDn;
  }

  public String getSignerIssuerSubjectCn()
  {
    return this.strSignerIssuerSubjectCn;
  }

  public String getSignerSerialNum()
  {
    return this.strSignerSerialNum;
  }

  public String getNotaryCert()
  {
    return this.strNotaryCert;
  }

  public String getNotarySubjectDn()
  {
    return this.strNotarySubjectDn;
  }

  public String getNotarySubjectCn()
  {
    return this.strNotarySubjectCn;
  }

  public String getNotaryEdipi()
  {
    return this.strNotaryEdipi;
  }

  public String getNotaryValidFrom()
  {
    return this.strNotaryValidFrom;
  }

  public String getNotaryValidTo()
  {
    return this.strNotaryValidTo;
  }

  public String getNotaryIssuerSubjectDn()
  {
    return this.strNotaryIssuerSubjectDn;
  }

  public String getNotaryIssuerSubjectCn()
  {
    return this.strNotaryIssuerSubjectCn;
  }

  public String getNotarySerialNum()
  {
    return this.strNotarySerialNum;
  }

  public Date getSignDate()
  {
    return this.dtSignDate;
  }

}
