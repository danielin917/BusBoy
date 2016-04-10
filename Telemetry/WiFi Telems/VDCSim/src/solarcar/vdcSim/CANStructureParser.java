package solarcar.vdcSim;


import com.sun.org.apache.xerces.internal.dom.DeferredCommentImpl;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class CANStructureParser {

    private Document doc;
	DefaultListModel<Message> listModel;

	public DefaultListModel<Message> getListModel() {
		return listModel;
	}

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
		
		listModel = new DefaultListModel<>();
    }

    /**
     * Parse MessageParser.xml and store it in a HashMap where the key is the
     * CAN message ID and the value is a list of MessageParsers
     */
    public void parse() {

        NodeList nodes = null;
        NodeList childNodes;

        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            XPathExpression expr = xpath.compile("/document/canmsg[@id]");
            nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException xee) {
            Logger.getLogger(CANStructureParser.class.getName()).log(Level.SEVERE, null, xee); 
        }

        for (int i = 0; i < nodes.getLength(); ++i) {

            String name = null;

            Element ele = (Element) nodes.item(i);
            childNodes = ele.getChildNodes();

            if (ele.getAttribute("type").equals("cmd")) { 
				continue;
            }
			
			ArrayList<Data> data = new ArrayList<>();

            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node n = childNodes.item(j);
				
                if (n.getNodeType() != Node.TEXT_NODE && !(n instanceof DeferredCommentImpl)  ) {
                    String type = ((Element) n).getTagName();
					
                    NamedNodeMap nl = n.getAttributes();
                    if (nl != null) {

                        Attr attr = (Attr) nl.getNamedItem("name");
                        if (attr != null) {
                            name = attr.getValue();
                        }
                    }
					
					data.add(new Data(name));
                }
            }
            int addr = Integer.parseInt(ele.getAttribute("address").substring(2), 16);
			Data[] dataArr = new Data[data.size()];
			data.toArray(dataArr);
			listModel.addElement(new Message(ele.getAttribute("id"), addr, dataArr));
        }
    }
}