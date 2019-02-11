/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proxify.server.bean;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * Bean represents a remote connection
 * @author sam
 */
public class BeanLink {

    public Socket client;
    public Socket assistent;
    public InputStream isClient;
    public OutputStream osClient;
    public InputStream isAssistent;
    public OutputStream osAssistent;
    public String id;
    public Date start;
    public boolean directConnection;
    public String clientIp;

}
