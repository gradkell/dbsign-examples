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

package com.example.code;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.example.code.SignatureAPI.CCanonicalDtbs;

/**
 * Example driver program for the CCanonicalDtbs class.
 *
 */
public class CTestDriverCanonicalDtbs
{
  private CTestDriverCanonicalDtbs()
  {
  }

  public static final void main( String[] args )
  {
    try
    {
      // Setup a canonical DTBS object.
      CCanonicalDtbs root = new CCanonicalDtbs();

      // Put in some random data
      root.setItem( "int", 345 );
      root.setItem( "long", 6540983460923409845L );
      root.setItem( "float", 98542.0956445F );
      root.setItem( "double", -12309845.989819D );
      root.setItem( "date", new Date() );
      root.setItem( "string", "blah blah blah" );

      // Put in a sub-section. Can be nested as far as necessary.
      CCanonicalDtbs items = root.addSection( "items" );

      items.setItem( "computer", 890 );
      items.setItem( "mouse", 30 );
      items.setItem( "keyboard", 50 );
      items.setItem( "monitor", 500 );

      // Make XML from the CCanonicalDtbs object
      String strXml = root.toXml();
      System.out.println( strXml );

      // Parse existing DTBS string back into a CCanonicalDtbs object.
      ByteArrayInputStream is = new ByteArrayInputStream( strXml.getBytes( StandardCharsets.UTF_8 ) );
      CCanonicalDtbs secParsed = CCanonicalDtbs.parse( is );

      // Add a few more items
      secParsed.setItem( "another", "blah" );
      secParsed.setItem( "mallory", "<i n=\"blah\">123</i>" );

      System.out.println( "\n\n" + secParsed.toXml() );

    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }

  }

}
