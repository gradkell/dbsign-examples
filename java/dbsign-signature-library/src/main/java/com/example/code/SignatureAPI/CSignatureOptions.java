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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class fore all Signature options. These classes allow the various
 * options to be set and they create the request maps that make the POST body's
 * of the requests to the DBsign Server.
 * 
 */
public class CSignatureOptions implements Cloneable
{
  public enum ContentType
  {
    APP_SIGN_SAS, APP_VERIFY, PDF_SIGN, MAKESIG_SAS, CHECK_SIG
  }

  public enum SasAuthType
  {
    CERT, USERID
  }

  public enum EncFmt
  {
    TEXT, BASE64, DTBS_ID
  }

  protected Map<String, String>           mapOtherParams  = null;
  protected CSignatureOptions.ContentType contentType     = null;
  protected String                        strInstanceName = null;
  protected SasAuthType                   sasAuthType     = null;
  protected String                        strSasAuthValue = null;

  public CSignatureOptions()
  {
  }

  public CSignatureOptions( CSignatureOptions.ContentType contentType )
  {
    this.setContentType( contentType );
  }

  public CSignatureOptions.ContentType getContentType()
  {
    return this.contentType;
  }

  public void setContentType( CSignatureOptions.ContentType contentType )
  {
    this.contentType = contentType;
  }

  public String getInstanceName()
  {
    return this.strInstanceName;
  }

  public void setInstanceName( String strInstanceName )
  {
    this.strInstanceName = strInstanceName;
  }

  public SasAuthType getSasAuthType()
  {
    return this.sasAuthType;
  }

  public void setSasAuthType( SasAuthType contentType )
  {
    this.sasAuthType = contentType;
  }

  public String getSasAuthValue()
  {
    return this.strSasAuthValue;
  }

  public void setSasAuthValue( String strSasAuthValue )
  {
    this.strSasAuthValue = strSasAuthValue;
  }

  public void setSasUserId( String strUserId )
  {
    this.setSasAuthType( SasAuthType.USERID );
    this.setSasAuthValue( strUserId );
  }

  public void setSasCert( String strCertB64 )
  {
    this.setSasAuthType( SasAuthType.CERT );
    this.setSasAuthValue( strCertB64 );
  }

  public Map<String, String> getOtherParams()
  {
    return this.mapOtherParams;
  }

  public void setOtherParams( Map<String, String> mapOtherParams )
  {
    this.mapOtherParams = mapOtherParams;
  }

  byte[] decode( String strVal, EncFmt fmt )
  {
    byte[] baReturn = null;

    if ( !CMiscUtils.isStringEmpty( strVal ) )
    {
      switch ( fmt )
      {
        case TEXT:
        {
          baReturn = CMiscUtils.stringToBytes( strVal );
          break;
        }
        case BASE64:
        {
          baReturn = CMiscUtils.base64Decode( strVal );
          break;
        }
        case DTBS_ID:
        {
          baReturn = null;
        }
      }
    }

    return baReturn;
  }

  public Map<String, String> toRequestMap() throws CSignatureException
  {
    Map<String, String> mapReturn = new HashMap<>();

    CSignatureOptions.ContentType contentTypeParam = this.getContentType();
    if ( this.contentType != null )
    {
      mapReturn.put( "CONTENT_TYPE", contentTypeParam.toString() );
    }
    else
    {
      throw new CSignatureException( "No content type specified" );
    }

    String strInstanceNameParam = this.getInstanceName();
    if ( !CMiscUtils.isStringEmpty( strInstanceNameParam ) )
    {
      mapReturn.put( "INSTANCE_NAME", strInstanceNameParam );
    }

    SasAuthType sasAuthTypeParam = this.getSasAuthType();
    if ( sasAuthTypeParam != null )
    {
      mapReturn.put( "DBS_SAS_AUTH_TYPE", sasAuthTypeParam.toString() );

      String strSasAuthValueParam = this.getSasAuthValue();
      if ( !CMiscUtils.isStringEmpty( strSasAuthValueParam ) )
      {
        mapReturn.put( "DBS_SAS_AUTH_VALUE", strSasAuthValueParam );
      }

      switch ( sasAuthTypeParam )
      {
        case CERT:
        {
          mapReturn.put( "DBS_SAS_AUTH_VALUE_FMT",
                         CSignatureOptions.EncFmt.BASE64.toString() );
          break;
        }
        case USERID:
        {
          mapReturn.put( "DBS_SAS_AUTH_VALUE_FMT",
                         CSignatureOptions.EncFmt.TEXT.toString() );
          break;
        }
      }
    }

    mapReturn.put( "DBS_RETURN_SIGNER_INFO", "true" );
    mapReturn.put( "DBS_RETURN_CERT_INFO", "true" );
    mapReturn.put( "DBS_RETURN_CERTS", "true" );

    Map<String, String> mapOtherParamsParam = this.getOtherParams();
    if ( (mapOtherParamsParam != null) && (mapOtherParamsParam.size() > 0) )
    {
      for ( Entry<String, String> entry: mapOtherParamsParam.entrySet() )
      {
        String strKey = entry.getKey();
        String strVal = entry.getValue();
        if ( !CMiscUtils.isStringEmpty( strKey ) && !CMiscUtils.isStringEmpty(
                                                                               strVal ) )
        {
          if ( !mapReturn.containsKey( strKey ) )
          {
            mapReturn.put( strKey, strVal );
          }
        }
      }
    }

    return mapReturn;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

}
