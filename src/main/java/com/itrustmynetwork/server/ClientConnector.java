/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itrustmynetwork.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.itrustmynetwork.server.bean.BeanLink;

/**
 *
 * @author sam
 */
public class ClientConnector implements Runnable {

    public static HashMap<String, BeanLink> sockets = new HashMap<String, BeanLink>();

    public static HashMap<String, String> ids = new HashMap<String, String>();

    class PoolCliente implements Runnable {

        Socket inputSocket;

        public PoolCliente(Socket _inputSocket) {
            inputSocket = _inputSocket;

        }

        public void run() {
            String uuidString = null;
            String code = "";
            try {

                String ipString = inputSocket.getRemoteSocketAddress().toString();
                ipString = Server.formatIP(ipString);

                InputStream is = inputSocket.getInputStream();
                OutputStream os = inputSocket.getOutputStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String line = in.readLine();

                if (!line.startsWith("id:")) {
                    
                    if (line.startsWith("remove:")) {
                        sockets.remove(line.replace("remove:", ""));
                    }

                    inputSocket.close();
                    return;
                } else {

                uuidString = inputSocket.getRemoteSocketAddress().toString();

                code = Server.md5(uuidString+line).substring(0, 5);

                PrintWriter out = new PrintWriter(os, true);

                out.println(code);
                out.flush();

                BeanLink beanLink = new BeanLink();

                String ident = line.replaceFirst("id:", "");

                if (ids.get(ident)!=null) {
                    
                    sockets.remove(ids.get(ident));
                }

                ids.put(ident, code);

                sockets.put(code, beanLink);
                
                boolean allowDirectConnect = false;

                beanLink.client = inputSocket;
                beanLink.clientIp = ipString;
                beanLink.isClient = is;
                beanLink.osClient = os;
                beanLink.id = ident;
                beanLink.start = new Date();
                beanLink.directConnection = allowDirectConnect;

                beanLink.directConnection = checkDirectConnection(ipString, ident);

                }

            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    sockets.remove(code);
                    inputSocket.close();
                } catch (Exception exSocket) {
                }
            }
        }

        /**
         * Check if is possible connect directly to client without to use this proxy server
         * @param clientIp
         * @param id
         * @return 
         */
        private boolean checkDirectConnection(String clientIp, String id) {
            try {
                Socket socket = new Socket(clientIp, Server.waitPort);

                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                PrintWriter out = new PrintWriter(os, true);

                BufferedReader in = new BufferedReader(new InputStreamReader(is));


                out.println("check:"+id);
                out.flush();


                String line = in.readLine();

                socket.close();

                boolean ok = "ok".equals(line);

                return ok;

            } catch (UnknownHostException ex) {
            } catch (IOException ex) {
            }


            return false;
        }
    }

    @Override
    public void run() {
        try {

            ServerSocket socketServerClient = new ServerSocket(Server.clientPort, 100, InetAddress.getByName(Server.ipServidorCliente));

            System.out.println("Client Module actived: " + Server.clientPort + " - " + 100 + " - " + Server.ipServidorCliente);

            while (true) {

                Socket socketClient = socketServerClient.accept();

//                socketClient.setReceiveBufferSize(ServidorJam.buffer.length);
//                socketClient.setSendBufferSize(ServidorJam.buffer.length);

                new Thread(new PoolCliente(socketClient)).start();

            }

        } catch (IOException ex) {
            Logger.getLogger(ClientConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}