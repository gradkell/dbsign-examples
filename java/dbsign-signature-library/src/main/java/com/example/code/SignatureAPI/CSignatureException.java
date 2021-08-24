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

/**
 * Exception class that also can have a CSignatureResult in it.
 */
public class CSignatureException extends Exception
{
  private static final long  serialVersionUID = 1L;
  protected CSignatureResult result           = null;

  public CSignatureException()
  {
    super();
  }

  public CSignatureException( String message )
  {
    super( message );
  }

  public CSignatureException( Throwable cause )
  {
    super( cause );
  }

  public CSignatureException( String message, Throwable cause )
  {
    super( message, cause );
  }

  public CSignatureException( String message, CSignatureResult result )
  {
    super( message );
    this.result = result;
  }

  public CSignatureException( Throwable cause, CSignatureResult result )
  {
    super( cause );
    this.result = result;
  }

  public CSignatureException( String message,
                             Throwable cause,
                             CSignatureResult result )
  {
    super( message, cause );
    this.result = result;
  }

  public CSignatureResult getSignatureResult()
  {
    return this.result;
  }

  public void setSignatureResult( CSignatureResult result )
  {
    this.result = result;
  }

}
