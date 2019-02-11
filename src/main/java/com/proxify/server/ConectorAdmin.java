/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proxify.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.proxify.server.bean.BeanLink;

/**
 * This class is a interface module for view the status of proxy server
 * @author sam
 */
public class ConectorAdmin implements Runnable {

    public static HashMap<String, BeanLink> sockets = new HashMap<String, BeanLink>();

    class PoolCliente implements Runnable {

        Socket inputSocket;

        public PoolCliente(Socket _inputSocket) {
            inputSocket = _inputSocket;

        }

        public void run() {
            try {

                String ipString = inputSocket.getRemoteSocketAddress().toString();
                ipString = Server.formatIP(ipString);

                InputStream is = inputSocket.getInputStream();
                OutputStream os = inputSocket.getOutputStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                PrintWriter out = new PrintWriter(os, true);

                String line = in.readLine();

                if (line.startsWith("lista")) {

                    HashMap<String, BeanLink> sockets = ClientConnector.sockets;

                    Set<String> codigos = sockets.keySet();

                    for (String cod : codigos) {
                        BeanLink beanLink = sockets.get(cod);

                        out.println("id: " + beanLink.id + " Client address: " + beanLink.clientIp + " code: " + cod + " connection date: " + beanLink.start);
                        out.flush();

                    }

                } else if (line.startsWith("total")) {

                    HashMap<String, BeanLink> sockets = ClientConnector.sockets;

                    out.println("Current conenctions: " + sockets.size());
                    out.flush();

                } else if (line.startsWith("resetar")) {

                    HashMap<String, BeanLink> sockets = ClientConnector.sockets;

                    Set<String> codigos = sockets.keySet();

                    for (String cod : codigos) {
                        BeanLink beanLink = sockets.get(cod);

                        Socket socket = beanLink.client;
                        try {
                            if (socket.isConnected()) {
                                socket.close();
                            }
                        } finally {
                            ClientConnector.sockets.remove(cod);
                            Server.conexao = 0;
                        }

                        System.gc();

                        out.println("Current connections: " + sockets.size());
                        out.println("Connections already done: " + Server.conexao);
                        out.flush();

                    }

                } else if (line.startsWith("conexoes")) {

                    out.println("Connections already done: " + Server.conexao);
                    out.flush();

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    inputSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        try {

            ServerSocket socketServerClient = new ServerSocket(Server.adminPort, 100, InetAddress.getByName("127.0.0.1"));

            System.out.println("Admin module on: " + Server.adminPort);


            while (true) {

                Socket socketClient = socketServerClient.accept();

                socketClient.setReceiveBufferSize(Server.buffer.length);
                socketClient.setSendBufferSize(Server.buffer.length);

                new Thread(new PoolCliente(socketClient)).start();

            }

        } catch (IOException ex) {
            Logger.getLogger(ConectorAdmin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
