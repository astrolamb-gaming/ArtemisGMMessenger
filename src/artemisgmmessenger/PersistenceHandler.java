/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artemisgmmessenger;

import java.io.File;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 *
 * @author Matthew
 */
public class PersistenceHandler {
    
    private HashMap<String, Object> options;
    
    private Document doc;
    private File persistenceFile;
    
    public PersistenceHandler() {
        try {
            //java.net.URL url = ClassLoader.getSystemResource("resources/persistentGMData.xml");
            //System.out.println(url.getPath());
            //File persistentFile = new File("C:/Users/Matthew/Documents/NetBeansProjects/ArtemisGameMasterConsole/src/resources/persistentGMData.xml");
            //TODO: Figure out how the hell to get File working right :( see: https://stackoverflow.com/questions/14967449/read-from-a-file-that-is-in-the-same-folder-as-the-jar-file
            
            persistenceFile = new File("./resources/persistentGMData.xml");
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            if (!persistenceFile.exists()) {
                doc = docBuilder.newDocument();
                
            } else {
                doc = docBuilder.parse(persistenceFile);
            }
            //parsePersistenceFile();
            
            options = new HashMap<>();
            System.out.println("persistenceHandler generated successfully");
            
            
        } catch (Exception e) {
            System.out.println("Persistence failed");
            e.printStackTrace();
        }
    }
    
    public void parsePersistenceFile() {
        NodeList nl = doc.getChildNodes();
        
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            System.out.println(n.getNodeType());
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                System.out.println(((Element) n).getTagName());
                
            }
            //Element e = (Element) (n.getParentNode());
            
            //System.out.println(e.getTagName());
            //System.out.println(n.getParentNode().getNodeName());
        }
    }
    
    public Object getOption(String name) {
        return options.get(name);
    }
    
    public void setOption(String key, Object o) {
        options.put(key, o);
        
        System.out.println((String)o);
        String s = (String) o;
        //Node newChild = doc.createTextNode(s);
        //Node old = doc.getElementsByTagName(key).item(0).getFirstChild();
        //doc.replaceChild(newChild, old);
        NodeList list = doc.getElementsByTagName(key);
        if (list.getLength() == 0) {
            Node newChild = doc.createTextNode(s);
            Element el = doc.createElement(key);
            el.appendChild(newChild);
        } else {
            doc.getElementsByTagName(key).item(0).getFirstChild().setTextContent(s);
        }
        
        //doc.getElementsByTagName(key).item(0).appendChild(newChild);
        
        XMLWriter.writeDocumentToFile(doc, persistenceFile);
    }
    
    /**public void updatePersistenceFile() {
        for (String s : options.keySet()) {
            doc.getElementsByTagName(s).item(0).setNodeValue(options.get(s).toString());
        }
        
    }*/
    
    

    public static class XMLWriter {
        public static void writeDocumentToFile(Document document, File file) {

            
            try {
                // Make a transformer factory to create the Transformer
                TransformerFactory tFactory = TransformerFactory.newInstance();

                // Make the Transformer
                Transformer transformer = tFactory.newTransformer();

                // Mark the document as a DOM (XML) source
                DOMSource source = new DOMSource(document);

                // Say where we want the XML to go
                StreamResult result = new StreamResult(file);

                // Write the XML to file
                transformer.transform(source, result);
            
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
