# dbsign-signature-library

```
/* ----------------------------------------------------------------------------
 * DO NOT REMOVE THIS DISCLAIMER:
 *
 * This software is provided as example code only! It is not part of the DBsign
 * Security Suite, nor is it supported or maintained by DBsign, Inc.
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
```

Table of Contents:
- [dbsign-signature-library](#dbsign-signature-library)
  - [Basic Use of the API](#basic-use-of-the-api)
  - [Canonical DTBS](#canonical-dtbs)
  - [Building the Code](#building-the-code)

This is a simple library that takes care of many of the details of interacting with the DBsign Server.  

It is called from server side Java code and contains classes and APIs that help with

- Formatting and making HTTP requests to the DBsign Server,
- Parsing responses from the DBsign Server into easily usable objects,
- Managing database records for the DBsign DTBS-ID feature, and
- Creating canonical XML to format data to be signed (DTBS).

The `dbsign-signature-library` can be used to sign various types of data including PDF files, text data, binary data, etc.  It also has a class called `CCanonicalXml` which allows you to use a `java.util.Map` type interface to create structured data to be signed that is always generated exactly for the same data.

## Basic Use of the API

The Java code of the `dbsign-signature-library` is pretty simple and can be modified to meet your requirements.  Essentially, you just do the following basic steps: 

1. Make an instance of the `CSignatureAPI` object,
2. Create and populate a `CSignatureOptions*` object,
3. Pass the the CSignatureOptions object to the API's `sign*()` or `verify*()` functions, and
4. Receive the result of the operation in a `CSignatureResult` object.

The function is almost identical for signature verification.

Here is an example of making a user-id mode derived signature on some text data:

```
// Make an instance of the signature API class.
CSignatureAPI api = new CSignatureAPI();

// Set DBsign Server URL
api.setDataUrl( "https://demo.dbsign.com/dbsign/server" );

// Set up the options for the user-id mode signature.
CSignatureOptionsAcd options = new CSignatureOptionsAcd( ContentType.APP_SIGN_SAS );
options.setSasUserId( "user1234" );
options.setDtbs( "My dog has fleas.", CSignatureOptions.EncFmt.TEXT );

// Actually start the signing process.
System.out.println( "Calling signing API... " );

CSignatureResult result = api.sign( options );

String strDtbsBase64Signed = result.getSignature();
```

There are also functions in the API for inserting, fetching, and deleting database records for DBsign's DTBS-ID feature.  The DTBS-ID feature allows the host application to use the database to communicate potentially large DTBS blocks to the DBsign Server via database tables.  There are also API helper functions for obtaining database connections from a JNDI `javax.sql.DataSource` and also just creating connections using a JDBC driver.  

There are two example driver programs in the `com.signature.code` package.

## Canonical DTBS

> *Canonicalization: a process for converting data that has more than one possible representation into a "standard", "normal", or canonical form.*

Data canonicalization is important when data is digitally signed because any difference in the data values or format will cause a signature verification failure.  The `dbsign-signature-library` contains a class called `CCanonicalDtbs` to help construct XML data that is always formatted the same way.

The `CCanonicalDtbs` class is just a `Map` that contains "items" which are contained in "sections".  A `CCanonicalDtbs` object itself represents a section and contains both items and references to other `CCanonicalDtbs` sections. Items are just string key to string value mappings inside a section. These nested sections implement a heirarchical data structure which can be easily represented in XML.  There are also open standards for XML canonicalization, unlike JSON.

A `CanonicalDtbs` class can be converted to a canonicalized XML via its `toXml()` method.  Conversly, the XML can be parsed back into nested `CanonicalDtbs` objects with its `parse()` method.

Here is an example:

```
  // Setup a canonical DTBS object.
  CCanonicalDtbs root = new CCanonicalDtbs();

  root.setItem( "_docType", "purchase_order" );
  root.setItem( "_signDate", new Date() );

  CCanonicalDtbs doc = root.addSection( "document" );

  doc.setItem( "vendor", "ACME Widgets, Inc." );
  doc.setItem( "po_num", 1234 );
  doc.setItem( "po_date", new Date() );

  // Put in a sub-section. Can be nested as far as necessary.
  CCanonicalDtbs items = doc.addSection( "po_items" );

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
```

This code produces the following canonical XML:

```
<s><i n="_docType">purchase_order</i><i n="_signDate">2021-08-24T20:13:32Z</i><s n="document"><i n="po_date">2021-08-24T20:13:32Z</i><s n="po_items"><i n="computer">890</i><i n="keyboard">50</i><i n="monitor">500</i><i n="mouse">30</i></s><i n="po_num">1234</i><i n="vendor">ACME Widgets, Inc.</i></s></s>
```

The canonical XML (above) has the following structure:

```
<s>
  <i n="_docType">purchase_order</i>
  <i n="_signDate">2021-08-24T20:13:32Z</i>
  <s n="document">
    <i n="po_date">2021-08-24T20:13:32Z</i>
    <s n="po_items">
      <i n="computer">890</i>
      <i n="keyboard">50</i>
      <i n="monitor">500</i>
      <i n="mouse">30</i>
    </s>
    <i n="po_num">1234</i>
    <i n="vendor">ACME Widgets, Inc.</i>
  </s>
</s>
```

The XML format is very simple and designed to be as space-efficient as possible.  The `<s>` tag is a section and the `<i>` tag is an item.  The `n` attribute is the name of the section or item.  Notice that the items in a section are in order by name.  Also note that the `Date` objects are all in a specific ISO 8601 compliant format and in the UTC timezone.  `CCanonicalDtbs.setItem()` is overloaded for common data types so that the data is always represented in exactly the same way.

## Building the Code

The code is a Maven project so building is simple.  You have to have a Java JDK and Maven installed and working.

```
$ mvn install
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------< com.example.code:dbsign-signature-library >--------------
[INFO] Building dbsign-signature-library 0.0.1
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[...]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.623 s
[INFO] Finished at: 2021-08-24T15:37:31-05:00
[INFO] ------------------------------------------------------------------------
```

The resulting jar file will be in the `target` folder.

```
$ ls -l target/*.jar
-rwxrwxrwx 1 root root 59774 Aug 24 15:37 target/dbsign-signature-library-0.0.1.jar
```
