import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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

        public void run(){
            try{
                BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                String cmd;
                String[] pages = {
                    "TYPE,,PAGE,,2",
                    "1,,http://missouristate.edu",
                    "2,,http://google.com",
                };
                String[] ad_locations = {
                    "TYPE,,AD,,4",
                    "1,,A,,0.334,,0.45,,15,,1",
                    "2,,B,,0.223,,0.55,,12,,1",
                    "3,,C,,0.221,,0.11,,11,,1",
                    "4,,D,,0.123,,0.8888,,32,,1",
                    }; 
                String[] keywords = {
                    "TYPE,,KEY,,2",
                    "1,,test keyword 1",
                    "2,,test kwd 2",
                };
                String[][] to_send = {
                    pages,
                    keywords,
                    ad_locations,
                };

                out.println("Please enter your command.\n");
                cmd = in.readLine();
                for(int i = 0; i < to_send.length; i++){
                    log(cmd);
                    for(int j = 0; j < to_send[i].length; j++){
                        out.println(to_send[i][j]);
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
}
