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
                String[] to_send = {
                    "1,0.334,0.45,15,1",
                    "2,0.223,0.55,12,1",
                    "3,0.221,0.11,11,1",
                    "4,0.123,0.8888,32,1",
                    }; 

                out.println("Please enter your command.\n");
                cmd = in.readLine();
                log(cmd);
                for(int i = 0; i < to_send.length; i++){
                    out.println(to_send[i]);
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
