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
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

/**
 * A Map<String,Object>-like class that can transform into canonicalized XML.
 * 
 * Uses "items" and "sections". Items are just string key to string value pairs in the map.
 * Sections are just string key to CCanonicalDtbs objects to represent a hierarchical subsection.
 * 
 * The setItem() functions provide default formats for many data types.
 * 
 * After populating the CCanonicalDtbs object(s), use toXml() to get canonicalized XML.
 * 
 * (Cobbled together from bits existing code in inner classes.
 * There is probably a better way.)
 */
public class CCanonicalDtbs extends TreeMap<String, Object>
{
  private static final long        serialVersionUID  = 9195544078729817534L;

  public static final String       NODE_NAME_SECTION = "s";
  public static final String       NODE_NAME_PARAM   = "i";
  public static final String       NODE_ATTR_NAME    = "n";
  public static final String       NODE_ATTR_TIME    = "t";

  protected CCompareElements       compareElements   = null;

  protected CCanonicalDtbs         sectionParent     = null;

  private static DateTimeFormatter fmtDate           = DateTimeFormatter.ISO_INSTANT;
  private static DecimalFormat     fmtDecimal        = new DecimalFormat( "#0.0###" );
  private static DecimalFormat     fmtInteger        = new DecimalFormat( "#0" );

  public CCanonicalDtbs()
  {
    super();
  }

  public CCanonicalDtbs( CCanonicalDtbs copy )
  {
    this();
    this.putAll( copy );
  }

  public CCanonicalDtbs( Map<String, Object> map )
  {
    this();
    this.putAll( map );
  }

  protected CCanonicalDtbs getParent()
  {
    return this.sectionParent;
  }

  protected void setParent( CCanonicalDtbs sectionParent )
  {
    this.sectionParent = sectionParent;
  }

  protected CCompareElements getCompareElements()
  {
    CCompareElements ceReturn = this.compareElements;

    if ( ceReturn == null )
    {
      if ( this.getParent() != null )
      {
        try
        {
          ceReturn = (CCompareElements) this.getParent().getCompareElements().clone();
          ceReturn.configSection = this;
        }
        catch ( CloneNotSupportedException e )
        {
          e.printStackTrace();
        }

      }
    }

    if ( ceReturn == null )
    {
      ceReturn = new CCompareElements();
      ceReturn.configSection = this;
    }

    return ceReturn;
  }

  protected void setCompareElements( CCompareElements compareElements )
  {
    this.compareElements = compareElements;
  }

  public CCanonicalDtbs getSection( String strSectionName,
                                    boolean bAddIfDoesntExist )
  {
    CCanonicalDtbs secReturn = null;

    Object objTmp = this.get( strSectionName );

    if ( objTmp != null )
    {
      if ( objTmp instanceof CCanonicalDtbs )
      {
        secReturn = (CCanonicalDtbs) objTmp;
      }
    }

    if ( (secReturn == null) && bAddIfDoesntExist )
    {
      secReturn = this.addSection( strSectionName );
    }

    return secReturn;
  }

  public CCanonicalDtbs getSection( String strSectionName )
  {
    return this.getSection( strSectionName, true );
  }

  public CCanonicalDtbs getSection( String strSectionName,
                                    CCanonicalDtbs sectionDefault )
  {
    CCanonicalDtbs sectionReturn = this.getSection( strSectionName, false );
    if ( sectionReturn == null )
    {
      sectionReturn = sectionDefault;
    }
    return sectionReturn;
  }

  public Object getItem( String strItemName )
  {
    return this.getItem( strItemName, null );
  }

  public Object getItem( String strItemName, Object oDefault )
  {
    Object oReturn = oDefault;

    Object strTmp = this.get( strItemName );

    if ( strTmp != null )
    {
      oReturn = strTmp;
    }

    return oReturn;
  }

  public void setItem( String strName, String strValue )
  {
    this.put( strName, valToString( strValue ) );
  }

  public void setItem( String strName, Date dtValue )
  {
    this.setItem( strName, valToString( dtValue ) );
  }

  public void setItem( String strName, short nValue )
  {
    this.setItem( strName, valToString( nValue ) );
  }

  public void setItem( String strName, int nValue )
  {
    this.setItem( strName, valToString( nValue ) );
  }

  public void setItem( String strName, long nValue )
  {
    this.setItem( strName, valToString( nValue ) );
  }

  public void setItem( String strName, float fValue )
  {
    this.setItem( strName, valToString( fValue ) );
  }

  public void setItem( String strName, double fValue )
  {
    this.setItem( strName, valToString( fValue ) );
  }

  public static String valToString( String strValue )
  {
    return strValue;
  }

  public static String valToString( Date dtValue )
  {
    return CCanonicalDtbs.fmtDate.format( Instant.ofEpochSecond( dtValue.getTime()
                                                                 / 1000L ) );
  }

  public static String valToString( short nValue )
  {
    return CCanonicalDtbs.fmtInteger.format( nValue );
  }

  public static String valToString( int nValue )
  {
    return CCanonicalDtbs.fmtInteger.format( nValue );
  }

  public static String valToString( long nValue )
  {
    return CCanonicalDtbs.fmtInteger.format( nValue );
  }

  public static String valToString( float fValue )
  {
    return CCanonicalDtbs.fmtDecimal.format( fValue );
  }

  public static String valToString( double fValue )
  {
    return CCanonicalDtbs.fmtDecimal.format( fValue );
  }

  public CCanonicalDtbs addSection( String strName )
  {
    return this.addSection( strName, null );
  }

  public CCanonicalDtbs addSection( String strName, CCanonicalDtbs section )
  {
    CCanonicalDtbs secReturn = null;

    if ( section != null )
    {
      secReturn = section;
    }
    else
    {
      secReturn = new CCanonicalDtbs();
    }

    secReturn.setParent( this );
    this.put( strName, secReturn );

    return secReturn;
  }

  public String removeItem( String strName )
  {
    String strReturn = null;

    Object oTmp = this.get( strName );
    if ( (oTmp != null) && !(oTmp instanceof CCanonicalDtbs) )
    {
      strReturn = (String) this.remove( strName );
    }

    return strReturn;
  }

  public CCanonicalDtbs removeSection( String strName )
  {
    CCanonicalDtbs secReturn = null;

    Object oTmp = this.get( strName );
    if ( oTmp instanceof CCanonicalDtbs )
    {
      secReturn = (CCanonicalDtbs) this.remove( strName );
    }

    return secReturn;
  }

  public Set<String> getSectionNames()
  {
    Set<String> setReturn = new TreeSet<>();

    for ( Map.Entry<String, Object> entry: this.entrySet() )
    {
      Object oVal = entry.getValue();
      if ( oVal instanceof CCanonicalDtbs )
      {
        setReturn.add( entry.getKey() );
      }
    }

    return setReturn;
  }

  public Set<String> getItemNames()
  {
    Set<String> setReturn = new TreeSet<>();

    for ( Map.Entry<String, Object> entry: this.entrySet() )
    {
      Object oVal = entry.getValue();
      if ( !(oVal instanceof CCanonicalDtbs) )
      {
        setReturn.add( entry.getKey() );
      }
    }

    return setReturn;
  }

  private static CCanonicalDtbs parse( CXmlNode node )
  {
    CCanonicalDtbs mapReturn = new CCanonicalDtbs();

    for ( CXmlNode nodeCur: node.getChildren() )
    {
      String strNodeName = nodeCur.getName();
      String strAttrName = nodeCur.getAttribute( CCanonicalDtbs.NODE_ATTR_NAME );
      String strNodeValue = nodeCur.getValue();

      if ( strNodeName != null )
      {
        if ( strNodeName.equalsIgnoreCase( CCanonicalDtbs.NODE_NAME_PARAM )
             && (strAttrName != null) && !nodeCur.hasChildren() )
        {
          mapReturn.setItem( strAttrName, strNodeValue );
        }
        else if ( strNodeName.equalsIgnoreCase( CCanonicalDtbs.NODE_NAME_SECTION )
                  && (strAttrName != null) && nodeCur.hasChildren() )
        {
          mapReturn.addSection( strAttrName, CCanonicalDtbs.parse( nodeCur ) );
        }
      }
    }

    return mapReturn;
  }

  public static CCanonicalDtbs parse( String strXml ) throws IOException
  {
    return CCanonicalDtbs.parse( new ByteArrayInputStream( strXml.getBytes( StandardCharsets.UTF_8 ) ) );
  }

  public static CCanonicalDtbs parse( InputStream isXml ) throws IOException
  {
    CCanonicalDtbs mapReturn = null;

    CXmlNode node = new CXmlNode();
    node.fromInputStream( isXml );

    mapReturn = CCanonicalDtbs.parse( node );

    return mapReturn;
  }

  protected static void copyStream( InputStream in,
                                    OutputStream out,
                                    int nBufSize ) throws IOException
  {
    if ( (in == null) || (out == null) )
    {
    }
    else
    {
      byte[] baBuffer = new byte[nBufSize];
      int nBytesRead = 0;

      while ( (nBytesRead = in.read( baBuffer )) >= 0 )
      {
        out.write( baBuffer, 0, nBytesRead );
        out.flush();
      }
    }
  }

  protected static void serialize( CCanonicalDtbs mapSection,
                                   OutputStream osXml ) throws ParserConfigurationException, IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, TransformException
  {
    Document doc = CCanonicalDtbs.toXmlDocument( mapSection );
    Data data = new NodeSetDataImpl( doc,
                                     NodeSetDataImpl.getNoInterTagWsFilter() );
    // Data data = new OctetStreamData( new FileInputStream( strXmlFileIn ) );
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance( "DOM" );
    CanonicalizationMethod canonicalizationMethod = fac.newCanonicalizationMethod( "http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
                                                                                   (C14NMethodParameterSpec) null );
    // Doing the actual canonicalization
    OctetStreamData transformedData = (OctetStreamData) canonicalizationMethod.transform( data,
                                                                                          null );

    // Write the output
    CCanonicalDtbs.copyStream( transformedData.getOctetStream(), osXml, 1024 );
  }

  private static String valueToString( Object oValue )
  {
    String strReturn = oValue.toString();
    if ( oValue instanceof Number )
    {
      if ( (oValue instanceof Byte) || (oValue instanceof Short)
           || (oValue instanceof Integer) || (oValue instanceof Long)
           || (oValue instanceof BigInteger) )
      {
        strReturn = CCanonicalDtbs.fmtInteger.format( ((Number) oValue).longValue() );
      }
      else
      {
        strReturn = CCanonicalDtbs.fmtDecimal.format( ((Number) oValue).doubleValue() );
      }
    }
    return strReturn;
  }

  private static Element toXmlElement( Document doc,
                                       String strName,
                                       Object oVal )
  {
    Element elReturn = null;

    if ( oVal instanceof CCanonicalDtbs )
    {
      elReturn = doc.createElement( CCanonicalDtbs.NODE_NAME_SECTION );

      if ( strName != null )
      {
        elReturn.setAttribute( CCanonicalDtbs.NODE_ATTR_NAME, strName );
      }

      CCanonicalDtbs mapSection = (CCanonicalDtbs) oVal;
      List<Object> listKeys = Arrays.asList( mapSection.keySet().toArray() );
      Collections.sort( listKeys, mapSection.getCompareElements() );
      for ( Object strChildName: listKeys )
      {
        Object oChildVal = mapSection.get( strChildName );

        if ( !(oChildVal instanceof CCanonicalDtbs) )
        {
          oChildVal = CCanonicalDtbs.valueToString( oChildVal );
        }

        elReturn.appendChild( CCanonicalDtbs.toXmlElement( doc,
                                                           (String) strChildName,
                                                           oChildVal ) );
      }
    }
    else
    {
      elReturn = doc.createElement( CCanonicalDtbs.NODE_NAME_PARAM );
      elReturn.setAttribute( CCanonicalDtbs.NODE_ATTR_NAME, strName );
      // elReturn.setTextContent( oVal.toString() );
      if ( oVal != null )
      {
        elReturn.appendChild( doc.createTextNode( oVal.toString() ) );
      }
    }

    return elReturn;
  }

  private static Document toXmlDocument( CCanonicalDtbs mapConfig ) throws ParserConfigurationException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
    factory.setAttribute( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
    factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document docReturn = builder.newDocument();
    Element elRoot = CCanonicalDtbs.toXmlElement( docReturn, null, mapConfig );
    docReturn.appendChild( elRoot );
    // docReturn.normalizeDocument();
    return docReturn;
  }

  public String toXml()
  {
    String strReturn = "";
    try
    {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      CCanonicalDtbs.serialize( this, os );
      strReturn = os.toString( "UTF-8" );
    }
    catch ( Exception e )
    {
    }
    return strReturn;
  }

  @Override
  public boolean equals( Object that )
  {
    return super.equals( that );
  }

  @Override
  public int hashCode()
  {
    return super.hashCode();
  }

  /**
   * Class to sort the items in a CCanonicalDtbs
   */
  protected class CCompareElements implements Comparator<Object>, Cloneable
  {
    private CCanonicalDtbs configSection = null;

    public CCompareElements()
    {
      this.configSection = CCanonicalDtbs.this;
    }

    @Override
    public int compare( Object oThing1, Object oThing2 )
    {
      int nReturn = 0;

      // The following two if's put all the regular values first and the sections last.
      // Seems best to keep them in item order.
      // if ( !(this.configSection.get( oThing1 ) instanceof CCanonicalDtbs)
      // && (this.configSection.get( oThing2 ) instanceof CCanonicalDtbs) )
      // {
      // nReturn = -1;
      // }
      // else if ( (this.configSection.get( oThing1 ) instanceof CCanonicalDtbs)
      // && !(this.configSection.get( oThing2 ) instanceof CCanonicalDtbs) )
      // {
      // nReturn = 1;
      // }
      // else

      if ( !(this.configSection.get( oThing1 ) instanceof CCanonicalDtbs)
           && !(this.configSection.get( oThing2 ) instanceof CCanonicalDtbs) )
      {
        nReturn = this.compareItemNames( oThing1, oThing2 );
      }
      else if ( (this.configSection.get( oThing1 ) instanceof CCanonicalDtbs)
                && (this.configSection.get( oThing2 ) instanceof CCanonicalDtbs) )
      {
        nReturn = this.compareSectionNames( oThing1, oThing2 );
      }

      return nReturn;
    }

    public int compareItemNames( Object oThing1, Object oThing2 )
    {
      return (((String) oThing1).compareTo( (String) oThing2 ));
    }

    public int compareSectionNames( Object oThing1, Object oThing2 )
    {
      return (((String) oThing1).compareTo( (String) oThing2 ));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
      return super.clone();
    }
  }

  /**
   * Class to help with XML canonicalization
   */
  @SuppressWarnings( "rawtypes" )
  protected static class NodeSetDataImpl implements NodeSetData, Iterator
  {
    private Node              ivNode;
    private NodeFilter        ivNodeFilter;
    private Document          ivDocument;
    private DocumentTraversal ivDocumentTraversal;
    private NodeIterator      ivNodeIterator;
    private Node              ivNextNode;

    public NodeSetDataImpl( Node pNode, NodeFilter pNodeFilter )
    {
      this.ivNode = pNode;
      this.ivNodeFilter = pNodeFilter;

      if ( this.ivNode instanceof Document )
      {
        this.ivDocument = (Document) this.ivNode;
      }
      else
      {
        this.ivDocument = this.ivNode.getOwnerDocument();
      }

      this.ivDocumentTraversal = (DocumentTraversal) this.ivDocument;
    }

    private NodeSetDataImpl( NodeIterator pNodeIterator )
    {
      this.ivNodeIterator = pNodeIterator;
    }

    @Override
    public Iterator iterator()
    {
      NodeIterator nodeIterator = this.ivDocumentTraversal.createNodeIterator( this.ivNode,
                                                                               NodeFilter.SHOW_ALL,
                                                                               this.ivNodeFilter,
                                                                               false );
      return new NodeSetDataImpl( nodeIterator );
    }

    private Node checkNextNode()
    {
      if ( (this.ivNextNode == null) && (this.ivNodeIterator != null) )
      {
        this.ivNextNode = this.ivNodeIterator.nextNode();
        if ( this.ivNextNode == null )
        {
          this.ivNodeIterator.detach();
          this.ivNodeIterator = null;
        }
      }
      return this.ivNextNode;
    }

    private Node consumeNextNode()
    {
      Node nextNode = this.checkNextNode();
      this.ivNextNode = null;
      return nextNode;
    }

    @Override
    public boolean hasNext()
    {
      return this.checkNextNode() != null;
    }

    @Override
    public Node next()
    {
      Node next = this.consumeNextNode();
      return next;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException( "Removing nodes is not supported." );
    }

    public static NodeFilter getRootNodeFilter()
    {
      return new NodeFilter()
      {
        @Override
        public short acceptNode( Node pNode )
        {
          if ( (pNode instanceof Element)
               && (pNode.getParentNode() instanceof Document) )
          {
            return NodeFilter.FILTER_SKIP;
          }
          return NodeFilter.FILTER_ACCEPT;
        }
      };
    }

    /**
     * NodeFilter to not inject any inter-tag whitespace
     */
    public static NodeFilter getNoInterTagWsFilter()
    {
      return new NodeFilter()
      {
        @Override
        public short acceptNode( Node node )
        {
          short shortReturn = NodeFilter.FILTER_ACCEPT;

          if ( node.getNodeType() == Node.TEXT_NODE )
          {
            boolean bIsValueNode = ((node.getParentNode() != null)
                                    && (node.getParentNode().getChildNodes().getLength() == 1));

            if ( !bIsValueNode )
            {
              shortReturn = NodeFilter.FILTER_REJECT;
            }
          }

          return shortReturn;
        }

      };
    }

  }

  /**
   * Class to parse XML into nested maps
   */
  protected static class CXmlNode implements Serializable
  {
    private static final long     serialVersionUID = 1L;
    public static final String    ENDL             = System.getProperty( "line.separator" );

    private String                strNodeName      = "";
    private String                strNodeValue     = "";
    private Map<String, String>   hashAttrs        = new HashMap<>();
    private List<CXmlNode>        vChildren        = new ArrayList<>();
    private CXmlNode              nodeParent       = null;
    private Map<String, CXmlNode> mapNameToChild   = new HashMap<>();

    public CXmlNode()
    {
    }

    public CXmlNode( CXmlNode nodeXml )
    {
      this.setParent( nodeXml );
    }

    private void setName( String strValue )
    {
      this.strNodeName = strValue;
    }

    public String getName()
    {
      return this.strNodeName;
    }

    private void setParent( CXmlNode node )
    {
      this.nodeParent = node;
    }

    public CXmlNode getParent()
    {
      return this.nodeParent;
    }

    private void setValue( String strValue )
    {
      if ( strValue == null )
      {
        strValue = "";
      }
      this.strNodeValue = strValue;
    }

    public String getValue()
    {
      return this.strNodeValue;
    }

    public CXmlNode getChildByAttributeValue( String strAttrName,
                                              String strValue )
    {
      CXmlNode retVal = new CXmlNode();
      int nNumKids = this.getNumChildren();

      for ( int i = 0; i < nNumKids; i++ )
      {
        retVal = this.getChildByIndex( i );
        Map<String, String> tmpHash = retVal.getAttributes();

        String x = tmpHash.get( strAttrName );
        if ( x.equals( strValue ) )
        {
          break;
        }
      }

      return retVal;
    }

    public void addChild( CXmlNode nodeChild )
    {
      if ( this.mapNameToChild.get( nodeChild.getName() ) == null )
      {
        this.mapNameToChild.put( nodeChild.getName(), nodeChild );
      }
      this.vChildren.add( nodeChild );
    }

    public List<CXmlNode> getChildren()
    {
      return this.vChildren;
    }

    public boolean hasChildren()
    {
      return !(this.vChildren.isEmpty());
    }

    public int getNumChildren()
    {
      return this.getChildren().size();
    }

    public CXmlNode getChildByName( String strChildName )
    {
      return this.mapNameToChild.get( strChildName );
    }

    public CXmlNode getChildByIndex( int nIndex )
    {
      CXmlNode nodeReturn = null;
      try
      {
        nodeReturn = this.getChildren().get( nIndex );
      }
      catch ( IndexOutOfBoundsException e )
      {
        nodeReturn = null;
      }

      return nodeReturn;
    }

    public String getChildValue( String strChildId )
    {
      CXmlNode nodeCur = this;
      CXmlNode nodeFound = null;
      StringTokenizer strtokSlash = new StringTokenizer( strChildId, "/" );
      while ( strtokSlash.hasMoreElements() )
      {
        String strCurName = (String) strtokSlash.nextElement();
        int nChildIndex = -1;
        int nOpenBracket = strCurName.lastIndexOf( "[" );
        if ( nOpenBracket > 0 )
        {
          int nCloseBracket = strCurName.lastIndexOf( "]" );
          if ( (nCloseBracket > 0) && (nCloseBracket > nOpenBracket)
               && (nCloseBracket > (nOpenBracket + 1)) )
          {
            String strChildIndex = strCurName.substring( nOpenBracket + 1,
                                                         nCloseBracket );
            if ( strChildIndex != null )
            {
              strChildIndex = strChildIndex.trim();
              if ( strChildIndex.length() > 0 )
              {
                try
                {
                  nChildIndex = Integer.parseInt( strChildIndex.trim() );
                  strCurName = strCurName.substring( 0, nOpenBracket );
                }
                catch ( NumberFormatException e )
                {
                }
              }
            }
          }
        }
        nodeFound = nodeCur.getChildByName( strCurName );
        if ( nodeFound == null )
        {
          break;
        }
        if ( nChildIndex >= 0 )
        {
          nodeFound = nodeFound.getChildByIndex( nChildIndex );
          if ( nodeFound == null )
          {
            break;
          }
        }
        nodeCur = nodeFound;
      }

      return (nodeFound == null) ? (null) : (nodeFound.getValue());
    }

    public void fromInputStream( InputStream isXml ) throws IOException
    {
      Document document = null;
      try
      {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        factory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
        factory.setAttribute( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
        factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse( isXml );
      }
      catch ( SAXException sEx )
      {
        System.out.println( sEx.toString() );
      }
      catch ( ParserConfigurationException pcEx )
      {
        System.out.println( pcEx.toString() );
      }

      if ( document != null )
      {
        Node rootNode = document.getFirstChild();
        this.makeXmlNode( rootNode, this );
      }
    }

    public void fromXmlFile( String strFileName ) throws IOException
    {
      this.fromInputStream( new FileInputStream( strFileName ) );
    }

    private boolean isGenericItemNode()
    {
      return !this.hashAttrs.isEmpty();
    }

    public Map<String, String> getAttributes()
    {
      return this.hashAttrs;
    }

    public String getAttribute( String strName )
    {
      return this.hashAttrs.get( strName );
    }

    private void setAttributes( Map<String, String> attrs )
    {
      this.hashAttrs = attrs;
    }

    private boolean isValueNode( Node node )
    {
      boolean isEmpty = false;
      if ( !node.hasChildNodes() )
      {
        isEmpty = true;
      }

      boolean bIsValueNode = ((node.getNodeType() == Node.ELEMENT_NODE)
                              && (node.getChildNodes().getLength() == 1)
                              && (node.getFirstChild().getNodeType() == Node.TEXT_NODE))
                             || (isEmpty
                                 && (node.getNodeType() == Node.ELEMENT_NODE));

      return bIsValueNode;
    }

    private boolean isContainerNode( Node node )
    {
      boolean bIsContainerNode = (!this.isValueNode( node ))
                                 && (node.getNodeType() == Node.ELEMENT_NODE)
                                 && (node.getChildNodes().getLength() > 0);
      return bIsContainerNode;
    }

    private void makeXmlNode( Node nodeFrom, CXmlNode nodeTo )
    {
      NamedNodeMap attrs = nodeFrom.getAttributes();
      HashMap<String, String> hashTempAttrs = new HashMap<>();

      if ( attrs != null )
      {
        for ( int i = 0; i < attrs.getLength(); i++ )
        {
          hashTempAttrs.put( attrs.item( i ).getNodeName(),
                             attrs.item( i ).getNodeValue() );
        }
      }

      /* if( !hashTempAttrs.isEmpty() ) System.out.println( hashTempAttrs ); */

      if ( this.isValueNode( nodeFrom ) )
      {
        CXmlNode nodeNew = new CXmlNode( nodeTo );
        nodeNew.setName( nodeFrom.getNodeName() );
        nodeNew.setAttributes( hashTempAttrs );

        if ( nodeFrom.hasChildNodes() )
        {
          nodeNew.setValue( nodeFrom.getFirstChild().getNodeValue() );
        }
        else
        {
          nodeNew.setValue( "" );
        }

        nodeTo.addChild( nodeNew );
      }
      else if ( this.isContainerNode( nodeFrom ) )
      {
        boolean bAtRoot = (nodeFrom.getParentNode() == null)
                          || nodeFrom.getParentNode().getNodeName().equals( "#document" );
        CXmlNode nodeParentForChildren = null;
        // If I'm on the very first node, just set info
        if ( bAtRoot )
        {
          nodeTo.setName( nodeFrom.getNodeName() );
          nodeParentForChildren = nodeTo;
        }
        // Otherwise allocate new node
        else
        {
          CXmlNode nodeNew = new CXmlNode( nodeTo );
          nodeNew.setAttributes( hashTempAttrs );
          nodeNew.setName( nodeFrom.getNodeName() );
          nodeTo.addChild( nodeNew );
          nodeParentForChildren = nodeNew;
        }
        NodeList myChildren = nodeFrom.getChildNodes();
        for ( int i = 0; i < myChildren.getLength(); i++ )
        {
          this.makeXmlNode( myChildren.item( i ), nodeParentForChildren );
        }
      }
    }

    public void printNodeTree()
    {
      this.printNodeTree( this, 0 );
    }

    private String spaces( int n )
    {
      StringBuffer buff = new StringBuffer();

      for ( int i = 0; i < n; i++ )
      {
        buff.append( " " );
      }

      return buff.toString();
    }

    private void printNodeTree( CXmlNode node, int nLevel )
    {
      if ( node.isGenericItemNode() )
      {
        System.out.println( this.spaces( nLevel * 2 ) + node.getName() + " "
                            + node.getAttributes() + ": " + node.getValue() );
      }
      else
      {
        if ( node.hasChildren() )
        {
          System.out.println( this.spaces( nLevel * 2 ) + node.getName() );
        }
        else
        {
          System.out.println( this.spaces( nLevel * 2 ) + node.getName() + "="
                              + node.getValue() );
        }
      }

      for ( int i = 0; i < node.getChildren().size(); i++ )
      {
        CXmlNode nodeXml = node.getChildByIndex( i );
        this.printNodeTree( nodeXml, nLevel + 1 );
      }
    }

    private String strTemp = "";

    @Override
    public String toString()
    {
      this.strTemp = "";
      this.toString( this, 0 );
      return this.strTemp;
    }

    private void toString( CXmlNode node, int nLevel )
    {
      if ( node.hasChildren() )
      {
        this.strTemp = this.strTemp + this.spaces( nLevel * 2 ) + " <"
                       + node.getName() + ">" + CXmlNode.ENDL;
      }
      else
      {
        this.strTemp = this.strTemp + this.spaces( nLevel * 2 ) + " <"
                       + node.getName() + "><![CDATA[" + node.getValue()
                       + "]]>";
      }

      for ( int i = 0; i < node.getChildren().size(); i++ )
      {
        CXmlNode nodeXml = node.getChildByIndex( i );
        this.toString( nodeXml, nLevel + 1 );
      }

      if ( node.getValue().equals( "" ) && node.hasChildren() )
      {
        this.strTemp = this.strTemp + this.spaces( nLevel * 2 ) + " </"
                       + node.getName() + ">" + CXmlNode.ENDL;
      }
      else
      {
        this.strTemp = this.strTemp + " </" + node.getName() + ">"
                       + CXmlNode.ENDL;
      }
    }

    public Element toXmlNode( Document doc )
    {
      Element elReturn = doc.createElement( this.strNodeName );

      for ( String strKey: this.getAttributes().keySet() )
      {
        String strValue = this.getAttribute( strKey );
        elReturn.setAttribute( strKey, strValue );
      }

      if ( this.hasChildren() )
      {
        for ( CXmlNode nodeChild: this.getChildren() )
        {
          elReturn.appendChild( nodeChild.toXmlNode( doc ) );
        }
      }
      else
      {
        // elReturn.setTextContent( this.getValue() );
        elReturn.appendChild( doc.createTextNode( this.getValue() ) );
      }

      return elReturn;
    }

    public Document toXmlDocument() throws ParserConfigurationException
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
      factory.setAttribute( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
      factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document docReturn = builder.newDocument();
      docReturn.appendChild( this.toXmlNode( docReturn ) );
      return docReturn;
    }

  }
}
