# dbsign-signature-library

Table of Contents:
- [dbsign-signature-library](#dbsign-signature-library)
  - [Basic Use of the API](#basic-use-of-the-api)
  - [Canonical DTBS](#canonical-dtbs)
  - [Building the Code](#building-the-code)

This is a simple library that takes care of many of the details of interacting with the DBsign Server.  

It is called from server side Java code and contains classses and APIs that help with

- Formatting and making HTTP requests to the DBsign Server
- Parsing responses from the DBsign Server into easily usable objects
- Managing database records for the DBsign DTBS-ID feature.
- Creating canonical XML to format data to be signed (DTBS).

The dbsign-singature-api can be used to sign various types of data including PDF files, text data, binary data, etc.  It also has a class called CCanonicalXml which allows you to use a `java.util.Map` type interface to create structured data to be signed and always generate exactly the same XML for the same data.

## Basic Use of the API

The Java code of the dbsign-signature-library is pretty simple and can be read or modified to meet your requirements.  Essentially, you just 

- make an instance of the `CSignatureAPI` object,
- create and populate a `CSignatureOptions*` object,
- pass the the CSignatureOptions object to the API's `sign*()` or `verify*()` functions, and
- Receive the result of the operation in a `CSignatureResult` object.

There are also functions in the API for inserting, fetching, and deleting database records for DBsign's DTBS-ID feature.  The DTBS-ID feature allows the host application to use the database to communicate potentially large DTBS blocks via database tables.  There are also API helper functions for obtaining database connections from a JNDI `java.util.DataSource` and also just creating connections using a JDBC driver.  

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

There are two example driver programs in the `com.signature.code` package.

## Canonical DTBS

> *Canonicalization: a process for converting data that has more than one possible representation into a "standard", "normal", or canonical form.*

Data canonicalization is important when data is digitally signed because any change in the data will cause a signature verification failure.  The signature library contains a class called `CCanonicalDtbs` to help construct XML data that is always formatted the same way.

The `CCanonicalDtbs` class is just a `Map` that contains "items" which are contained in "sections".  A `CCanonicalDtbs` object itself represents a section and contains both items and references to other `CCanonicalDtbs` sections. Items are just string key to string value mappings inside a section. These recursively defined sections implement a heirarchical data structure.  

A `CanonicalDtbs` class can convert itself to a canonicalized XML via its `toXml()` method.  It can also parse the XML back into a nested `CanonicalDtbs` object with its `parse()` method.

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

The XML format is very simple and designed to be as low-overhead as possible.  The `<s>` tag is a section and the `<i>` tag is an item.  The `n` attribute is the name of the section or item.  Notice that the items in a section are in order.  Also not that the `Date` objects are all in a certain format and the UTC timezone.  `CCanonicalDtbs.setItem()` is overloaded for common data types so that the data is always represented in exactly the same way.

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