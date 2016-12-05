package edu.missouristate.csc450.group1.proxyServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.net.httpserver.HttpServer;

public class ProxyServer {
	//sql connection is a shared resource between all threads
	 public static MysqlConnection sqlConnection;
	 //the contents of the riveted.js file
	private static String rivetedJs;
	
	public static void main(String[] args) {
		rivetedJs = loadRiveted(); //get the rivited string
		//declare both threads
		HttpServer theProxyServer; //access to kc star website
		ARSCommunicator communicator; //access to DB from ARS
		try {			
			//start SQL connection
			sqlConnection = new MysqlConnection(5);
			//initialize Proxyserver
			theProxyServer = HttpServer.create(new InetSocketAddress(80), 2056);
			theProxyServer.createContext("/", new RequestHandler(rivetedJs));
			theProxyServer.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(64));
			theProxyServer.start();
			//initialize communicator
			communicator = new ARSCommunicator();
			communicator.start();
		} catch (IOException e) {
			//if something went wrong starting those processes, print out the error and quit
			System.out.println(ZonedDateTime.now(ZoneId.of("America/Chicago")) + " Could not Create Server!");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(ZonedDateTime.now(ZoneId.of("America/Chicago")) + " Proxy Server Started.");
	} 
	
	private static String loadRiveted(){
		//save the rivtedJs into memory
		//file IO inspired by http://stackoverflow.com/questions/4716503/reading-a-plain-text-file-in-java
		String rivetedJsString = "";
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader("riveted.js"));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while(line!= null){
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			rivetedJsString = sb.toString();
			br.close();
		} catch (IOException e) {
			System.out.println(ZonedDateTime.now(ZoneId.of("America/Chicago"))+ " FATAL ERROR: COULD NOT READ RIVITED FILE!");
			e.printStackTrace();
			System.exit(1);
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				System.out.println(ZonedDateTime.now(ZoneId.of("America/Chicago")) + " FATAL ERROR: COULD NOT CLOSE RIVITED FILE!");
				e.printStackTrace();
				System.exit(1);
			}
		}
		return rivetedJsString;
	}

}
