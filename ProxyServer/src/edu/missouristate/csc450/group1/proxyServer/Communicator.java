package edu.missouristate.csc450.group1.proxyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Communicator {
    Socket socket;
    private int clientNumber;

    public Communicator(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        log("New connection at " + socket);
    }

    private List<String> add_pages(List<String> pages, java.sql.ResultSet results){
        String entry = "";
        try{
        while(results.next()){
            entry += results.getLong("id");
            entry += ",," + results.getString("url");
            pages.add(entry);
            entry = "";
        }
        } catch (SQLException ex){
            log(ex.getMessage());
        }
        return pages;
    }

    private List<String> add_ads(List<String> ads, java.sql.ResultSet results){
        String entry = "";
        try{
        while(results.next()){
            entry += results.getLong("id");
            entry += ",," + results.getString("page_location");
            entry += ",," + results.getDouble("focus_ratio");
            entry += ",," + results.getDouble("active_ratio");
            entry += ",," + results.getDouble("total_spent");
            entry += ",," + results.getLong("page_id");
            ads.add(entry);
            entry = "";
        }
        } catch (SQLException ex){
            log(ex.getMessage());
        }
        return ads;
    }

    private List<String> add_keywords(List<String> keywords, java.sql.ResultSet results){
        String entry = "";
        try{
        while(results.next()){
            entry += results.getLong("id");
            entry += ",," + results.getString("keyword_name");
            keywords.add(entry);
            entry = "";
        }
        } catch (SQLException ex){
            log(ex.getMessage());
        }
        return keywords;
    }

    private List<String> add_kp_relations(List<String> KP_relations, java.sql.ResultSet results){
        String entry = "";
        try{
        while(results.next()){
            entry += results.getLong("keywords");
            entry += ",," + results.getLong("page");
            KP_relations.add(entry);
            entry = "";
        }
        } catch (SQLException ex){
            log(ex.getMessage());
        }
        return KP_relations;
    }

    public void run(){
        try{
            BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String cmd;
            cmd = in.readLine();
            String[] data = cmd.split(",,");
            String date = null;
            if(data.length > 1)
                date = data[1];

            java.sql.ResultSet page_results = null;
            java.sql.ResultSet ad_results = null;
            java.sql.ResultSet key_results = null;
            java.sql.ResultSet KP_relations = null;
            try{
            	boolean gotEverything = false;
            	while(!gotEverything){
            		synchronized(ProxyServer.sqlConnection){
                        page_results = ProxyServer.sqlConnection.getAllPages(date);
                        ad_results = ProxyServer.sqlConnection.getAllAdLocationVisits(date);
                        key_results =ProxyServer.sqlConnection.getAllKeywords(date);
                        KP_relations = ProxyServer.sqlConnection.getAllPageKeywordRelationships(date);
                        gotEverything = true;
            		}
            	}

            } catch(NullPointerException ex){
                log(ex.getMessage());
            }
            List<String> pages = new ArrayList<String>();
            List<String> ad_locations = new ArrayList<String>();
            List<String> keywords = new ArrayList<String>();
            List<String> relations = new ArrayList<String>();
            pages.add("TYPE,,PAGE");
            ad_locations.add("TYPE,,AD");
            keywords.add("TYPE,,KEY");
            relations.add("TYPE,,KPR");
            if(page_results != null){
                pages = add_pages(pages, page_results);
            }
            if(ad_results != null){
                ad_locations = add_ads(ad_locations, ad_results);
            }
            if(key_results != null){
                keywords = add_keywords(keywords, key_results);
            }
            if(KP_relations != null){
                relations = add_kp_relations(relations, KP_relations);
            }


            List<List<String>> to_send = new ArrayList<List<String>>();
            to_send.add(pages);
            to_send.add(ad_locations);
            to_send.add(keywords);
            to_send.add(relations);

            out.println("Please enter your command.\n");
            cmd = in.readLine();
            for(int i = 0; i < to_send.size(); i++){
                for(int j = 0; j < to_send.get(i).size(); j++){
                    out.println(to_send.get(i).get(j));
                }
            }
        } catch (IOException e) {
            log("Error: " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log("Couldn't close the socket: " + socket);
            }
            log("Connection closed");
        }
    }

    private void log(String message) {
        System.out.println(message);
    }
}
