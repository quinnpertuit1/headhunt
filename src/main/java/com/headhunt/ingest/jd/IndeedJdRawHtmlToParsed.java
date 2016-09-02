package com.headhunt.ingest.jd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sagraw200 on 09/06/16.
 */
public class IndeedJdRawHtmlToParsed {

    public IndeedJdRawHtmlToParsed() {}

    public static void main(String[] args) {
        IndeedJdRawHtmlToParsed indeedJdRawHtmlToParsed = new IndeedJdRawHtmlToParsed();

        String inputfile = "src/resources/data/ingest/jd/jd.data.raw";
        String outputfile = "src/resources/data/ingest/jd/jd.data.parsed";
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(inputfile));
            bw = new BufferedWriter(new FileWriter(outputfile));
            String line;
//            int N = 0;
//            int i = 0;
            while( (line = br.readLine()) != null ) {
//                N++;
//                if (N % 10000 == 0) {
//                    i++;
//                    System.out.println(N*i + "  done.");
//                }
                String parsed = indeedJdRawHtmlToParsed.parseJD(line);
                if (parsed.length() > 100) {
                    bw.write(parsed);
                    bw.write("\n");
                }
            }

        } catch (Exception ex) {
            System.err.println("Error: " + ex);
        }

        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                System.err.println("Error: " + e);
            }
        }
        if (bw != null) {
            try {
                bw.close();
            } catch (IOException e) {
                System.err.println("Error: " + e);
            }
        }
    }

    private String parseJD(String urlText) {
        Document doc = Jsoup.parse(urlText);
        Elements elems = doc.select("span[class=summary]");
        StringBuilder builder = new StringBuilder();
        for (Element elem : elems) {
            Element idElem = elem.getElementById("job_summary");
            String text = idElem.toString().replaceAll("<br>"," . ").replaceAll("</p>"," . ").replaceAll("</li>"," . ");
            // remove all html tags
            String t = Jsoup.parse(text).text();
            builder.append(t).append("\n");
        }
        String ret = builder.toString();
        ret = ret.replaceAll("\n", ".").replaceAll("( )+", " ").replaceAll("[.]+", ".").replaceAll("([.] )+", ". ").trim();
        return ret;
    }

    private String parseJDDeep(String urlText) {
        Document doc = Jsoup.parse(urlText);

        StringBuilder general = new StringBuilder();

        List<String> ll = new ArrayList();
        Elements elems = doc.select("span[class=summary]");
        for (Element elem : elems) {
            Element idElem = elem.getElementById("job_summary");
            List<Node> childnodes = idElem.childNodes();

            for (Node childnode : childnodes) {
                boolean isList = false;
                String listContent = "";
                if (childnode instanceof Element) {
                    String childTag = ((Element) childnode).tagName();
                    if (childTag.contains("ul")) {
                        isList = true;
                        listContent = getListFromUL(childnode);
                    } else if (childTag.contains("p") && childnode.toString().contains("<br>")) {
                        isList = true;
                        listContent = getListFromP(childnode);
                    }
                }

                if (!listContent.trim().isEmpty()) {
//                    System.out.println(listContent);
                    general.append(listContent).append("\n");
                } else {
                    Node childParent = childnode.parentNode().parentNode();
                    if (childParent instanceof Element) {
                        String ee = ((Element)childParent).tagName();
                        if (ee.contains("li")) {
                            isList = true;
                        }
                    }

                    String nodeText = "";
                    String nodeTag = "";

                    if (childnode instanceof TextNode) {
                        nodeText = ((TextNode) childnode).text().replaceAll("\n", " ").trim();
                    }
                    else if (childnode instanceof Element) {
                        nodeText = ((Element)childnode).text().replaceAll("\n", " ").trim();
                        String tag = ((Element)childnode).tagName();
                        if (tag != null && tag.equalsIgnoreCase("b") || (childnode.toString().contains("<b>"))) {
                            nodeTag = "b";
                        }
                    }

                    // print
                    if (!isList && ll.isEmpty()) {
//                        general.append(nodeTag).append(".").append(nodeText).append(".");
                    }
                    else if (!isList && !ll.isEmpty()) {
//                        general.append(ll).append(" ");
                        ll = new ArrayList<>();
                    } else {
                        ll.add(nodeText);
                    }

                    if (isList) {
//                        System.out.print(isList + " ");
                    }
                    if (!nodeTag.isEmpty()) {
//                        System.out.print(nodeTag + " ");
                        if (nodeTag.equalsIgnoreCase("b")) {
                            general.append("<key> ");//.append("\n");
                        }
                    }
                    if (!nodeText.isEmpty()) {
//                        System.out.println(nodeText + " ");
                        general.append("").append(nodeText).append("\n");
                    }
                }

            }
        }

//        System.out.println(general.toString());


        // summary : <span id="job_summary" class="summary">
        // job header : <div id="job_header" data-tn-component="jobHeader">
        // <b class="jobtitle"> : job title
        // <span class="company"> : company
        // <span class="location"> : location

        return general.toString();
    }

    private Map<String, String> parseDeepJDStringBuilder(StringBuilder text) {
        // logic : split by \n
        // if not starts with <key> make <key> == 'general', and put in value until get <key>
        // put <key> as key and successive lines in value


        return null;
    }

    private String getListFromUL(Node childnode) {
        List<String> lis = new ArrayList<>();
        String[] lii = childnode.toString().replaceAll("<ul>","").replaceAll("</ul>","").split("<li>");
        for (String li : lii) {
            li = li.replaceAll("</li>","").replaceAll("\\|","").replaceAll("\n","").replaceAll("( )+"," ").trim();
            if (!li.isEmpty()) {
                lis.add(li);
            }
        }
        return String.join(" | ", lis);
    }

    private String getListFromP(Node childnode) {
        List<String> lis = new ArrayList<>();
        String[] lii = childnode.toString().replaceAll("<p>","").replaceAll("</p>","").split("<br>");
        for (String li : lii) {
            li = li.replaceAll("</br>","").replaceAll("\\|","").replaceAll("\n","").replaceAll("( )+"," ").trim();
            if (!li.isEmpty()) {
                lis.add(li);
            }
        }
        return String.join(" | ", lis);
    }

}
