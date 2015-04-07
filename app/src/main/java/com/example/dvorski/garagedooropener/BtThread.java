package com.example.dvorski.garagedooropener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by dvorski on 24.2.2015..
 */
public class BtThread extends Thread {

    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private static Handler mHandler;
    private static Handler ctHandler;
    //private static ConnectedThread streamThread = null;
    private boolean sockConnected = false;

    public BtThread(BluetoothAdapter adapter, String addr, Handler handler, Handler cHandler) {
        mBluetoothAdapter = adapter;
        mHandler = handler;
        ctHandler = cHandler;
        //HC-05
        mDevice = mBluetoothAdapter.getRemoteDevice(addr);
        //connect to bDevice device
        BluetoothSocket tmp = null;
        Method m;
        try {
            m = mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(mDevice, 1);
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mSocket = tmp;
    }

    @Override
    public void run(){
        mBluetoothAdapter.cancelDiscovery();
        try {
            //send connecting message to the handler
            sendMessageToHandler("Spajanje na: " + mDevice.getName(),2);
            //connect to the bluetooth device
            mSocket.connect();
            sockConnected = true;
            //send connecting message to the handler
            sendMessageToHandler("Uspješno spojen na: " + mDevice.getName(),3);
        }catch(IOException ex){
            // Unable to connect; close the socket and get out
            //sendMessageToHandler("Spajanje na: " + mDevice.getName() + " nije moguće.",1);
            sendMessageToHandler(ex.getMessage(),3);
            //close the socket
            try {
                mSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        // Do work to manage the connection (in a separate thread)
        //streamThread = manageConnectedSocket(mSocket,ctHandler);
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) { }
    }

    private ConnectedThread manageConnectedSocket(BluetoothSocket mSocket,Handler handler){
        ConnectedThread streamThread = new ConnectedThread(mSocket,handler);
        streamThread.start();
        return streamThread;
    }

    public ConnectedThread getStreamThread(){
        //while (!sockConnected){}
        if(sockConnected){
            return manageConnectedSocket(mSocket,ctHandler);
        }else{
            return null;
        }
    }

    private void sendMessageToHandler(String message, int what){
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("message", message);
        msg.setData(b);
        msg.what = what;
        mHandler.sendMessage(msg);
    }

}
