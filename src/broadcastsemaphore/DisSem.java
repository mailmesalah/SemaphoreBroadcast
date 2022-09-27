package broadcastsemaphore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import broadcastsemaphore.Message.MessageType;

public class DisSem {

	
	private Connection mConnection;	
	private String mIpAddress;
	private int mHelperID;
	private int returnPort;
	private int helperPNO;
	private Connection returnC;
	
	
	
	public DisSem(int helperPNO, String mIpAddress,int mHelperID) {
		super();				
		this.helperPNO=helperPNO;
		this.mIpAddress = mIpAddress;
		this.mHelperID = mHelperID;
		mConnection= new Connection(helperPNO+100+mHelperID);		
		returnPort = 5000 + mHelperID;
		if (helperPNO == 5000 + mHelperID) {
			System.out
					.println("The port number is reserved, use another port number");
			System.exit(0);
		}
		returnC = new Connection(returnPort);
	}

	public void requestPOP(int clockH) throws IOException {
		
		class RunBack extends Thread {
			int clockH;
			RunBack(int clock) {
				setDaemon(true);
				clockH=clock;
				start();				
			}

			public void run() {				
				DataIO dio = mConnection.connectIO(mIpAddress, helperPNO);
				DataInputStream dis = dio.getDis();
				DataOutputStream dos = dio.getDos();

				// Request for pop broadcast by this helper
				System.out.println("Send POP");
				try {
					dos.writeInt(mHelperID);// 1)helper ID
					dos.writeInt(clockH++);// 2)TimeStamp
					dos.writeInt(MessageType.reqP.ordinal());// 3)Message Type

					// Wait for completing POP		
					DataIO dioReturn = returnC.acceptConnect();
					int ts = dioReturn.getDis().readInt();// 1)TimeStamp
					// Update clock
					clockH = (clockH < ts + 1 ? ts + 2 : ++clockH);
					System.out.println("Last pop Clock " + clockH);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		new RunBack(clockH);
		/*
		DataIO dio = mConnection.connectIO(mIpAddress, helperPNO);
		DataInputStream dis = dio.getDis();
		DataOutputStream dos = dio.getDos();

		// Request for pop broadcast by this helper
		System.out.println("Send POP");
		dos.writeInt(mHelperID);// 1)helper ID
		dos.writeInt(clockH++);// 2)TimeStamp
		dos.writeInt(MessageType.reqP.ordinal());// 3)Message Type

		// Wait for completing POP		
		DataIO dioReturn = returnC.acceptConnect();
		int ts = dioReturn.getDis().readInt();// 1)TimeStamp
		// Update clock
		clockH = (clockH < ts + 1 ? ts + 2 : ++clockH);
		System.out.println("Last pop Clock " + clockH);*/
	}

	public void requestVOP(int clockH) throws IOException {
			
		DataIO dio = mConnection.connectIO(mIpAddress, helperPNO);		
		DataOutputStream dos = dio.getDos();
		// Request for vop broadcast by this helper
		dos.writeInt(mHelperID);// 1)helper ID
		dos.writeInt(clockH++);// 2)TimeStamp
		dos.writeInt(MessageType.reqV.ordinal());// 3)Message Type
	}

}
