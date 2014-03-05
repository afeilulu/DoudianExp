package cn.com.xinli.android.doudian.service;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import cn.com.xinli.android.doudian.model.Method;

public class UdpClient {
	private InetAddress IPAddress;
	private Method mMethod;
	int packageLength = 1450;

	public UdpClient(Method method) {
		this.mMethod = method;
	}
	
	public String run(){
		String s1;
		String result;

		try {
			IPAddress = InetAddress.getByName(SyncService.SERVER_IP_ADDRESS);
			System.out.println("Attemping to connect to " + IPAddress
					+ " via UDP port 9876");
		} catch (UnknownHostException ex) {
			System.err.println(ex);
			return "error:port used";
		}

		try {			
			s1 = new Gson().toJson(mMethod);
			System.out.println("Sending message : " + s1);

			byte[] sendData = new byte[packageLength];
			sendData = s1.getBytes();

			DatagramSocket clientSocket = new DatagramSocket();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, 9876);

			clientSocket.send(sendPacket);

			byte[] receiveData = new byte[packageLength];
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

//				System.out.println("Waiting for return packet");
			clientSocket.setSoTimeout(10000);

			try {
				clientSocket.receive(receivePacket);
				result = new String(receivePacket.getData());
				
//				System.out.println("Response in " + (System.currentTimeMillis() - start) + "ms");
//				System.out.println("Message: " + modifiedSentence);
				
			} catch (SocketTimeoutException ste) {
				System.out.println("Timeout Occurred: Packet assumed lost");
				return "error:Timeout Occurred";
			}

			clientSocket.close();
			return result;
			
		} catch (IOException ex) {
			System.err.println(ex);
			return "error:unknown";
		}
	}

}
