package com.itrustmynetwork.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.itrustmynetwork.server.bean.BeanLink;

public class Server {

    public static byte[] buffer = new byte[1];
    public static MessageDigest md = null;
    public static final String CONFIG_FILE = "c:\\demo.properties";
    public static int clientPort = 1123;
    public static int assistantPort = 1122;
    public static int waitPort = 5555;
    public static int portaAccessRemote = 1123;
    static int conexao = 0;
    static int adminPort = 2211;
    public static String assistanceServerIp = "127.0.0.1";
    public static String ipServidorCliente = "127.0.0.1";
    public static String servidorPath = "";
    public static String clientePath = "";
    public static String md5Servidor = "";
    public static String md5Cliente = "";

    public static void main(String[] args)
            throws Exception {
        loadConfigServer(args);

        new Thread(new ClientConnector()).start();

        new Thread(new AssistanceConnector()).start();

        new Thread(new ConectorAdmin()).start();

        new Thread(new RemoteAccessConnec()).start();


    }

    /**
     * Create a bridge between the client and assistance
     * @param beanLink 
     */
    static void connect(BeanLink beanLink) {
        try {
            new Thread(new InputStreamToOutputStream("assistent-cliente", beanLink.assistent, beanLink.isAssistent, beanLink.osClient)).start();
            new Thread(new InputStreamToOutputStream("cliente-assistent", beanLink.client, beanLink.isClient, beanLink.osAssistent)).start();
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String md5(String source) {
        try {
            byte[] bytes = md.digest(source.getBytes());
            BigInteger hashBig = new BigInteger(1, bytes);

            return hashBig.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load config properties to use in the proxy server
     * @param args 
     */
    private static void loadConfigServer(String[] args) {
        Properties prop = new Properties();
        try {
            String arq;
            if (args.length == 1) {
                arq = args[0];
            } else {
                arq = CONFIG_FILE;
            }
            prop.load(new FileInputStream(arq));
        } catch (IOException e) {
        }
        String assistantPortCfg = prop.getProperty("assistantPort");

        String portaClienteCfg = prop.getProperty("clientPort");

        String portaEsperaCfg = prop.getProperty("waitPort");

        String ipAssistenteCfg = prop.getProperty("assistantServerIp");

        String ipClienteCfg = prop.getProperty("clientServerIp");

        String adminPortCfg = prop.getProperty("adminPort");
        if (adminPortCfg != null) {
            adminPort = new Integer(adminPortCfg);
        }

        servidorPath = prop.getProperty("servidorPath");

        clientePath = prop.getProperty("clientePath");

        assistanceServerIp = ipAssistenteCfg;
        ipServidorCliente = ipClienteCfg;

        clientPort = new Integer(portaClienteCfg).intValue();
        assistantPort = new Integer(assistantPortCfg).intValue();
        waitPort = new Integer(portaEsperaCfg).intValue();
    }

    public static String formatIP(String aIP) {
        if (aIP.contains("/")) {
            aIP = aIP.replaceAll("/", "");
            aIP = aIP.split(":")[0];
        }
        return aIP.trim();
    }

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            md = null;
        }
    }
}
