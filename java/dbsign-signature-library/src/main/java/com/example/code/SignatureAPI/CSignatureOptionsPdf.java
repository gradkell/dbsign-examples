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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to holds common signature options for PDF files. These this are converted into
 * request params for the DBsign Server. The names of the members and their getters/setters
 * are self explanatory.
 */
public class CSignatureOptionsPdf extends CSignatureOptionsAcd
{
  public enum LockAction
  {
    INCLUDE, EXCLUDE
  }

  protected String              strSignerName       = null;
  protected String              strPdfSigFieldName  = null;
  protected String              strPdfSignerReason  = null;
  protected boolean             bShowSignerReason   = false;
  protected String              strSignDateFormat   = "MMM d, yyyy HH:mm:ss zz";
  protected String              strSignDateTimezone = "America/New_York";
  protected boolean             bShowSignDate       = true;
  protected Map<String, String> mapFillFields       = null;
  protected List<String>        listLockFields      = null;
  protected LockAction          lockAction          = LockAction.INCLUDE;
  protected boolean             bLockFilledFields   = false;
  protected boolean             bLockAllFields      = false;

  public CSignatureOptionsPdf()
  {
    super( ContentType.PDF_SIGN );
  }

  public String getSignerName()
  {
    return this.strSignerName;
  }

  public void setSignerName( String strSignerName )
  {
    this.strSignerName = strSignerName;
  }

  public String getPdfSigFieldName()
  {
    return this.strPdfSigFieldName;
  }

  public void setPdfSigFieldName( String strPdfSigFieldName )
  {
    this.strPdfSigFieldName = strPdfSigFieldName;
  }

  public String getPdfSignerReason()
  {
    return this.strPdfSignerReason;
  }

  public void setPdfSignerReason( String strPdfSignerReason )
  {
    this.strPdfSignerReason = strPdfSignerReason;
  }

  public boolean getShowSignerReason()
  {
    return this.bShowSignerReason;
  }

  public void setShowSignerReason( boolean bShowSignerReason )
  {
    this.bShowSignerReason = bShowSignerReason;
  }

  public String getSignDateFormat()
  {
    return this.strSignDateFormat;
  }

  public void setSignDateFormat( String strSignDateFormat )
  {
    this.strSignDateFormat = strSignDateFormat;
  }

  public String getSignDateTimezone()
  {
    return this.strSignDateTimezone;
  }

  public void setSignDateTimezone( String strSignDateTimezone )
  {
    this.strSignDateTimezone = strSignDateTimezone;
  }

  public boolean getShowSignDate()
  {
    return this.bShowSignDate;
  }

  public void setShowSignDate( boolean bShowSignDate )
  {
    this.bShowSignDate = bShowSignDate;
  }

  public Map<String, String> getFillFields()
  {
    return this.mapFillFields;
  }

  public void setFillFields( Map<String, String> mapFillFields )
  {
    this.mapFillFields = mapFillFields;
  }

  public List<String> getLockFields()
  {
    return this.listLockFields;
  }

  public void setLockFields( List<String> listLockFields )
  {
    this.listLockFields = listLockFields;
  }

  public boolean getLockFilledFields()
  {
    return this.bLockFilledFields;
  }

  public void setLockFilledFields( boolean bLockFilledFields )
  {
    this.bLockFilledFields = bLockFilledFields;
  }

  public boolean getLockAllFields()
  {
    return this.bLockAllFields;
  }

  public void setLockAllFields( boolean bLockAllFields )
  {
    this.bLockAllFields = bLockAllFields;
  }

  public LockAction getLockAction()
  {
    return this.lockAction;
  }

  public void setLockAction( LockAction lockAction )
  {
    this.lockAction = lockAction;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  /**
   * Convert a CSignatureOptionsPdf to a DBsign Server request map.
   *
   * @return
   */
  @Override
  public Map<String, String> toRequestMap() throws CSignatureException
  {
    Map<String, String> mapReturn = super.toRequestMap();

    // Signer this
    CSignatureUtils.putIfNotEmpty( mapReturn,
                                   "DBS_PDF_SIGNER_NAME",
                                   this.getSignerName() );
    CSignatureUtils.putIfNotEmpty( mapReturn,
                                   "DBS_PDF_SIG_FIELD_NAME",
                                   this.getPdfSigFieldName() );
    CSignatureUtils.putIfNotEmpty( mapReturn,
                                   "DBS_PDF_SIGNER_REASON",
                                   this.getPdfSignerReason() );
    mapReturn.put( "DBS_SHOW_SIGNER_REASON",
                   this.getShowSignerReason() ? "true" : "false" );

    // Sign Date Options
    mapReturn.put( "DBS_SHOW_SIGN_DATE",
                   this.getShowSignDate() ? "true" : "false" );
    CSignatureUtils.putIfNotEmpty( mapReturn,
                                   "DBS_SIGN_DATE_TIMEZONE",
                                   this.getSignDateTimezone() );
    CSignatureUtils.putIfNotEmpty( mapReturn,
                                   "DBS_SIGN_DATE_FORMAT",
                                   this.getSignDateFormat() );

    mapReturn.put( "DBS_FIELDS_DELIM", "," );
    mapReturn.put( "DBS_FIELDS_PREFIX", ":" );

    // Field Locking Options
    mapReturn.put( "DBS_LOCK_FILLED_FIELDS",
                   this.getLockFilledFields() ? "true" : "false" );
    mapReturn.put( "DBS_LOCK_ALL_FIELDS",
                   this.getLockAllFields() ? "true" : "false" );
    mapReturn.put( "DBS_LOCK_FIELDS_ACTION", "" + this.getLockAction() );
    CSignatureUtils.putIfNotEmpty( mapReturn,
                                   "DBS_LOCK_FIELDS",
                                   CMiscUtils.joinPrefix( this.getLockFields(),
                                                          mapReturn.get( "DBS_FIELDS_DELIM" ),
                                                          mapReturn.get( "DBS_FIELDS_PREFIX" ) ) );

    // Field Filling Options
    Map<String, String> mapFillFieldsParam = this.getFillFields();
    if ( (mapFillFieldsParam != null) && (mapFillFieldsParam.size() > 0) )
    {
      List<String> listFillFieldNames = new ArrayList<>();
      for ( Entry<String, String> entry: mapFillFieldsParam.entrySet() )
      {
        if ( (entry.getKey() != null) && (entry.getValue() != null) )
        {
          mapReturn.put( mapReturn.get( "DBS_FIELDS_PREFIX" ) + entry.getKey(),
                         entry.getValue() );
          listFillFieldNames.add( entry.getKey() );
        }
      }
      if ( !listFillFieldNames.isEmpty() )
      {
        mapReturn.put( "DBS_FILL_FIELDS",
                       CMiscUtils.joinPrefix( listFillFieldNames,
                                              mapReturn.get( "DBS_FIELDS_DELIM" ),
                                              mapReturn.get( "DBS_FIELDS_PREFIX" ) ) );
      }
    }

    return mapReturn;
  }

}
