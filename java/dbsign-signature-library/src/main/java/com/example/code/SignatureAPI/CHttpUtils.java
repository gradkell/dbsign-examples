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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Simple class that uses the Java URL object to do HTTP POST or GET
 * requests.
 */
public class CHttpUtils
{
  private CHttpUtils()
  {
  }

  /**
   * Set the JRE to trust all SSL/TLS certificates.
   * For dev/test environments only!!!
   * INSECURE!!! Do not use in production environments!!!
   */
  public static void trustAllSslCerts() {
    System.out.println("#############################");
    System.out.println("#############################");
    System.out.println("# BYPASSING ALL SSL/TLS CERT CHECKS!!!");
    System.out.println("# This is an INSECURE solution and should only be used in dev/test environments!!!");
    System.out.println("#############################");
    System.out.println("#############################");

    try {
      TrustManager[] trustAllCerts = new TrustManager[] { new X509ExtendedTrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
          return null;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] certs, final String authType)
        {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] certs, final String authType)
        {
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] xcs, final String string, final Socket socket) throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] xcs, final String string, final Socket socket) throws CertificateException
        {
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] xcs, final String string, final SSLEngine ssle) throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] xcs, final String string, final SSLEngine ssle) throws CertificateException
        {
        }
      } };

      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        @Override
        public boolean verify(final String hostname, final SSLSession session) {
          return true;
        }
      };
      
      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Does an HTTP request to some server, returning a byte array
   *
   * @param strUrl
   * @param baRequestData
   * @param mapRequestHeaders
   * @return
   * @throws IOException
   */
  public static byte[] doHttpTransactionBytes( String strUrl,
                                               byte[] baRequestData,
                                               Map<String, String> mapRequestHeaders ) throws IOException
  {

    byte[] baResponseData = null;
    HttpURLConnection urlConn = null;
    InputStream is = null;
    OutputStream os = null;

    try
    {
      boolean bDoOutput = (baRequestData != null) && (baRequestData.length > 0);

      URL url = new URL( strUrl );

      urlConn = (HttpURLConnection) url.openConnection();

      // Set up
      urlConn.setDoOutput( bDoOutput );
      urlConn.setDoInput( true );
      urlConn.setRequestMethod( bDoOutput ? "POST" : "GET" );
      urlConn.setUseCaches( false );
      urlConn.setAllowUserInteraction( false );

      if ( mapRequestHeaders != null )
      {
        for ( Entry<String, String> entryHeader: mapRequestHeaders.entrySet() )
        {
          urlConn.setRequestProperty( entryHeader.getKey(),
                                      entryHeader.getValue() );
        }
      }

      if ( bDoOutput )
      {
        if ( (mapRequestHeaders != null) && !mapRequestHeaders.containsKey(
                                                                            "Content-Type" ) )
        {
          urlConn.setRequestProperty( "Content-Type",
                                      "application/x-www-form-urlencoded" );
        }

        if ( (mapRequestHeaders != null) && !mapRequestHeaders.containsKey(
                                                                            "Content-Length" ) )
        {
          urlConn.setRequestProperty( "Content-Length",
                                      "" + baRequestData.length );
        }

        urlConn.setFixedLengthStreamingMode( baRequestData.length );
      }

      // Write post data if it's a post request
      if ( bDoOutput )
      {
        os = urlConn.getOutputStream();
        os.write( baRequestData );
        os.flush();
      }

      // Read the response
      byte[] baBlock = new byte[1024];

      is = urlConn.getInputStream();
      ByteArrayOutputStream respOs = new ByteArrayOutputStream();

      for ( int nRead = 0; nRead >= 0; nRead = is.read( baBlock ) )
      {
        respOs.write( baBlock, 0, nRead );
      }

      respOs.close();
      is.close();
      urlConn.disconnect();
      baResponseData = respOs.toByteArray();
    }
    finally
    {
      if ( is != null )
      {
        is.close();
      }

      if ( os != null )
      {
        os.close();
      }

      if ( urlConn != null )
      {
        urlConn.disconnect();
      }
    }
    return baResponseData;
  }

  /**
   * Does an HTTP request to some server, returning a String.
   *
   * @param strUrl
   * @return
   * @throws IOException
   */
  public static String doHttpTransactionString( String strUrl ) throws IOException
  {
    return CHttpUtils.doHttpTransactionString( strUrl, null, null );
  }

  /**
   * Does an HTTP request to some server, returning a String.
   *
   * @param strUrl
   * @param mapFormData
   * @return
   * @throws IOException
   */
  public static String doHttpTransactionString( String strUrl,
                                                Map<String, String> mapFormData ) throws IOException
  {
    String strRequestData = null;
    if ( mapFormData != null )
    {
      strRequestData = CMiscUtils.formEncode( mapFormData );
    }
    return CHttpUtils.doHttpTransactionString( strUrl, strRequestData );
  }

  // Does an HTTP request to some server, returning a String
  public static String doHttpTransactionString( String strUrl,
                                                String strRequestData ) throws IOException
  {
    return CHttpUtils.doHttpTransactionString( strUrl, strRequestData, null );
  }

  // Does an HTTP request to some server, returning a String
  public static String doHttpTransactionString( String strUrl,
                                                String strRequestData,
                                                Map<String, String> mapRequestHeaders ) throws IOException
  {
    return new String( CHttpUtils.doHttpTransactionBytes( strUrl,
                                                          strRequestData == null ? null
                                                                                 : strRequestData.getBytes(),
                                                          mapRequestHeaders ) );
  }
}
