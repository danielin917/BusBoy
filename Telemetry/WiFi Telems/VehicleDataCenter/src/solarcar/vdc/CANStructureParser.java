package solarcar.vdc;


import solarcar.vdcPublisher.reverseParsers.ReverseMessageParser;
import solarcar.vdc.parsers.MessageParser;
import solarcar.vdc.parsers.IntBigendianParser;
import solarcar.vdc.parsers.DoubleBigendianParser;
import solarcar.vdc.parsers.UIntBigendianParser;
import solarcar.vdc.parsers.IntParser;
import solarcar.vdc.parsers.ULongBigendianParser;
import solarcar.vdc.parsers.FakeParser;
import solarcar.vdc.parsers.ULongParser;
import solarcar.vdc.parsers.DoubleParser;
import solarcar.vdc.parsers.FloatParser;
import solarcar.vdc.parsers.UIntParser;
import solarcar.vdc.parsers.FloatBigendianParser;
import solarcar.vdc.parsers.DataParser;
import com.sun.org.apache.xerces.internal.dom.DeferredCommentImpl;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class CANStructureParser {

    private HashMap<Integer, MessageParser> messageMap;
	private HashMap<String, ReverseMessageParser> reverseMessageMap;
    private Document doc;

    /**
     * @param docPath Path to the XML file
     */
    public CANStructureParser(String docPath) {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        try {
            DocumentBuilder db = domFactory.newDocumentBuilder();
            doc = db.parse(docPath);
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            Logger.getLogger(CANStructureParser.class.getName()).log(Level.SEVERE, null, pce); 
        }
        messageMap = new HashMap<>();
		reverseMessageMap = new HashMap<>();
    }

    /**
     * Parse MessageParser.xml and store it in a HashMap where the key is the
     * CAN message ID and the value is a list of MessageParsers
     */
    public void parse() {

        NodeList nodes = null;
        NodeList childNodes;
        MessageParser mp;
		ReverseMessageParser rmp;
        ArrayList<DataParser> dataparsers;

        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            XPathExpression expr = xpath.compile("/document/canmsg[@id]");
            nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException xee) {
            Logger.getLogger(CANStructureParser.class.getName()).log(Level.SEVERE, null, xee); 
        }

        for (int i = 0; i < nodes.getLength(); ++i) {

            String name = null;
            boolean bigendian = false;
            double mult, off;
            int startbit = 0;
            int length = 0;
            int value = 0;

            Element ele = (Element) nodes.item(i);
            childNodes = ele.getChildNodes();

            if (ele.getAttribute("type").equals("cmd")) { 
                rmp = new ReverseMessageParser(new DataParser[0], ele.getAttribute("address"), false);
				reverseMessageMap.put(ele.getAttribute("id"), rmp);
				continue;
            }

            dataparsers = new ArrayList<>();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node n = childNodes.item(j);
				
                if (n.getNodeType() != Node.TEXT_NODE && !(n instanceof DeferredCommentImpl)  ) {
                    String type = ((Element) n).getTagName();

                    if (type.equals("bigendian")) {
                        bigendian = true;
                    }

                    NamedNodeMap nl = n.getAttributes();
                    if (nl != null) {

                        Attr attr = (Attr) nl.getNamedItem("name");
                        if (attr != null) {
                            name = attr.getValue();
                        }

                        attr = (Attr) nl.getNamedItem("mult");
                        if (attr != null) {
                            mult = Double.parseDouble(attr.getValue());
                        } else {
                            mult = 1;
                        }

                        attr = (Attr) nl.getNamedItem("off");
                        if (attr != null) {
                            off = Double.parseDouble(attr.getValue());
                        } else {
                            off = 0;
                        }

                        attr = (Attr) nl.getNamedItem("length");
                        if (attr != null) {
                            length = Byte.parseByte(attr.getValue());
                        }

                        attr = (Attr) nl.getNamedItem("startbit");
                        if (attr != null) {
                            startbit = Integer.parseInt(attr.getValue());
                        }

                        attr = (Attr) nl.getNamedItem("value");
                        if (attr != null) {
                            value = Integer.parseInt(attr.getValue());
                        }
                        switch (type) {
                            case "uint":
                                if (length == 64) {
                                    if (bigendian) {
                                        dataparsers.add(new ULongBigendianParser(startbit, length,
                                                mult, off, name));
                                    } else {
                                        dataparsers.add(new ULongParser(startbit, length,
                                                mult, off, name));
                                    }
                                } else if (bigendian) {
                                    dataparsers.add(new UIntBigendianParser(startbit, length,
                                            mult, off, name));
                                } else {
                                    dataparsers.add(new UIntParser(startbit, length,
                                            mult, off, name));
                                }
                                break;
                            case "int":
                                if (bigendian) {
                                    dataparsers.add(new IntBigendianParser(startbit, length,
                                            mult, off, name));
                                } else {
                                    dataparsers.add(new IntParser(startbit, length,
                                            mult, off, name));
                                }
                                break;
                            case "float":
                                if (bigendian) {
                                    dataparsers.add(new FloatBigendianParser(startbit, length,
                                            mult, off, name));
                                } else {
                                    dataparsers.add(new FloatParser(startbit, length,
                                            mult, off, name));
                                }
                                break;
                            case "double":
                                if (bigendian) {
                                    dataparsers.add(new DoubleBigendianParser(startbit, length,
                                            mult, off, name));
                                } else {
                                    dataparsers.add(new DoubleParser(startbit, length,
                                            mult, off, name));
                                }
                                break;
                            case "fake":
                                dataparsers.add(new FakeParser(value, name));
                                break;
                        }
                        startbit += length;
                        startbit %= 64;
                    }
                }
            }
            DataParser[] dp = new DataParser[dataparsers.size()];
            dataparsers.toArray(dp);
			mp = new MessageParser(dp, ele.getAttribute("id"));
            int addr = Integer.parseInt(ele.getAttribute("address").substring(2), 16);
            messageMap.put(addr, mp);
        }
    }

    public HashMap<Integer, MessageParser> getMap() {
        return messageMap;
    }

	public HashMap<String, ReverseMessageParser> getReverseMap() {
        return reverseMessageMap;
    }
	
    public void printMap() {
        for (Entry<Integer, MessageParser> entry : messageMap.entrySet()) {
            int key = entry.getKey();
            System.out.println("<" + key + ">");
            MessageParser value = entry.getValue();
            DataParser[] subP = value.subParsers;
            for (int i = 0; i < subP.length; ++i) {
                System.out.printf("startBit: %d, mult: %f, "
                        + "off: %f, name: %s, \n", subP[i].startBit,
                        subP[i].multiplier, subP[i].offset, subP[i].name);
            }
            System.out.println();
        }
    }

    public void printMapEntry(String addr) {
        MessageParser mp = getMap().get(addr);
        System.out.println(mp.id);
        DataParser[] subP = mp.subParsers;
        for (int i = 0; i < subP.length; ++i) {
            System.out.printf("startBit: %d, mult: %f, "
                    + "off: %f, name: %s, \n", subP[i].startBit,
                    subP[i].multiplier, subP[i].offset, subP[i].name);
        }
    }
}