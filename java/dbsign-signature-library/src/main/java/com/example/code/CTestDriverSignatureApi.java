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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.example.code.SignatureAPI.CMiscUtils;
import com.example.code.SignatureAPI.CSignatureAPI;
import com.example.code.SignatureAPI.CSignatureException;
import com.example.code.SignatureAPI.CSignatureOptions;
import com.example.code.SignatureAPI.CSignatureOptions.ContentType;
import com.example.code.SignatureAPI.CSignatureOptionsAcd;
import com.example.code.SignatureAPI.CSignatureOptionsPdf;
import com.example.code.SignatureAPI.CSignatureResult;

/**
 * Test driver program for the CSignatureAPI class.
 */
public class CTestDriverSignatureApi
{

  public static final String DATA_URL      = "http://localhost/dbsign/server";
  public static final String JDBC_DRIVER   = "com.mysql.jdbc.Driver";
  public static final String JDBC_URL      = "jdbc:mysql://localhost:3306/dbsign?useSSL=false";
  public static final String JDBC_USER     = "dbsign";
  public static final String JDBC_PASSWORD = "Qwerty_123";

  public static void main( String[] args )
  {
    try
    {
      System.out.println( "\n#################################" );
      System.out.println( "testSignPdfDtbsParamUserMode()" );
      CTestDriverSignatureApi.testSignPdfDtbsParamUserMode( args );
      
      System.out.println( "\n#################################" );
      System.out.println( "testSignPdfDtbsIdUserMode()" );
      CTestDriverSignatureApi.testSignPdfDtbsIdUserMode( args );

      System.out.println( "\n#################################" );
      System.out.println( "testSignAcdUserMode()" );
      CTestDriverSignatureApi.testSignAcdUserMode( args );

      System.out.println( "\n#################################" );
      System.out.println( "testSignAcdDtbsIdUserMode()" );
      CTestDriverSignatureApi.testSignAcdDtbsIdUserMode( args );

      System.out.println( "\n#################################" );
      System.out.println( "testVerifyAcd()" );
      CTestDriverSignatureApi.testVerifyAcd( args );

      System.out.println( "\n#################################" );
      System.out.println( "testVerifyAcdDtbsId()" );
      CTestDriverSignatureApi.testVerifyAcdDtbsId( args );
    }
    catch ( CSignatureException e )
    {
      System.out.println( "An Error Occurred." );
      // The following is not necessary, just showing how to get the full error info returned from the DBsign Server, if any:
      CSignatureResult result = e.getSignatureResult();
      if ( result != null )
      {
        Map<String, Object> mapErrorFields = result.getErrorFields();
        System.out.println( "DBsign ERROR FIELDS:" );
        for ( Entry<String, Object> entry: mapErrorFields.entrySet() )
        {
          System.out.printf( "  %s: %s%n",
                             entry.getKey(),
                             entry.getValue().toString() );
        }
      }
      System.out.println();
      System.out.println( "Stack Trace:" );
      e.printStackTrace();
    }
    catch ( Exception e )
    {
      e.printStackTrace();
    }
  }

  /**
   * Example function to sign a PDF with User-Id Mode Derived Signature via DTBS-ID
   * method. The DTBS-ID method stores the PDF in a database table that the DBsign
   * Server has access to so that the PDF doesn't have to be sent in the HTTP
   * request. The PDF file is in file Form1040ALL.pdf. The signed PDF will be
   * written to Form1040ALL_Signed.pdf.
   * 
   * @param args
   * @throws CSignatureException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void testSignPdfDtbsIdUserMode( String[] args ) throws CSignatureException, IOException, ClassNotFoundException, SQLException
  {
    System.out.println( "Starting." );

    Connection conn = null;
    try
    {
      // Get base64 PDF.
      File FILE_INPUT = new File( "Form1040ALL.pdf" );
      File FILE_OUTPUT = new File( "Form1040ALL_Signed_id.pdf" );
      System.out.println( "Reading input file..." );
      String strPdfBase64 = CMiscUtils.readFileB64( FILE_INPUT );

      System.out.println( "Connecting to database..." );
      // If using the DTBS-ID method, get a database connection. You should really get this
      // from the DataSource configure in Tomcat. Or maybe a different one.
      // I wrote a little function to do this. Like this:
      // conn = CSignatureAPI.getConnectionFromDatasource("jdbc/SOME_DB_NAME");
      conn = CSignatureAPI.connectToDatabaseJdbc( CTestDriverSignatureApi.JDBC_DRIVER,
                                                  CTestDriverSignatureApi.JDBC_URL,
                                                  CTestDriverSignatureApi.JDBC_USER,
                                                  CTestDriverSignatureApi.JDBC_PASSWORD );

      // Make an instance of the signature API class.
      CSignatureAPI api = new CSignatureAPI();

      // Set DBsign Server URL
      api.setDataUrl( CTestDriverSignatureApi.DATA_URL );

      // Set up DTBS-ID table info. This shows how you can include the schema in the
      // table name and how you can override the default column names. If you are using the
      // DBS_DTBS table in the DBsign system schema, then you don't need to specify column names.
      // api.setDtbsTable( "PdfDemo.DbsDtbs" );
      // api.setColDtbsId( "dtbsId" );
      // api.setColDtbs( "dtbs" );
      // api.setColDtbsDate( "dtbsDate" );
      // api.setColSignature( "signature" );

      // Set up the options for the PDF signature.
      CSignatureOptionsPdf options = new CSignatureOptionsPdf();
      // options.setInstanceName( "PdfDemo" );
      options.setPdfSigFieldName( "yourSignature" );
      options.setSasUserId( "user1234" );
      options.setSignerName( "Bob D. User" );
      options.setDtbs( strPdfBase64, CSignatureOptions.EncFmt.BASE64 );

      // Optionally add lock fields
      options.setLockFields( Arrays.asList( "amountYouMade", "amountYouOwe" ) );

      // Optionally add PDF fields and values to be filled.
      Map<String, String> mapFillFields = new HashMap<>();
      mapFillFields.put( "amountYouMade", "123" );
      mapFillFields.put( "amountYouOwe", "1234" );
      mapFillFields.put( "yourOccupation", "Slacker" );
      mapFillFields.put( "yourPhone", "123-456-7890" );
      options.setFillFields( mapFillFields );

      // Actually start the signing process. Base64 signed PDF is returned in strPfdBase64Signed.
      System.out.println( "Calling signing API... " );
      long nStart = System.currentTimeMillis();

      CSignatureResult result = api.signDtbsId( conn, options );
      String strPfdBase64Signed = result.getSignature();

      long nStop = System.currentTimeMillis();
      System.out.printf( "%02.2f sec%n", (nStop - nStart) / 1000.0 );

      if ( (strPfdBase64Signed == null) || (strPfdBase64Signed.length() <= 0) )
      {
        throw new CSignatureException( "Empty signature returned from signing operation." );
      }
      else
      {
        // Here, you would put the signed PDF back in the application database somehow.
        System.out.println( "Writing signed PDF output file..." );

        CMiscUtils.writeBytesToFile( FILE_OUTPUT,
                                     CMiscUtils.base64Decode( strPfdBase64Signed ) );
      }
    }
    finally
    {
      // Carefully close the JDBC connection
      if ( (conn != null) )
      {
        try
        {
          conn.close();
        }
        catch ( Exception e )
        {
        }
      }
    }
    System.out.println( "Done!" );
  }

  /**
   * Example function to sign a PDF with User-Id Mode Derived Signature via DTBS-ID
   * method. The DTBS-ID method stores the PDF in a database table that the DBsign
   * Server has access to so that the PDF doesn't have to be sent in the HTTP
   * request. The PDF file is in file Form1040ALL.pdf. The signed PDF will be
   * written to Form1040ALL_Signed.pdf.
   * 
   * @param args
   * @throws CSignatureException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void testSignPdfDtbsParamUserMode( String[] args ) throws CSignatureException, IOException, ClassNotFoundException, SQLException
  {
    System.out.println( "Starting." );

    Connection conn = null;
    try
    {
      // Get base64 PDF.
      File FILE_INPUT = new File( "Form1040ALL.pdf" );
      File FILE_OUTPUT = new File( "Form1040ALL_Signed_param.pdf" );
      System.out.println( "Reading input file..." );
      String strPdfBase64 = CMiscUtils.readFileB64( FILE_INPUT );

      // Make an instance of the signature API class.
      CSignatureAPI api = new CSignatureAPI();

      // Set DBsign Server URL
      api.setDataUrl( CTestDriverSignatureApi.DATA_URL );

      // Set up the options for the PDF signature.
      CSignatureOptionsPdf options = new CSignatureOptionsPdf();
      // options.setInstanceName( "PdfDemo" );
      options.setPdfSigFieldName( "yourSignature" );
      options.setSasUserId( "user1234" );
      options.setSignerName( "Bob D. User" );
      options.setDtbs( strPdfBase64, CSignatureOptions.EncFmt.BASE64 );

      // Optionally add lock fields
      options.setLockFields( Arrays.asList( "amountYouMade", "amountYouOwe" ) );

      // Optionally add PDF fields and values to be filled.
      Map<String, String> mapFillFields = new HashMap<>();
      mapFillFields.put( "amountYouMade", "123" );
      mapFillFields.put( "amountYouOwe", "1234" );
      mapFillFields.put( "yourOccupation", "Slacker" );
      mapFillFields.put( "yourPhone", "123-456-7890" );
      options.setFillFields( mapFillFields );

      // Actually start the signing process. Base64 signed PDF is returned in strPfdBase64Signed.
      System.out.println( "Calling signing API... " );
      long nStart = System.currentTimeMillis();

      CSignatureResult result = api.sign( options );
      String strPfdBase64Signed = result.getSignature();

      long nStop = System.currentTimeMillis();
      System.out.printf( "%02.2f sec%n", (nStop - nStart) / 1000.0 );

      if ( (strPfdBase64Signed == null) || (strPfdBase64Signed.length() <= 0) )
      {
        throw new CSignatureException( "Empty signature returned from signing operation." );
      }
      else
      {
        // Here, you would put the signed PDF back in the application database somehow.
        System.out.println( "Writing signed PDF output file..." );

        CMiscUtils.writeBytesToFile( FILE_OUTPUT,
                                     CMiscUtils.base64Decode( strPfdBase64Signed ) );
      }
    }
    finally
    {
      // Carefully close the JDBC connection
      if ( (conn != null) )
      {
        try
        {
          conn.close();
        }
        catch ( Exception e )
        {
        }
      }
    }
    System.out.println( "Done!" );
  }

  /**
   * Example function to sign data with Application Constructed Data (i.e., data
   * specified by the host application) using User-Id mode Derived Signature.
   * The Data To Be Signed (DTBS) is stored in file DTBS.txt and the resulting
   * signature is stored in DTBS_Signature.txt.
   * 
   * @param args
   * @throws CSignatureException
   * @throws IOException
   */
  public static void testSignAcdUserMode( String[] args ) throws CSignatureException, IOException
  {
    System.out.println( "Starting." );

    try
    {
      // Get base64 DTBS.
      File FILE_INPUT = new File( "DTBS.txt" );
      File FILE_OUTPUT = new File( "DTBS_Signature.txt" );
      System.out.println( "Reading input file..." );
      String strDtbsBase64 = CMiscUtils.base64Encode( CMiscUtils.getBytesFromFile( FILE_INPUT ) );

      // Make an instance of the signature API class.
      CSignatureAPI api = new CSignatureAPI();

      // Set DBsign Server URL
      api.setDataUrl( CTestDriverSignatureApi.DATA_URL );

      // Set up the options for the ACD signature.
      CSignatureOptionsAcd options = new CSignatureOptionsAcd( ContentType.APP_SIGN_SAS );
      options.setSasUserId( "user1234" );
      options.setDtbs( strDtbsBase64, CSignatureOptions.EncFmt.BASE64 );

      // Actually start the signing process. Base64 signature is returned in strPfdBase64Signed.
      System.out.println( "Calling signing API... " );
      long nStart = System.currentTimeMillis();

      CSignatureResult result = api.sign( options );
      String strDtbsBase64Signed = result.getSignature();

      long nStop = System.currentTimeMillis();
      System.out.printf( "%02.2f sec%n", (nStop - nStart) / 1000.0 );

      if ( (strDtbsBase64Signed == null)
           || (strDtbsBase64Signed.length() <= 0) )
      {
        throw new CSignatureException( "Empty signature returned from signing operation." );
      }
      else
      {
        // Here, you would put the signature back in the application database somehow.
        System.out.println( "Writing signature output file..." );

        CMiscUtils.writeBytesToFile( FILE_OUTPUT,
                                     CMiscUtils.stringToBytes( strDtbsBase64Signed ) );
      }
    }
    finally
    {
    }
    System.out.println( "Done!" );
  }

  /**
   * Example function to sign data with Application Constructed Data (i.e., data
   * specified by the host application) using User-Id mode Derived Signature and
   * using DTBS-ID. The DTBS-ID method stores the PDF in a database table that
   * the DBsign Server has access to so that the PDF doesn't have to be sent in
   * the HTTP request. The Data To Be Signed (DTBS) is stored in file DTBS.txt
   * and the resulting signature is stored in DTBS_Signature.txt.
   * 
   * @param args
   * @throws CSignatureException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void testSignAcdDtbsIdUserMode( String[] args ) throws CSignatureException, IOException, ClassNotFoundException, SQLException
  {
    System.out.println( "Starting." );

    Connection conn = null;
    try
    {
      // Get base64 DTBS.
      File FILE_INPUT = new File( "DTBS.txt" );
      File FILE_OUTPUT = new File( "DTBS_Signature.txt" );
      System.out.println( "Reading input file..." );
      String strDtbsBase64 = CMiscUtils.readFileB64( FILE_INPUT );

      System.out.println( "Connecting to database..." );
      // If using the DTBS-ID method, get a database connection. You should really get this
      // from the DataSource configure in Tomcat. Or maybe a different one.
      // I wrote a little function to do this. Like this:
      // conn = CSignatureAPI.getConnectionFromDatasource("java:/comp/env/jdbc/SOME_DB_NAME");
      conn = CSignatureAPI.connectToDatabaseJdbc( CTestDriverSignatureApi.JDBC_DRIVER,
                                                  CTestDriverSignatureApi.JDBC_URL,
                                                  CTestDriverSignatureApi.JDBC_USER,
                                                  CTestDriverSignatureApi.JDBC_PASSWORD );

      // Make an instance of the signature API class.
      CSignatureAPI api = new CSignatureAPI();

      // Set DBsign Server URL
      api.setDataUrl( CTestDriverSignatureApi.DATA_URL );

      // Set up the options for the ACD signature.
      CSignatureOptionsAcd options = new CSignatureOptionsAcd( ContentType.APP_SIGN_SAS );
      options.setSasUserId( "user1234" );
      options.setDtbs( strDtbsBase64, CSignatureOptions.EncFmt.BASE64 );

      // Actually start the signing process. Base64 signature is returned in strPfdBase64Signed.
      System.out.println( "Calling signing API... " );
      long nStart = System.currentTimeMillis();

      CSignatureResult result = api.signDtbsId( conn, options );
      String strDtbsBase64Signed = result.getSignature();

      long nStop = System.currentTimeMillis();
      System.out.printf( "%02.2f sec%n", (nStop - nStart) / 1000.0 );

      if ( (strDtbsBase64Signed == null)
           || (strDtbsBase64Signed.length() <= 0) )
      {
        throw new CSignatureException( "Empty signature returned from signing operation." );
      }
      else
      {
        // Here, you would put the signature back in the application database somehow.
        System.out.println( "Writing signature output file..." );

        CMiscUtils.writeBytesToFile( FILE_OUTPUT,
                                     CMiscUtils.stringToBytes( strDtbsBase64Signed ) );
      }
    }
    finally
    {
      // Carefully close the JDBC connection
      if ( (conn != null) )
      {
        try
        {
          conn.close();
        }
        catch ( Exception e )
        {
        }
      }
    }
    System.out.println( "Done!" );
  }

  /**
   * Example function to verify a signature with Application Constructed Data
   * (i.e., data specified by the host application). The Data To Be Signed (DTBS)
   * is stored in file DTBS.txt and the resulting signature is stored in
   * DTBS_Signature.txt.
   * 
   * @param args
   * @throws CSignatureException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void testVerifyAcd( String[] args ) throws CSignatureException, IOException
  {
    System.out.println( "Starting." );

    try
    {
      // Get base64 DTBS.
      File FILE_DTBS = new File( "DTBS.txt" );
      File FILE_SIG = new File( "DTBS_Signature.txt" );
      System.out.println( "Reading input file..." );
      String strDtbs = CMiscUtils.readFileText( FILE_DTBS );
      String strSignatureBase64 = CMiscUtils.readFileText( FILE_SIG );

      // Make an instance of the signature API class.
      CSignatureAPI api = new CSignatureAPI();

      // Set DBsign Server URL
      api.setDataUrl( CTestDriverSignatureApi.DATA_URL );

      // Set up the options for the signature.
      CSignatureOptionsAcd options = new CSignatureOptionsAcd( ContentType.APP_VERIFY );
      options.setDtbs( strDtbs, CSignatureOptions.EncFmt.TEXT );
      options.setSignature( strSignatureBase64,
                            CSignatureOptions.EncFmt.BASE64 );

      // Actually start the verification process.
      System.out.println( "Calling verify API... " );
      long nStart = System.currentTimeMillis();

      CSignatureResult result = api.verify( options );

      long nStop = System.currentTimeMillis();
      System.out.printf( "%02.2f sec%n", (nStop - nStart) / 1000.0 );

      System.out.println( "Signer SubjectDn: " + result.getSignerSubjectDn() );
      System.out.println( "Signer UserId: " + result.getSignerUserId() );
      System.out.println( "Signing Time: " + result.getSignDate() );

      if ( result.isError() )
      {
        throw new CSignatureException( "Signature Verification Failed",
                                       result );
      }

    }
    finally
    {
    }
    System.out.println( "Done!" );
  }

  /**
   * Example function to verify a signature with Application Constructed Data
   * (i.e., data specified by the host application) using DTBS-ID. The DTBS-ID
   * method stores the PDF in a database table that the DBsign Server has access
   * to so that the PDF doesn't have to be sent in the HTTP request. The Data To
   * Be Signed (DTBS) is stored in file DTBS.txt and the resulting signature is
   * stored in DTBS_Signature.txt.
   * 
   * @param args
   * @throws CSignatureException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static void testVerifyAcdDtbsId( String[] args ) throws CSignatureException, IOException, ClassNotFoundException, SQLException
  {
    System.out.println( "Starting." );

    Connection conn = null;
    try
    {
      // Get base64 DTBS.
      File FILE_DTBS = new File( "DTBS.txt" );
      File FILE_SIG = new File( "DTBS_Signature.txt" );
      System.out.println( "Reading input file..." );
      String strDtbsBase64 = CMiscUtils.readFileText( FILE_DTBS );
      String strSignatureBase64 = CMiscUtils.readFileText( FILE_SIG );

      System.out.println( "Connecting to database..." );
      // If using the DTBS-ID method, get a database connection. You should really get this
      // from the DataSource configure in Tomcat. Or maybe a different one.
      // I wrote a little function to do this. Like this:
      // conn = CSignatureAPI.getConnectionFromDatasource("java:/comp/env/jdbc/SOME_DB_NAME");
      conn = CSignatureAPI.connectToDatabaseJdbc( CTestDriverSignatureApi.JDBC_DRIVER,
                                                  CTestDriverSignatureApi.JDBC_URL,
                                                  CTestDriverSignatureApi.JDBC_USER,
                                                  CTestDriverSignatureApi.JDBC_PASSWORD );

      // Make an instance of the signature API class.
      CSignatureAPI api = new CSignatureAPI();

      // Set DBsign Server URL
      api.setDataUrl( CTestDriverSignatureApi.DATA_URL );

      // Set up the options for the ACD signature.
      CSignatureOptionsAcd options = new CSignatureOptionsAcd( ContentType.APP_VERIFY );
      options.setDtbs( strDtbsBase64, CSignatureOptions.EncFmt.TEXT );
      options.setSignature( strSignatureBase64,
                            CSignatureOptions.EncFmt.BASE64 );

      // Actually start the verification process.
      System.out.println( "Calling verify API... " );
      long nStart = System.currentTimeMillis();

      CSignatureResult result = api.verifyDtbsId( conn, options );

      long nStop = System.currentTimeMillis();
      System.out.printf( "%02.2f sec%n", (nStop - nStart) / 1000.0 );

      System.out.println( "Signer SubjectDn: " + result.getSignerSubjectDn() );
      System.out.println( "Signer UserId: " + result.getSignerUserId() );
      System.out.println( "Signing Time: " + result.getSignDate() );

      if ( result.isError() )
      {
        throw new CSignatureException( "Signature Verification Failed",
                                       result );
      }

    }
    finally
    {
      // Carefully close the JDBC connection
      if ( (conn != null) )
      {
        try
        {
          conn.close();
        }
        catch ( Exception e )
        {
        }
      }
    }
    System.out.println( "Done!" );
  }

}
