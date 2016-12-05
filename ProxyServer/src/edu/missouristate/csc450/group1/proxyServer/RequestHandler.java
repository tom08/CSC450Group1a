package edu.missouristate.csc450.group1.proxyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.*;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHandler implements HttpHandler {
	
	
	private String rivetedJsString;
	private boolean isAlive; //whether or not we have a valid copy of the rivited file
	private static String domainName = "li107-234.members.linode.com";
	private static String page404 = 
			"<!DOCTYPE html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head> <body><p>404. File Not Found.</p> <iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/u3Oron-nbHA?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe></body>";
	public RequestHandler(String riveted){
		rivetedJsString = riveted;
		if(!riveted.equals("")){
			//if we got a valid file
			isAlive = true;
		}
	}
	
	public void handle(HttpExchange HTTPEx) throws IOException{
		if(isAlive){
		String requestMethod = HTTPEx.getRequestMethod();
		if(requestMethod.equals("POST")) //if we're getting data from the browser of users
		{
			JSONObject request = new JSONObject(readRequest(HTTPEx.getRequestBody()));
			boolean postedAdLocationVisit = false;
			while(!postedAdLocationVisit){ //synchronize lock to make sure sqlConnections don't collide
				synchronized(ProxyServer.sqlConnection){
					ProxyServer.sqlConnection.addAdLocationVisit(request.getString("url"), request.getString("ad location"), request.getDouble("focus ratio"), request.getDouble("active ratio"), request.getDouble("total time"));
					postedAdLocationVisit = true;
				}
			}
			HTTPEx.sendResponseHeaders(200, 1); //reply with a basic "OK"
			HTTPEx.getResponseBody().write(0);
		}
		else{
			URI path = HTTPEx.getRequestURI();
			Document response = null;
			if(path.toString().contains("/riveted/riveted")){
				//serve the riveted script
				int fileExt = path.toString().indexOf(".js"); //figure out what number of riveted to serve
				//serve it
				String number = path.toString().substring(16, fileExt); //make the variable for riveted in the file have the right number on it
				String tempRivetedJsString = rivetedJsString.replace("var riveted", "var riveted" + number);
				byte riveted[] = tempRivetedJsString.getBytes();
				HTTPEx.getResponseHeaders().add("content-type", "text/javascript");
				HTTPEx.sendResponseHeaders(200, riveted.length);
				HTTPEx.getResponseBody().write(riveted);
			}
			else{
				//if the client doesn't want riveted get the page off of KC star and modify it
				try{
				
				response = getModifiedPage(path.toString(), HTTPEx.getLocalAddress().getHostName());
				boolean completedPageAndTagsAd = false;
				while(!completedPageAndTagsAd){
				synchronized(ProxyServer.sqlConnection){
				if((path.toString().contains(".html")|| path.toString().endsWith("/")) && !ProxyServer.sqlConnection.doesPageExistInDB(path.toString())){
						//if the page doesn't already exist in the DB
						ProxyServer.sqlConnection.addPage(path.toString()); //add it
						Elements keywordsElement = response.getElementsByAttributeValue("name", "keywords");//get the list of keywords
						String[] keywords =keywordsElement.get(0).attr("content").split(", ");
						for(String keyword : keywords){//add all the keywords to the DB
							keyword = keyword.replace("'", "\'");//escape all apostrophes
							if(!ProxyServer.sqlConnection.doesKeywordExistInDB(keyword)){
								ProxyServer.sqlConnection.addKeywordToDB(keyword);
								
							}
							ProxyServer.sqlConnection.addKeywordPage(path.toString(), keyword);
						}

					}
					completedPageAndTagsAd = true;
					}
				}
				}
				catch(IOException e){
					System.out.println(ZonedDateTime.now(ZoneId.of("America/Chicago")) +" Error getting page from KC Star:" + path.toString());
					response = null;
				}
				if(response != null){//if there is a response to give send it
					byte responseArray[] = response.toString().getBytes();
					HTTPEx.sendResponseHeaders(200, responseArray.length);
					HTTPEx.getResponseBody().write(responseArray);
				}
				else{ //otherwise serve the 404 page
					byte fourOhFourArray[] = page404.getBytes();
					HTTPEx.sendResponseHeaders(404, fourOhFourArray.length);
					HTTPEx.getResponseBody().write(fourOhFourArray);
				}
			}//end not serving riveted
		}//end not a post
		}//end isAlive
		else{
			System.out.println(ZonedDateTime.now(ZoneId.of("America/Chicago")) + " INVALID RIVETED COPY GIVEN TO THREAD!");
			byte fourOhFourArray[] = page404.getBytes();
			HTTPEx.sendResponseHeaders(404, fourOhFourArray.length);
			HTTPEx.getResponseBody().write(fourOhFourArray);
		}
		HTTPEx.getResponseBody().flush();
		HTTPEx.close();
	}//end handle
	
	
	private Document getModifiedPage(String sitePath, String hostName) throws IOException{
			//Here's where the """"""""""magic"""""""""" happens
			Document page = Jsoup.connect("http://www.kansasCity.com/" + sitePath).get(); 
			if(!sitePath.contains(".js") && !sitePath.contains(".woff")){
				//wrap all images that aren't hosted from KC star's web site in a section with an id equal to the riveted id that will later track it.
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
				//make all links that link to kc star link back to our server
				for(Element link : links){
				
				Element newLink = new Element(Tag.valueOf("a"), "");
				
				newLink.attr("href", link.attr("href").replace("www.kansascity.com", domainName));
				newLink.text(link.text());
				link.replaceWith(newLink);
				
				
			}
				Elements scripts = page.select("script[src*=lightbox]"); //KC Star has a really annoying popup. I removed it.
				for(Element script:scripts){
					script.remove();
				}
				
				//add all the scripts to the page
			Element scriptLink = new Element(Tag.valueOf("script"), "");
			//add references to riveted dependencies
			scriptLink.attr("type", "text/javascript");
			scriptLink.attr("src", "https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js");
			page.select("head").append(scriptLink.toString());
			scriptLink = new Element(Tag.valueOf("script"), "");
			scriptLink.attr("type", "text/javascript");
			scriptLink.attr("src", "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js");
			page.select("head").append(scriptLink.toString());
			//add all riveted references and inline lines of code to start the riveted tracking.
			for(int i=0; i <imgId; i++){
				//script link
				scriptLink = new Element(Tag.valueOf("script"), "");
				scriptLink.attr("type", "text/javascript");
				scriptLink.attr("src", "http://" + domainName +"/riveted/riveted" + i +".js");
				page.select("head").append(scriptLink.toString());
				//script activation line of code
				scriptLink = new Element(Tag.valueOf("script"), "");
				scriptLink.text("riveted"+i+".init('" + i + "');");
				page.select("body").append(scriptLink.toString());
			}
			}
		return page;
	}
	
	private String readRequest(InputStream body){
		
		//get a string of what the user's browser sent to our server
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
