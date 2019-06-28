/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Серверный поток
 * @author psy888
 */
public class ServerThread extends Thread{
    public static final String SERVER_ADDRESS = "192.168.13.129"; 
    @Override
    public void run(){
        try{
            ServerSocket server = new ServerSocket(4000, 5, (InetAddress.getByName(SERVER_ADDRESS)));
            while(true){
                Socket client = server.accept();
                ReadWriteThread RWT = new ReadWriteThread(client);
                RWT.setDaemon(true);
                RWT.start();
            }
        }catch(Exception e){
            System.out.println("Ошибка подключения" + e.getMessage());
        }
    }
    
}
