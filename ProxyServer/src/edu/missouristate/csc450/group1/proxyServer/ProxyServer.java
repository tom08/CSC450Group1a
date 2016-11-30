package edu.missouristate.csc450.group1.proxyServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.net.httpserver.HttpServer;

public class ProxyServer {
	 public static MysqlConnection sqlConnection;
	private static String rivetedJs;
	public static void main(String[] args) {
		rivetedJs = loadRiveted();
		HttpServer theProxyServer;
		ARSCommunicator communicator;
		try {
			
			theProxyServer = HttpServer.create(new InetSocketAddress(80), 0);
			theProxyServer.createContext("/", new RequestHandler(rivetedJs));
			theProxyServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
			theProxyServer.start();
			sqlConnection = new MysqlConnection();
			communicator = new ARSCommunicator();
			communicator.start();
		} catch (IOException e) {
			System.out.println("Could not Create Server!");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Proxy Server Started.");
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
			System.out.println("FATAL ERROR: COULD NOT READ RIVITED FILE!");
			e.printStackTrace();
			System.exit(1);
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				System.out.println("FATAL ERROR: COULD NOT CLOSE RIVITED FILE!");
				e.printStackTrace();
				System.exit(1);
			}
		}
		return rivetedJsString;
	}

}
