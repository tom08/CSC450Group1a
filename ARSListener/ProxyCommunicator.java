import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.lang.NullPointerException;

public class ProxyCommunicator{
    public static void main(String[] args) throws Exception {
        System.out.println("The ProxyCommunicator server is running.");
        int clientNumber = 0;
        ServerSocket listener = new ServerSocket(12000);
        try {
            while (true) {
                new Communicator(listener.accept(), clientNumber++).run();
            }
        } finally {
            listener.close();
        }
    }

    private static class Communicator {
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

        public void run(){
            try{
                BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String cmd;
                MysqlConnection conn = new MysqlConnection();
                java.sql.ResultSet page_results = null;
                java.sql.ResultSet ad_results = null;
                java.sql.ResultSet key_results = null;
                try{
                    page_results = conn.getAllPages();
                    ad_results = conn.getAllAdLocationVisits();
                    key_results = conn.getAllKeywords();
                } catch(NullPointerException ex){
                    log(ex.getMessage());
                }
                List<String> pages = new ArrayList<String>();
                List<String> ad_locations = new ArrayList<String>();
                List<String> keywords = new ArrayList<String>();
                pages.add("TYPE,,PAGE");
                ad_locations.add("TYPE,,AD");
                keywords.add("TYPE,,KEY");
                if(page_results != null){
                    pages = add_pages(pages, page_results);
                }
                if(ad_results != null){
                    ad_locations = add_ads(ad_locations, ad_results);
                }
                if(key_results != null){
                    keywords = add_keywords(keywords, key_results);
                }


                List<List<String>> to_send = new ArrayList<List<String>>();
                to_send.add(pages);
                to_send.add(ad_locations);
                to_send.add(keywords);

                out.println("Please enter your command.\n");
                cmd = in.readLine();
                for(int i = 0; i < to_send.size(); i++){
                    for(int j = 0; j < to_send.get(i).size(); j++){
                        log(to_send.get(i).get(j));
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

    public static class MysqlConnection {
        java.sql.Connection connection;

        public MysqlConnection(){
            try{
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                this.connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/ProxyAdData?","root","password");
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

        public java.sql.ResultSet getAllPages(){
            java.sql.Statement stmt = null;
            java.sql.ResultSet results = null;
            try {
                    stmt = conn().createStatement();
                    results = stmt.executeQuery("SELECT * FROM page");

                }
            catch (SQLException ex){
                System.out.println("SQLException: " + ex.getMessage());
            }
            return results;
        }

        public java.sql.ResultSet getAllKeywords(){
            java.sql.Statement stmt = null;
            java.sql.ResultSet results = null;
            try {
                    stmt = conn().createStatement();
                    results = stmt.executeQuery("SELECT * FROM keyword");

                }
            catch (SQLException ex){
                System.out.println("SQLException: " + ex.getMessage());
            }
            return results;
        }

        public java.sql.ResultSet getAllAdLocationVisits(){
            java.sql.Statement stmt = null;
            java.sql.ResultSet results = null;
            try {
                    stmt = conn().createStatement();
                    results = stmt.executeQuery("SELECT * FROM ad_location_visit");

                }
            catch (SQLException ex){
                System.out.println("SQLException: " + ex.getMessage());
            }
            return results;
        }
    }
}
