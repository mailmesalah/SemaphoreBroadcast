package test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import broadcastsemaphore.Connection;
import broadcastsemaphore.DataIO;

public class Test {
	Connection mConnection;

	public Test() {
		mConnection = new Connection(5555);
	}

	public void sendClientData() throws IOException {
		DataIO dio = mConnection.connectIO("127.0.0.1", 5555);
		DataInputStream dis = dio.getDis();
		DataOutputStream dos = dio.getDos();
		dos.writeInt(1234);
		dos.writeInt(4221);
		System.out.println(dis.readInt());
	}

	public void waitForIncomingData() {
		class RunBack extends Thread {

			RunBack() {
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				while (true) {
					try {
						DataIO dio = mConnection.acceptConnect();
						DataInputStream dis = dio.getDis();
						DataOutputStream dos = dio.getDos();
						System.out.println(dis.readInt());
						System.out.println(dis.readInt());
						dos.writeInt(65756);
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}

		new RunBack();

	}

	public static void main(String arg[]) {
		try {
			Test t = new Test();
			t.waitForIncomingData();
			System.out.println("Enter 1 to Test from this Helper");
			if (Integer.parseInt(new BufferedReader(new InputStreamReader(
					System.in)).readLine()) == 1) {
			}
			t.sendClientData();
			t.sendClientData();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

}
