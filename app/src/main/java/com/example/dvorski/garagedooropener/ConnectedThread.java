package com.example.dvorski.garagedooropener;


import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.String;

/**
 * Created by dvorski on 23.2.2015..
 */
public class ConnectedThread extends Thread {
    private static BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private static Handler mmHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        mmHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run(){
        byte delimiter = 10;
        byte[] readBuffer;
        int readBufferPosition = 0;
        readBuffer = new byte[1024];
        int bytesAvailable;
        while (true) {
            try {
                bytesAvailable = mmInStream.available();
                if(bytesAvailable > 0)
                {
                    byte[] buffer = new byte[bytesAvailable];
                    mmInStream.read(buffer);
                    for(int i=0;i<bytesAvailable;i++)
                    {
                        byte b = buffer[i];
                        if(b == delimiter)
                        {
                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                            // initialize an InputStream
                            //InputStream is = new ByteArrayInputStream(encodedBytes);
                            //String result = getStringFromInputStream(is);
                            final String result = new String(encodedBytes, "UTF-8");
                            readBufferPosition = 0;

                            //send message to handler
                            Message msg = new Message();
                            //msg.what = 1;
                            Bundle bun = new Bundle();
                            String jsonString = result;
                            bun.putString("message", jsonString);
                            msg.setData(bun);
                            mmHandler.sendMessage(msg);
                        }
                        else
                        {
                            readBuffer[readBufferPosition++] = b;
                        }
                    }
                }
            }catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            sendMessageToHandler(e.getMessage());
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            sendMessageToHandler(e.getMessage());
        }
    }

    private void sendMessageToHandler(String message){
        Message msg = new Message();
        msg.what = 1;
        Bundle b = new Bundle();
        b.putString("message", message);
        msg.setData(b);
        mmHandler.sendMessage(msg);
    }

    public void setMmHandler(Handler handler){
        mmHandler = handler;
    }

}
