package edu.missouristate.csc450.group1.proxyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.*;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHandler implements HttpHandler {
	
	 MysqlConnection sqlConnection;
	private String rivetedJsString;
	private boolean isAlive;
	private static String domainName = "li107-234.members.linode.com";
	private static String page404 = 
			"<!DOCTYPE html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head> <body><p>404. File Not Found.</p> <iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/u3Oron-nbHA?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe></body>";
	public RequestHandler(String riveted){
		rivetedJsString = riveted;
		if(!riveted.equals("")){
			//if we got a valid file
			isAlive = true;
		}
		sqlConnection = new MysqlConnection();
	}
	
	public void handle(HttpExchange HTTPEx) throws IOException{
		if(isAlive){
		String requestMethod = HTTPEx.getRequestMethod();
		if(requestMethod.equals("POST"))
		{
			System.out.println("GOT DATA FROM USER!");
			JSONObject request = new JSONObject(readRequest(HTTPEx.getRequestBody()));
			sqlConnection.addAdLocationVisit(request.getString("url"), request.getString("ad location"), request.getDouble("focus ratio"), request.getDouble("active ratio"), request.getDouble("total time"));
			HTTPEx.sendResponseHeaders(200, 1);
			HTTPEx.getResponseBody().write(0);
		}
		else{
			URI path = HTTPEx.getRequestURI();
			Document response = null;
			if(path.toString().contains("/riveted/riveted")){
				//serve the riveted script
				int fileExt = path.toString().indexOf(".js");
				String number = path.toString().substring(16, fileExt);
				String tempRivetedJsString = rivetedJsString.replace("var riveted", "var riveted" + number);
				byte riveted[] = tempRivetedJsString.getBytes();
				HTTPEx.getResponseHeaders().add("content-type", "text/javascript");
				HTTPEx.sendResponseHeaders(200, riveted.length);
				HTTPEx.getResponseBody().write(riveted);
				
			}
			else{
				try{
				response = getModifiedPage(path.toString(), HTTPEx.getLocalAddress().getHostName());
				if((path.toString().contains(".html")|| path.toString().endsWith("/")) && !sqlConnection.doesPageExistInDB(path.toString())){
					sqlConnection.addPage(path.toString());
					Elements keywordsElement = response.getElementsByAttributeValue("name", "keywords");
					String[] keywords =keywordsElement.get(0).attr("content").split(", ");
					for(String keyword : keywords){
						keyword = keyword.replace("'", "\'");
						if(!sqlConnection.doesKeywordExistInDB(keyword)){
							sqlConnection.addKeywordToDB(keyword);
							
						}
						sqlConnection.addKeywordPage(path.toString(), keyword);
					}
				}
				}
				catch(IOException e){
					System.out.println("Error getting page from KC Star:" + path.toString());
					response = null;
				}
				if(response != null){
					byte responseArray[] = response.toString().getBytes();
					HTTPEx.sendResponseHeaders(200, responseArray.length);
					HTTPEx.getResponseBody().write(responseArray);
				}
				else{
					byte fourOhFourArray[] = page404.getBytes();
					HTTPEx.sendResponseHeaders(404, fourOhFourArray.length);
					HTTPEx.getResponseBody().write(fourOhFourArray);
				}
			}//end not serving riveted
		}//end not a post
		}//end isAlive
		else{
			System.out.println("INVALID RIVETED COPY GIVEN TO THREAD!");
			byte fourOhFourArray[] = page404.getBytes();
			HTTPEx.sendResponseHeaders(404, fourOhFourArray.length);
			HTTPEx.getResponseBody().write(fourOhFourArray);
		}
		HTTPEx.close();
	}//end handle
	
	
	private Document getModifiedPage(String sitePath, String hostName) throws IOException{
			Document page = Jsoup.connect("http://www.kansasCity.com/" + sitePath).get();
			if(!sitePath.contains(".js") && !sitePath.contains(".woff")){
				Elements images = page.select("img:not([src^=http://www.kansascity.com])");
				int imgId = 0;
				for (Element img : images){
					Element wrap = img.wrap("<section id=\""+ imgId + "\"></section>");
					Element parent = img.parent();
					parent.children().remove(img);
					parent.append(wrap.html());
					imgId++;
		
				}
				Elements links = page.select("a[href^=Http://www.kansascity.com]");
				for(Element link : links){
				
				Element newLink = new Element(Tag.valueOf("a"), "");
				newLink.attr("href", link.attr("href").replace("www.kansascity.com", domainName));
				newLink.text(link.text());
				link.replaceWith(newLink);
				
				
			}
				//add all the scripts to the page
			Element scriptLink = new Element(Tag.valueOf("script"), "");

			scriptLink.attr("type", "text/javascript");
			scriptLink.attr("src", "https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js");
			page.select("head").append(scriptLink.toString());
			scriptLink = new Element(Tag.valueOf("script"), "");
			scriptLink.attr("type", "text/javascript");
			scriptLink.attr("src", "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js");
			page.select("head").append(scriptLink.toString());
			for(int i=0; i <imgId; i++){
				scriptLink = new Element(Tag.valueOf("script"), "");
				scriptLink.attr("type", "text/javascript");
				scriptLink.attr("src", "http://" + domainName +"/riveted/riveted" + i +".js");
				page.select("head").append(scriptLink.toString());
				scriptLink = new Element(Tag.valueOf("script"), "");
				scriptLink.text("riveted"+i+".init('" + i + "');");
				page.select("body").append(scriptLink.toString());
			}
			}
		return page;
	}
	
	private String readRequest(InputStream body){
		try{
			InputStreamReader bodyReader = new InputStreamReader(body, "utf-8");
		
		BufferedReader bodybuffer = new BufferedReader(bodyReader);
		int aByte;
		StringBuilder builder = new StringBuilder();
		while((aByte = bodybuffer.read()) != -1){
			builder.append((char) aByte);
		}
		
		bodybuffer.close();
		bodyReader.close();
		return builder.toString();
		}
		catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}
}
