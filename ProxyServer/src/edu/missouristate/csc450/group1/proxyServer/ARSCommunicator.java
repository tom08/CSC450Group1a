package edu.missouristate.csc450.group1.proxyServer;

import java.io.IOException;
import java.net.ServerSocket;


public class ARSCommunicator extends Thread{
    @Override
	public void run() {
        System.out.println("The ProxyCommunicator server is running.");
        int clientNumber = 0;
        ServerSocket listener;
        listener = null;
		try {
			listener = new ServerSocket(12000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
            while (true) {
                try {
					new Communicator(listener.accept(), clientNumber++).run();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        } finally {
            try {
				listener.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }


}
