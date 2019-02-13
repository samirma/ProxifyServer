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
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.proxify.server.bean.BeanLink;

/**
 *
 * @author sam
 */
public class AssistanceConnector implements Runnable {

//    public static HashMap<String, Socket> sockets = new HashMap<String, Socket>();

    public static String md5(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(source.getBytes());
            return String.valueOf(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    class PoolCliente implements Runnable {

        Socket assistanceSocket;

        public PoolCliente(Socket _inputSocket) {
            assistanceSocket = _inputSocket;

        }

        public void run() {
            String uuidString = null;
            try {

                InputStream is = assistanceSocket.getInputStream();
                OutputStream os = assistanceSocket.getOutputStream();

//                is = new ZipInputStream(is);
//                os = new ZipOutputStream(os);

                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                boolean clienteEncontrado = true;

                String line = in.readLine();
                BeanLink beanLink = ClientConnector.sockets.get(line);

                if (beanLink == null && line.startsWith("id:")) {
                    
                    String nomeMaquina = line.replaceAll("id:", "");

                    line = ClientConnector.ids.get(nomeMaquina);

                    beanLink = ClientConnector.sockets.get(line);

                }

                if (beanLink == null || beanLink.client == null) {
                    clienteEncontrado = false;
                } else if (beanLink.client.isClosed() || !beanLink.client.isConnected()) {
                    ClientConnector.sockets.remove(line);
                    clienteEncontrado = false;
                }

                PrintWriter out = new PrintWriter(os);
                if (!clienteEncontrado) {
                    out.println("erro");
                    out.flush();
                    assistanceSocket.close();
                } else {

                    ClientConnector.sockets.remove(line);

                    Server.conexao++;

                    if (beanLink.directConnection) { //Support direct connection

                        String ipString = assistanceSocket.getRemoteSocketAddress().toString();
                        ipString = Server.formatIP(ipString);

                        out.println("ok:" + beanLink.directConnection + ":" + beanLink.clientIp);
                        out.flush();

                        assistanceSocket.close();
                        beanLink.client.close();

                    } else {
                        out.println("ok:false");
                        out.flush();

                        beanLink.assistent = assistanceSocket;
                        beanLink.osAssistent = os;
                        beanLink.isAssistent = is;

                        beanLink.osClient.write("gogogo\n".getBytes());

                        Server.connect(beanLink);
                    }

                }

            } catch (IOException ex) {
                ex.printStackTrace();
                try {
                    ClientConnector.sockets.remove(uuidString);
                    assistanceSocket.close();
                } catch (Exception exSocket) {
                }
            }
        }
    }

    @Override
    public void run() {
        try {

            ServerSocket socketServerAssist = new ServerSocket(Server.assistantPort, 100, InetAddress.getByName(Server.assistanceServerIp));

            System.out.println("Assistance Module actived: " + Server.assistantPort + " - " + 100 + " - " + Server.assistanceServerIp);

            while (true) {

                Socket socketClient = socketServerAssist.accept();

                socketClient.setReceiveBufferSize(Server.buffer.length);
                socketClient.setSendBufferSize(Server.buffer.length);

                new Thread(new PoolCliente(socketClient)).start();

            }

        } catch (IOException ex) {
            Logger.getLogger(AssistanceConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
