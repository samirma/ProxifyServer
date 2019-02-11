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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.proxify.server.bean.BeanLink;

/**
 *
 * @author sam
 */
public class RemoteAccessConnec implements Runnable {

    public static HashMap<String, BeanLink> sockets = new HashMap<String, BeanLink>();

    class PoolCliente implements Runnable {

        Socket assitenteSocket;

        public PoolCliente(Socket _inputSocket) {
            assitenteSocket = _inputSocket;

        }

        public void run() {
            try {

                InputStream is = assitenteSocket.getInputStream();
                OutputStream os = assitenteSocket.getOutputStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                String line = in.readLine();

                String codigo = ClientConnector.ids.get(line);
                BeanLink beanLink = ClientConnector.sockets.get(codigo);

                PrintWriter out = new PrintWriter(os, true);

                if (beanLink == null || beanLink.client == null) {
                    out.println("erro:notfound");
                } else {
                    if (beanLink.client.isConnected()) {
                        out.println("ok:" + codigo);
                    } else {
                        out.println("erro:offline");
                    }

                }

                out.flush();

            } catch (Exception e) {
                Logger.getLogger(RemoteAccessConnec.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                try {
                    assitenteSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(RemoteAccessConnec.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    @Override
    public void run() {
        try {

            ServerSocket socketServerAssist = new ServerSocket(Server.portaAccessRemote, 100, InetAddress.getByName("127.0.0.1"));

            System.out.println("Remote module on: " + Server.portaAccessRemote);

            while (true) {

                Socket socketClient = socketServerAssist.accept();

//                socketClient.setReceiveBufferSize(ServidorJam.buffer.length);
//                socketClient.setSendBufferSize(ServidorJam.buffer.length);

                new Thread(new PoolCliente(socketClient)).start();

            }

        } catch (IOException ex) {
            Logger.getLogger(AssistanceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
