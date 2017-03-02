package com.moriarty.user.contacts.Others;

import android.content.SharedPreferences;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;  


public class MapToXML {
	
	public static HashMap<String,HashMap<String, String>> listToMap(ArrayList<HashMap<String, String>> list){
		HashMap<String,HashMap<String, String>> map=new HashMap<>();
		for(int i=0;i<list.size()-1;i++){
			map.put("C"+list.get(i).get("phone"), list.get(i));
		}
        map.put("M"+list.get(list.size()-1).get("phone"),list.get(list.size()-1));
		return map;
	}

	
	public static Document map2xml(HashMap<String,HashMap<String, String>> map, String rootName) 
			throws DocumentException, IOException  {  
		
		//HashMap<String,HashMap<String, String>> map=listToMap(list);
        Document doc = DocumentHelper.createDocument();  
        Element root = DocumentHelper.createElement(rootName);  
        doc.add(root);  
        map2xml(map, root);  
        //System.out.println(doc.asXML());  
        //System.out.println(formatXml(doc));  
        return doc;  
    } 
	
	public static Document map2xml2(HashMap<String,String> map, String rootName)
			throws DocumentException, IOException  {  
		
		//HashMap<String,HashMap<String, String>> map=listToMap(list);
        Document doc = DocumentHelper.createDocument();  
        Element root = DocumentHelper.createElement(rootName);  
        doc.add(root);  
        map2xml2(map, root);  
        //System.out.println(doc.asXML());  
        //System.out.println(formatXml(doc));  
        return doc;  
    }
	
	private static Element map2xml(Map<String, HashMap<String, String>> map, Element body) {  
        Iterator<Map.Entry<String, HashMap<String, String>>> entries = map.entrySet().iterator();  
        while (entries.hasNext()) {  
            Map.Entry<String, HashMap<String, String>> entry = entries.next();  
            String key = entry.getKey();  
            Object value = entry.getValue();  
            if(key.startsWith("@")){
                body.addAttribute(key.substring(1, key.length()), value.toString());  
            } else if(key.equals("#text")){
                body.setText(value.toString());  
            } else {  
                if(value instanceof java.util.List ){  
                    List list = (List)value;  
                    Object obj;  
                    for(int i=0; i<list.size(); i++){  
                        obj = list.get(i);
                        if(obj instanceof java.util.Map){  
                            Element subElement = body.addElement(key);  
                            map2xml((Map)list.get(i), subElement);  
                        } else {  
                            body.addElement(key).setText((String)list.get(i));  
                        }  
                    }  
                } else if(value instanceof java.util.Map ){  
                    Element subElement = body.addElement(key);  
                    map2xml((Map)value, subElement);  
                } else {  
                    body.addElement(key).setText(value.toString());  
                }  
            }  
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  
        }  
        return body;  
    } 
	
	private static Element map2xml2(Map<String, String> map, Element body) {
        Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();  
        while (entries.hasNext()) {  
            Map.Entry<String, String> entry = entries.next();  
            String key = entry.getKey();  
            Object value = entry.getValue();  
            if(key.startsWith("@")){
                body.addAttribute(key.substring(1, key.length()), value.toString());  
            } else if(key.equals("#text")){
                body.setText(value.toString());  
            } else {  
                if(value instanceof java.util.List ){  
                    List list = (List)value;  
                    Object obj;  
                    for(int i=0; i<list.size(); i++){  
                        obj = list.get(i);
                        if(obj instanceof java.util.Map){  
                            Element subElement = body.addElement(key);  
                            map2xml((Map)list.get(i), subElement);  
                        } else {  
                            body.addElement(key).setText((String)list.get(i));  
                        }  
                    }  
                } else if(value instanceof java.util.Map ){  
                    Element subElement = body.addElement(key);  
                    map2xml((Map)value, subElement);  
                } else {  
                    body.addElement(key).setText(value.toString());  
                }  
            }  
            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  
        }  
        return body;  
    }
	
	public static String formatXml(String xmlStr) throws DocumentException, IOException  {  
        Document document = DocumentHelper.parseText(xmlStr);  
        return formatXml(document);  
    }  
	
	public static String formatXml(Document document) throws DocumentException, IOException  {
        OutputFormat format = OutputFormat.createPrettyPrint();  
        //format.setEncoding("UTF-8");  
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(document);  
        xmlWriter.close();  
        return writer.toString();  
    }
}
