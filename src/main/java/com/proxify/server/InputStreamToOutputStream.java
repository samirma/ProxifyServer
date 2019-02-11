package com.proxify.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class InputStreamToOutputStream
        implements Runnable {

    Socket inputSocket;
    InputStream is;
    OutputStream os;
    String id = null;

    public InputStreamToOutputStream(String _id, Socket _inputSocket, InputStream _inputStream, OutputStream _outputStream) {
        this.is = _inputStream;
        this.os = _outputStream;
        this.inputSocket = _inputSocket;
        this.id = _id;
    }

    public void run() {
        try {
            byte[] buffer = new byte[10240];
            int lenRead = 0;
            while ((lenRead = this.is.read(buffer, 0, buffer.length)) > 0) {
                this.os.write(buffer, 0, lenRead);
                this.os.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                this.inputSocket.close();
            } catch (Exception exSocket) {
            }
        }
    }
}
