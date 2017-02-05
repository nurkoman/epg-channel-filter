package org.epg;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class EpgChannelFilter {
    private String epgInputFileName;
    private String epgOutputFileName;
    private String channelsFileName;

    private List<String> channels = new LinkedList<>();

    public EpgChannelFilter(String epgInputFileName, String epgOutputFileName, String channelsFileName) {

        this.epgInputFileName = epgInputFileName;
        this.epgOutputFileName = epgOutputFileName;
        this.channelsFileName = channelsFileName;
    }

    public void execute() throws Exception {
        try (Stream<String> stream = Files.lines(Paths.get(this.channelsFileName))) {
            stream.forEach(l -> {
                if (l.length() > 0) {
                    channels.add(l.trim());
                }
            });
        } catch (IOException e) {
            throw new Exception("Problem with file " + this.channelsFileName);
        }

        if (channels.size()==0) {
            System.out.println("Nothing to do");
            return;
        }

        File inputFile = new File(epgInputFileName);
        inputFile.setReadOnly();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        Node tv = doc.getDocumentElement();
        if (!tv.getNodeName().equals("tv")) {
            throw new Exception("This is not epg file");
        }

        List<Node> toRemove = new LinkedList<>();
        NodeList childs = tv.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            String childName = child.getNodeName();

            if(needRemoveChild(child)) {
                toRemove.add(child);
            }
        }
        toRemove.forEach(n -> tv.removeChild(n));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        DOMSource domSource= new DOMSource(doc);

        StreamResult result = new StreamResult(new File(epgOutputFileName));

        transformer.transform(domSource, result);

    }

    private boolean needRemoveChild(Node child) {
        boolean ret = false;
        switch (child.getNodeName()) {
            case "channel" :
                ret = checkChannel(child, "id");
                break;
            case "programme" :
                ret = checkChannel(child,"channel" );
                break;
        }

        return ret;

    }

    private boolean checkChannel (Node child, String attrName) {
        String channel = child.getAttributes().getNamedItem(attrName).getNodeValue().trim();
        boolean ret = !channels.contains(channel);
        return !channels.contains(channel);
    }


}
