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

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This class uses CSignatureUtils (which, in turn uses CHttpUtils) to make requests
 * to the DBsign Server. The parameters for the requests are specified via the
 * CSignatureOptions* classes. The result is a CSignatureResult object which has
 * easy to use access functions.
 *
 * The DTBS-ID feature is the most efficient and performant and should
 * be used when possible.
 *
 * To use the API, there are few basic steps:
 *
 * + Get a JDBC connection that can select, insert, and delete from the DTBS-ID table.
 * + Create a CSignatureOptions* object.
 * + Set the DBsign Server URL with the setDataUrl() method and also the DTBS-ID table
 * info with setDtbsTable() and setCol*() methods. The setCol*() methods default to
 * the columns in the DTBS_ID table in the DBsign schema. This is not necessary
 * if you are using DBsign's DBS_DTBS table.
 * + Call the one of the sign*() functions. These return the base64 encoded
 * signed PDF as their return value and except on error.
 * + Handle exceptions.
 * + Close or otherwise release the JDBC connection in a finally block
 *
 */
public class CSignatureAPI
{
  // DBsign Server URL
  protected String strDataUrl      = null;

  // DTBS-ID table info. These default to the DBS_DTBS table in the DBsign schema.
  protected String strDtbsTable    = "DBS_DTBS";
  protected String strColDtbsId    = "DTBS_ID";
  protected String strColDtbs      = "DTBS";
  protected String strColDtbsDate  = "DTBS_DATE";
  protected String strColSignature = "SIGNATURE";

  public CSignatureAPI()
  {
    super();
  }

  /**
   * Sign data (ACD, PDF), putting the data in the HTTP request to the DBsign Server.
   *
   * @param options
   * @return
   * @throws CSignatureException
   */
  public CSignatureResult sign( CSignatureOptions options ) throws CSignatureException
  {
    return this.doRequest( options );
  }

  /**
   * Verify signature (ACD), putting the signature and data in the HTTP request
   * to the DBsign Server.
   *
   * @param options
   * @return
   * @throws CSignatureException
   */
  public CSignatureResult verify( CSignatureOptions options ) throws CSignatureException
  {
    return this.doRequest( options );
  }

  /**
   * Signs data (ACD, PDF) with derived signature using the DTBS-ID feature.
   * Performs all the JDBC work to insert DTBS into the DTBS-ID table and handles
   * deleting the record when it's finished and on failure.
   *
   * @param conn
   *          JDBC Connection object
   * @param options
   *          The signature options object
   * @return Base64 encoded signed PDF
   * @throws CSignatureException
   */
  public CSignatureResult signDtbsId( Connection conn,
                                      CSignatureOptionsAcd options ) throws CSignatureException
  {
    return doRequestDtbsId( conn, options );
  }

  /**
   * Verify signature (ACD) using the DTBS-ID feature.
   * Performs all the JDBC work to insert DTBS into the DTBS-ID table and handles
   * deleting the record when it's finished and on failure.
   *
   * @param conn
   *          JDBC Connection object
   * @param options
   *          The signature options object
   * @return Base64 encoded signed PDF
   * @throws CSignatureException
   */
  public CSignatureResult verifyDtbsId( Connection conn,
                                        CSignatureOptionsAcd options ) throws CSignatureException
  {
    return doRequestDtbsId( conn, options );
  }

  /**
   * Does all the work using the non-DTBS-ID method
   * 
   * @param options
   * @return
   * @throws CSignatureException
   */
  protected CSignatureResult doRequest( CSignatureOptions options ) throws CSignatureException
  {
    CSignatureResult result = null;

    // Clone the options because we are going to change them. The caller might not like
    // us changing his copy.
    CSignatureOptions _options = null;
    try
    {
      _options = (CSignatureOptions) options.clone();
    }
    catch ( CloneNotSupportedException e )
    {
      throw new CSignatureException( e.toString(), e );
    }

    // Make request params from options object
    Map<String, String> mapRequest = _options.toRequestMap();

    // Do the HTTP request to the DBsign Server and parse the response.
    // System.out.println( "Doing HTTP request to DBsign Server..." );
    // long nStart = System.currentTimeMillis();
    Map<String, String> mapResponse = CSignatureUtils.doSignatureRequest( this.getDataUrl(),
                                                                          mapRequest );
    // long nStop = System.currentTimeMillis();
    // System.out.printf( "Request Time: %02.2f sec\n", (nStop - nStart) / 1000.0 );

    if ( (mapResponse == null) || mapResponse.isEmpty() )
    {
      throw new CSignatureException( "No response map from signature request." );
    }
    else
    {
      result = new CSignatureResult( mapResponse );

      // If the response indicates an error, throw an exception
      int nDbsErrorVal = result.getDbsErrorVal();
      if ( nDbsErrorVal != 0 )
      {
        String strMsg = result.getErrorDesc();
        if ( CMiscUtils.isStringEmpty( strMsg ) )
        {
          strMsg = result.getDbsErrorMsg();
        }
        throw new CSignatureException( strMsg, result );
      }
    }

    return result;
  }

  /**
   * Does all the work for {ACD, PDF} using the method
   * 
   * @param conn
   * @param options
   * @return
   * @throws CSignatureException
   */
  protected CSignatureResult doRequestDtbsId( Connection conn,
                                              CSignatureOptionsAcd options ) throws CSignatureException
  {
    CSignatureResult result = null;

    if ( (conn == null) || (options == null) )
    {
      throw new CSignatureException( "Invalid input parameter when signing PDF in DTBS-ID user mode." );
    }

    // Clone the options because we are going to change them. The caller might not like
    // us changing his copy.
    CSignatureOptionsAcd _options = null;
    try
    {
      _options = (CSignatureOptionsAcd) options.clone();
    }
    catch ( CloneNotSupportedException e )
    {
      throw new CSignatureException( e.toString(), e );
    }

    // If DTBS-ID is not set, make one up at random.
    String strDtbsId = _options.getDtbsId();
    if ( CMiscUtils.isStringEmpty( strDtbsId ) )
    {
      strDtbsId = CMiscUtils.makeRandomId();
      _options.setDtbsId( strDtbsId );
    }

    // Get the DTBS in binary so we can put it in the database.
    byte[] baDtbsBytes = _options.getDtbsBytes();
    byte[] baSignatureBytes = options.getSignatureBytes();

    // Don't want the PDF in the options at all because we don't want it sent to the server.
    _options.setDtbs( null, null );
    _options.setSignature( null, null );

    // Insert PDF into DTBS-ID table for the DBsign Server to access
    this.insertDtbsIdRecord( conn, strDtbsId, baDtbsBytes, baSignatureBytes );

    try
    {
      // Make request params from options object
      Map<String, String> mapRequest = _options.toRequestMap();

      // Do the HTTP request to the DBsign Server and parse the response.
      Map<String, String> mapResponse = CSignatureUtils.doSignatureRequest( this.getDataUrl(),
                                                                            mapRequest );

      if ( (mapResponse == null) || mapResponse.isEmpty() )
      {
        throw new CSignatureException( "No response map from signature request." );
      }
      else
      {
        result = new CSignatureResult( mapResponse );

        // If the response indicates an error, throw an exception
        int nDbsErrorVal = result.getDbsErrorVal();
        if ( nDbsErrorVal != 0 )
        {
          throw new CSignatureException( result.getErrorDesc(), result );
        }

        // Retrieve signed PDF in DTBS-ID signature column.
        byte[] baSignature = this.getDtbsIdSignature( conn, strDtbsId, false );

        if ( (baSignature == null) || (baSignature.length <= 0) )
        {
          throw new CSignatureException( "Empty DTBS-ID signature retrieved.",
                                        result );
        }
        else
        {
          result.setSignature( CMiscUtils.base64Encode( baSignature ) );
        }
      }
    }
    finally
    {
      // Delete the DTBS-ID record regardless of what happened.
      this.deleteDtbsId( conn, strDtbsId );
    }

    return result;
  }

  /**
   * Insert binary DTBS into DTBS-ID table
   *
   * @param conn
   * @param strDtbsId
   * @param baDtbsBytes
   * @param baSignature
   * @throws CSignatureException
   */
  public void insertDtbsIdRecord( Connection conn,
                                  String strDtbsId,
                                  byte[] baDtbsBytes,
                                  byte[] baSignature ) throws CSignatureException
  {
    if ( (conn == null) || CMiscUtils.isStringEmpty( strDtbsId )
         || (baDtbsBytes == null) || (baDtbsBytes.length <= 0) )
    {
      throw new CSignatureException( "Invalid input parameter inserting record into DTBS-ID table." );
    }

    PreparedStatement stmt = null;
    try
    {
      // Make SQL statement using the user configurable table and column names.
      String strSql = String.format( "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                                     this.getDtbsTable(),
                                     this.getColDtbsId(),
                                     this.getColDtbsDate(),
                                     this.getColDtbs(),
                                     this.getColSignature() );

      // Use PreparedStatment whenever possible for security reasons.
      stmt = conn.prepareStatement( strSql );
      int nParam = 1;
      stmt.setString( nParam++, strDtbsId );
      stmt.setTimestamp( nParam++,
                         new Timestamp( System.currentTimeMillis() ) );

      if ( (baDtbsBytes != null) && (baDtbsBytes.length > 0) )
      {
        stmt.setBinaryStream( nParam++,
                              new ByteArrayInputStream( baDtbsBytes ) );
      }
      else
      {
        stmt.setNull( nParam++, Types.LONGVARBINARY );
      }

      if ( (baSignature != null) && (baSignature.length > 0) )
      {
        stmt.setBinaryStream( nParam++,
                              new ByteArrayInputStream( baSignature ) );
      }
      else
      {
        stmt.setNull( nParam++, Types.LONGVARBINARY );
      }

      stmt.execute();
      stmt.close();

      if ( !conn.getAutoCommit() )
      {
        conn.commit();
      }
    }
    catch ( Exception e )
    {
      // Carefully rollback
      try
      {
        if ( !conn.getAutoCommit() )
        {
          conn.rollback();
        }
      }
      catch ( Exception x )
      {
      }
      throw new CSignatureException( "Error inserting record into DTBS-ID table: "
                                    + e.toString(),
                                    e );
    }
    finally
    {
      // Carefully close statement
      try
      {
        if ( stmt != null )
        {
          stmt.close();
        }
      }
      catch ( Exception e )
      {
      }
    }
  }

  /**
   * Queries the signature from the DTBS-ID table.
   *
   * @param conn
   * @param strDtbsId
   * @param bDelete
   * @return
   * @throws CSignatureException
   */
  public byte[] getDtbsIdSignature( Connection conn,
                                    String strDtbsId,
                                    boolean bDelete ) throws CSignatureException
  {
    byte[] baReturn = null;

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try
    {
      if ( (conn == null) || CMiscUtils.isStringEmpty( strDtbsId ) )
      {
        throw new Exception( "Invalid input parameter querying signature from DTBS-ID table." );
      }

      // Make SQL statement using the user configurable table and column names.
      String strSql = String.format( "SELECT %s FROM %s WHERE %s = ?",
                                     this.getColSignature(),
                                     this.getDtbsTable(),
                                     this.getColDtbsId() );

      // Use PreparedStatment whenever possible for security reasons.
      stmt = conn.prepareStatement( strSql );
      int nParam = 1;
      stmt.setString( nParam++, strDtbsId );
      rs = stmt.executeQuery();

      if ( rs.next() )
      {
        nParam = 1;
        baReturn = rs.getBytes( nParam++ );
      }
    }
    catch ( Exception e )
    {
      throw new CSignatureException( "Error inserting PDF into DTBS-ID table: "
                                    + e.toString(),
                                    e );
    }
    finally
    {
      // Carefully close result set
      try
      {
        if ( rs != null )
        {
          rs.close();
        }
      }
      catch ( Exception e )
      {
      }
      // Carefully close statement
      try
      {
        if ( stmt != null )
        {
          stmt.close();
        }
      }
      catch ( Exception e )
      {
      }
    }

    if ( bDelete )
    {
      this.deleteDtbsId( conn, strDtbsId );
    }

    return baReturn;
  }

  /**
   * Deletes a record from the DTBS-ID table.
   *
   * @param conn
   * @param strDtbsId
   * @throws CSignatureException
   */
  public void deleteDtbsId( Connection conn,
                            String strDtbsId ) throws CSignatureException
  {
    if ( (conn == null) || CMiscUtils.isStringEmpty( strDtbsId ) )
    {
      throw new CSignatureException( "Invalid input parameter deleting from DTBS-ID table." );
    }

    PreparedStatement stmt = null;
    try
    {
      // Make SQL statement using the user configurable table and column names.
      String strSql = String.format( "DELETE FROM %s WHERE %s = ?",
                                     this.getDtbsTable(),
                                     this.getColDtbsId() );

      // Use PreparedStatment whenever possible for security reasons.
      stmt = conn.prepareStatement( strSql );
      int nParam = 1;
      stmt.setString( nParam++, strDtbsId );
      stmt.execute();
      stmt.close();

      if ( !conn.getAutoCommit() )
      {
        conn.commit();
      }
    }
    catch ( Exception e )
    {
      // Carefully rollback
      try
      {
        if ( !conn.getAutoCommit() )
        {
          conn.rollback();
        }
      }
      catch ( Exception x )
      {
      }
      throw new CSignatureException( "Error deleting from DTBS-ID table: "
                                    + e.toString(),
                                    e );
    }
    finally
    {
      // Carefully close stmt
      try
      {
        if ( stmt != null )
        {
          stmt.close();
        }
      }
      catch ( Exception e )
      {
      }

    }
  }

  /**
   * Function to get JDBC connection from JNDI JDBC DataSource. Connections from
   * a DataSource should be closed in a finally block when you are finished with it.
   * Otherwise, you will leak connections.
   *
   * See: https://cwiki.apache.org/confluence/display/TOMCAT/UsingDataSources
   *
   * @param strDsName
   *          Data source name, something like "jdbc/SOME_DS_NAME"
   * @return JDBC connection
   * @throws NamingException
   * @throws SQLException
   */
  public static Connection getConnectionFromDatasource( String strDsName ) throws NamingException, SQLException
  {
    Connection connReturn = null;

    if ( !CMiscUtils.isStringEmpty( strDsName ) )
    {
      Context ctx = null;
      try
      {
        Context initContext = new InitialContext();
        Context envContext = (Context) initContext.lookup( "java:/comp/env" );
        DataSource ds = (DataSource) envContext.lookup( strDsName );

        connReturn = ds.getConnection();
      }
      finally
      {
        if ( ctx != null )
        {
          ctx.close();
        }
      }
    }

    return connReturn;
  }

  /**
   * Does a straight JDBC connection to the database using regular JDBC parameters
   * 
   * @param strJdbcDriverClass
   * @param strJdbcUrl
   * @param strUsername
   * @param strPassword
   * @return
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public static Connection connectToDatabaseJdbc( String strJdbcDriverClass,
                                                  String strJdbcUrl,
                                                  String strUsername,
                                                  String strPassword ) throws ClassNotFoundException, SQLException
  {
    Connection connReturn = null;

    if ( !CMiscUtils.isStringEmpty( strJdbcDriverClass ) )
    {
      Class.forName( strJdbcDriverClass );
    }

    if ( !CMiscUtils.isStringEmpty( strJdbcUrl ) && !CMiscUtils.isStringEmpty(
                                                                               strUsername )
         && !CMiscUtils.isStringEmpty( strPassword ) )
    {
      connReturn = DriverManager.getConnection( strJdbcUrl,
                                                strUsername,
                                                strPassword );
    }

    return connReturn;
  }

  // Getters and setters

  /**
   * Sets the "data URL", the URL to the DBsign Server.
   * Usually something like: http://some_host:some_port/dbsign/server
   *
   * @param strDataUrl
   */
  public void setDataUrl( String strDataUrl )
  {
    this.strDataUrl = strDataUrl;
  }

  public String getDataUrl()
  {
    return this.strDataUrl;
  }

  /**
   * Set the name of the DTBS-ID table. If required, a schema name can be prepended in
   * "schema.table" format. Defaults to "DBS_DTBS".
   *
   * This does not have to be the DBS_DTBS table that DBsign comes with, but it can be
   * a different table that is structured with at least the same 4 columns. The columns
   * can be named differently, but the data types should be the same.
   *
   * The DTBS-ID table MUST be accessible by the DBsign Server and the same table name and
   * columns must be specified in the DBsign Server configuration.
   *
   * @param strDtbsTable
   */
  public void setDtbsTable( String strDtbsTable )
  {
    this.strDtbsTable = strDtbsTable;
  }

  public String getDtbsTable()
  {
    return this.strDtbsTable;
  }

  /**
   * Sets the DTBS-ID id column. Defaults to "DTBS_ID".
   *
   * @param strColDtbsId
   */
  public void setColDtbsId( String strColDtbsId )
  {
    this.strColDtbsId = strColDtbsId;
  }

  public String getColDtbsId()
  {
    return this.strColDtbsId;
  }

  /**
   * Sets the DTBS-ID DTBS column. Defaults to "DTBS".
   *
   * @param strColDtbs
   */
  public void setColDtbs( String strColDtbs )
  {
    this.strColDtbs = strColDtbs;
  }

  public String getColDtbs()
  {
    return this.strColDtbs;
  }

  /**
   * Set the DTBS-ID date column, which is the data the DTBS record was created. This is
   * designed to be used to purge any records that don't get deleted for some reason.
   *
   * Default to "DTBS_DATE"
   *
   * @param strColDtbsDate
   */
  public void setColDtbsDate( String strColDtbsDate )
  {
    this.strColDtbsDate = strColDtbsDate;
  }

  public String getColDtbsDate()
  {
    return this.strColDtbsDate;
  }

  /**
   * Sets the DTBS-ID signature column which is populated by the result of the signature
   * operation. Defaults to "SIGNATURE".
   *
   * @param strColSignature
   */
  public void setColSignature( String strColSignature )
  {
    this.strColSignature = strColSignature;
  }

  public String getColSignature()
  {
    return this.strColSignature;
  }

}
