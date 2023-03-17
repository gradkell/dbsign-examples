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
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Signature options for Template signatures.
 * 
 */
public class CSignatureOptionsTemplate extends CSignatureOptions
{
  protected String strTemplateName = "";
  protected Map<String, String> mapPrimaryKeys = new TreeMap<>();
  
	public CSignatureOptionsTemplate() {
		super(ContentType.MAKESIG_SAS);
	}
	
	public void setTemplateName(String strTemplateName)
	{
	  this.strTemplateName = strTemplateName;
	}
	
	public String getTemplateName()
	{
	  return this.strTemplateName;
	}
	
	public void addPrimaryKey(String strPkName, String strPkValue)
	{
	  if(!CMiscUtils.isStringEmpty(strPkName) && !CMiscUtils.isStringEmpty(strPkValue))
	  {
	    this.mapPrimaryKeys.put(strPkName, strPkValue);
	  }
	}
	
	public void resetPrimaryKeys()
	{
	  this.mapPrimaryKeys.clear();
	}
	
	public Map<String, String> getPrimaryKeyMap()
	{
	  return new TreeMap<String, String>(this.mapPrimaryKeys);
	}
	
	@Override
  public Map<String, String> toRequestMap() throws CSignatureException
  {
    Map<String, String> mapReturn = super.toRequestMap();
    
    String strPkPrefix = ":";
    mapReturn.put("PK_PREFIX", strPkPrefix);
    
    String strPkDelim = ",";
    mapReturn.put("PK_DELIM", strPkDelim);
    
    if(!CMiscUtils.isStringEmpty(this.strTemplateName))
    {
      mapReturn.put("TEMPLATE_NAME", this.strTemplateName);
    }
    
    if( this.mapPrimaryKeys!=null )
    {
      StringBuilder sbPkList = new StringBuilder();
      
      for(Entry<String, String> entry:this.mapPrimaryKeys.entrySet())
      {
        String strPkName = entry.getKey();
        String strPkValue = entry.getValue();
        
        if(!CMiscUtils.isStringEmpty(strPkName) && !CMiscUtils.isStringEmpty(strPkValue))
        {
          mapReturn.put(strPkPrefix+strPkName, strPkValue);
          
          if(sbPkList.length()>0)
          {
            sbPkList.append(strPkDelim);
          }
          
          sbPkList.append(strPkPrefix).append(strPkName);
        }
      }
      
      mapReturn.put("PK_LIST", sbPkList.toString());
    }
    
    return mapReturn;
  }
	
	@Override
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
}
