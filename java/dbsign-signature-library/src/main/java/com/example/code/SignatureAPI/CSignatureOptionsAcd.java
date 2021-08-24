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

import java.util.Map;

/**
 * Signature options for Application Constructed Data signatures.
 * 
 */
public class CSignatureOptionsAcd extends CSignatureOptions
{
  protected String                   strDtbsId    = null;
  protected String                   strDtbs      = null;
  protected CSignatureOptions.EncFmt fmtDtbs      = null;
  protected String                   strSignature = null;
  protected CSignatureOptions.EncFmt fmtSignature = null;

  public CSignatureOptionsAcd( ContentType contentType )
  {
    super( contentType );
  }

  public String getDtbsId()
  {
    return this.strDtbsId;
  }

  public void setDtbsId( String strDtbsId )
  {
    this.strDtbsId = strDtbsId;
  }

  public String getDtbs()
  {
    return this.strDtbs;
  }

  public void setDtbs( String strDtbs, CSignatureOptions.EncFmt dtbsFmt )
  {
    this.strDtbs = strDtbs;
    this.fmtDtbs = dtbsFmt;
  }

  public CSignatureOptions.EncFmt getDtbsFmt()
  {
    return this.fmtDtbs;
  }

  public void setDtbsText( String strDtbs )
  {
    this.setDtbs( strDtbs, CSignatureOptions.EncFmt.TEXT );
  }

  public void setDtbsBytes( byte[] baDtbs )
  {
    this.setDtbs( CMiscUtils.base64Encode( baDtbs ),
                  CSignatureOptions.EncFmt.BASE64 );
  }

  public byte[] getDtbsBytes()
  {
    byte[] baReturn = null;

    String strVal = this.getDtbs();
    if ( !CMiscUtils.isStringEmpty( strVal ) )
    {
      baReturn = this.decode( strVal, fmtDtbs );
    }

    return baReturn;
  }

  public String getSignature()
  {
    return this.strSignature;
  }

  public EncFmt getSignatureFmt()
  {
    return this.fmtSignature;
  }

  public void setSignature( String strSignature,
                            CSignatureOptions.EncFmt fmtSignature )
  {
    this.strSignature = strSignature;
    this.fmtSignature = fmtSignature;
  }

  public byte[] getSignatureBytes()
  {
    byte[] baReturn = null;

    String strVal = this.getSignature();
    if ( !CMiscUtils.isStringEmpty( strVal ) )
    {
      baReturn = this.decode( strVal, fmtSignature );
    }

    return baReturn;
  }

  @Override
  public Map<String, String> toRequestMap() throws CSignatureException
  {
    Map<String, String> mapReturn = super.toRequestMap();

    // Set the DTBS from various this
    String strDtbsIdParam = this.getDtbsId();
    if ( !CMiscUtils.isStringEmpty( strDtbsIdParam ) )
    {
      // Use DTBS_ID is specified.
      mapReturn.put( "DBS_DTBS", strDtbsIdParam );
      mapReturn.put( "DBS_DTBS_FMT",
                     CSignatureOptions.EncFmt.DTBS_ID.toString() );
    }
    else
    {
      String strDtbsParam = this.getDtbs();
      if ( !CMiscUtils.isStringEmpty( strDtbsParam ) )
      {
        // Use supplied DTBS
        mapReturn.put( "DBS_DTBS", strDtbsParam );
        mapReturn.put( "DBS_DTBS_FMT", this.fmtDtbs.toString() );
      }
    }

    String strSignatureParam = this.getSignature();
    if ( !CMiscUtils.isStringEmpty( strSignatureParam ) )
    {
      mapReturn.put( "DBS_SIGNATURE", strSignatureParam );
      mapReturn.put( "DBS_SIGNATURE_FMT", this.fmtSignature.toString() );
    }

    return mapReturn;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
