package edu.missouristate.csc450.group1.proxyServer;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection {
    java.sql.Connection connection;

    public MysqlConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/adData?","root","password");
        }
        catch (Exception ex){
            System.out.println("There was an error establishing the DB connection.");
            System.out.println("SQLException: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private java.sql.Connection conn(){
        return this.connection;
    }

    public java.sql.ResultSet getAllPages(String date){
        java.sql.Statement stmt = null;
        java.sql.ResultSet results = null;
        String date_clause = "";
        if(date != null)
            date_clause = " WHERE created_at > '"+date+"'";
        try {
                stmt = conn().createStatement();
                results = stmt.executeQuery("SELECT * FROM page"+date_clause);

            }
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }
        return results;
    }

    public java.sql.ResultSet getAllPages(){
        return getAllPages(null);
    }
    
    public void addPage(String url){
    	//add a page to the db
    	java.sql.Statement stmt = null;
    	int results;
    	try{
    		stmt = conn().createStatement();
    		results = stmt.executeUpdate("INSERT into adData.page (url) VALUES ('" + url + "')");
    	}
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }   	
    }
    
    public long getPageId(String url){
    	//returns an id if the url is in the database, else returns -1
    	java.sql.Statement stmt = null;
    	java.sql.ResultSet results = null;
    	try{
    		stmt = conn().createStatement();
    		results = stmt.executeQuery("SELECT id FROM page WHERE url='" + url +  "'");
    		if(results.first()){
    			return results.getLong("id");
    		}
    		else{
    			return -1;
    		}
    	}
    	catch (SQLException ex){
    		System.out.println("SQL Exception: " + ex.getMessage());
    		return -1;
    	}	
    }
    
    public boolean doesPageExistInDB(String url){
    	return getPageId(url) != -1;
    }

    public java.sql.ResultSet getAllKeywords(String date){
        java.sql.Statement stmt = null;
        java.sql.ResultSet results = null;
        String date_clause = "";
        if(date != null)
            date_clause = " WHERE created_at > '"+date+"'";
        try {
                stmt = conn().createStatement();
                results = stmt.executeQuery("SELECT * FROM keyword"+date_clause);

            }
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }
        return results;
    }

    public java.sql.ResultSet getAllKeywords(){
        return getAllKeywords(null);
    }
    
    public long getKeywordId(String keyword){
    	//returns an id if the keyword is in the database, else returns -1
    	java.sql.Statement stmt = null;
    	java.sql.ResultSet results = null;
    	try{
    		stmt = conn().createStatement();
    		results = stmt.executeQuery("SELECT id FROM keyword WHERE keyword_name='" + keyword +  "'");
    		if(results.first()){
    			return results.getLong("id");
    		}
    		else{
    			return -1;
    		}
    	}
    	catch (SQLException ex){
    		System.out.println("SQL Exception: " + ex.getMessage());
    		return -1;
    	}	
    }
    
    public boolean doesKeywordExistInDB(String keyword){
    	return getKeywordId(keyword) != -1;
    }
    
    public void addKeywordToDB(String keyword){
    	//add a keyword to the db
    	java.sql.Statement stmt = null;
    	int results;
    	try{
    		stmt = conn().createStatement();
    		results = stmt.executeUpdate("INSERT into adData.keyword (keyword_name) VALUES ('" + keyword + "')");
    	}
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }   
    }

    public boolean doesKeywordPageExistInDB(String url, String keyword){
    	long urlId = getPageId(url);
    	long keyId = getKeywordId(keyword);
    	java.sql.Statement stmt = null;
    	java.sql.ResultSet results = null;
    	try{
    		stmt = conn().createStatement();
    		results = stmt.executeQuery("SELECT * FROM page_keywords WHERE keywords=" + keyId +  " AND page=" + urlId);
    		return results.first() ; //if there is a keyword-page relationship return true
    	}
    	catch (SQLException ex){
    		System.out.println("SQL Exception: " + ex.getMessage());
    		return false;
    	}	
    	
    }
    
    public void addKeywordPage(String url, String keyword){
    	//add a keyword page to the db
    	long urlId = getPageId(url);
    	long keyId = getKeywordId(keyword);
    	java.sql.Statement stmt = null;
    	int results;
    	try{
    		stmt = conn().createStatement();
    		results = stmt.executeUpdate("INSERT into adData.page_keywords (keywords, page) VALUES (" + keyId + "," + urlId +")");
    	}
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }   
    }
    
    public java.sql.ResultSet getAllAdLocationVisits(String date){
        java.sql.Statement stmt = null;
        java.sql.ResultSet results = null;
        String date_clause = "";
        if(date != null)
            date_clause = " WHERE created_at > '"+date+"'";
        try {
                stmt = conn().createStatement();
                results = stmt.executeQuery("SELECT * FROM ad_location_visit"+date_clause);

            }
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }
        return results;
    }

    public java.sql.ResultSet getAllAdLocationVisits(){
        return getAllAdLocationVisits(null);
    }

    public void addAdLocationVisit(String url, String pageLocation, double focusRatio, double activeRatio, double totalSpent){
    	long urlId = getPageId(url);
    	java.sql.Statement stmt = null;
    	int results;
    	try{
    		stmt = conn().createStatement();
    		results = stmt.executeUpdate("INSERT into adData.ad_location_visit (page_location, focus_ratio, active_ratio, total_spent, page_id) VALUES (" 
    				+ pageLocation.substring(0, 1) + "," 
    				+ focusRatio + "," 
    				+ activeRatio + "," 
    				+ totalSpent +","
    				+ urlId +")");
    	}
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }  
    }
    public java.sql.ResultSet getAllPageKeywordRelationships(String date){
        java.sql.Statement stmt = null;
        java.sql.ResultSet results = null;
        String date_clause = "";
        if(date != null)
            date_clause = " WHERE created_at > '"+date+"'";
        try {
                stmt = conn().createStatement();
                results = stmt.executeQuery("SELECT * FROM page_keywords"+date_clause);

            }
        catch (SQLException ex){
            System.out.println("SQLException: " + ex.getMessage());
        }
        return results;
    }

    public java.sql.ResultSet getAllPageKeywordRelationships(){
        return getAllPageKeywordRelationships(null);
    }
}
