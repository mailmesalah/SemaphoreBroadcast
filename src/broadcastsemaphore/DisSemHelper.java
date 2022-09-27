package broadcastsemaphore;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.sun.org.apache.xml.internal.security.utils.HelperNodeList;

import broadcastsemaphore.Message.MessageType;
import sun.security.util.DisabledAlgorithmConstraints;

public class DisSemHelper {
	// This Helper details
	int mHelperID = 0;
	String mIpAddress = "";
	int mPortNo = 0;
	// Initiator Details
	String mInitiatorIP = "";
	int mInitiatorPNo = 0;
	DataInputStream disInitiator;
	DataOutputStream dosInitiator;

	// All Helpers
	int mHelperIDs[];
	String mIpAddresses[];
	int mPortNumbers[];
	DataInputStream[] disHelpers;
	DataOutputStream[] dosHelpers;

	Connection mConnection;
	int mNoOfNodes;

	// Semaphore and Clock
	int semaPhore;
	int clockH;
	private int returnPort;

	public DisSemHelper(int mHelperID, String mIpAddress, int mPortNo,
			String mInitiatorIP, int mInitiatorPNo) {
		super();
		this.mHelperID = mHelperID;
		this.mIpAddress = mIpAddress;
		this.mPortNo = mPortNo;
		this.mInitiatorIP = mInitiatorIP;
		this.mInitiatorPNo = mInitiatorPNo;

		returnPort = 5000 + mHelperID;
		if (mPortNo == 5000 + mHelperID) {
			System.out
					.println("The port number is reserved, use another port number");
			System.exit(0);
		}
		mConnection = new Connection(mPortNo);
		clockH = 0;
	}

	public void connectWithInitiator() throws IOException {
		// Connect with all helpers and grabs their details
		DataIO dio = mConnection.connectIO(mInitiatorIP, mInitiatorPNo);
		DataInputStream dis = dio.getDis();
		DataOutputStream dos = dio.getDos();
		disInitiator = dis;
		dosInitiator = dos;
		// Send this Helper Details
		dos.writeInt(mHelperID);// 1)Send Helper ID
		dos.writeUTF(mIpAddress);// 2)Send Helper IPAddress
		dos.writeInt(mPortNo);// 3)Send Helper Port No
	}

	public void receiveHelperDetails() throws IOException {
		// Receives all other helper details to this helper
		mNoOfNodes = disInitiator.readInt();// 4)Number Of Nodes
		mHelperIDs = new int[mNoOfNodes];
		mIpAddresses = new String[mNoOfNodes];
		mPortNumbers = new int[mNoOfNodes];
		disHelpers = new DataInputStream[mNoOfNodes];
		dosHelpers = new DataOutputStream[mNoOfNodes];
		for (int i = 0; i < mNoOfNodes; i++) {
			mHelperIDs[i] = disInitiator.readInt();// 5)Helper ID
			mIpAddresses[i] = disInitiator.readUTF();// 6)Helper IP Address
			mPortNumbers[i] = disInitiator.readInt();// 7)Helper Port No
			System.out.println(mHelperIDs[i]);
		}

	}

	// Helper fields
	// ArrayList<Message> messageQueue = new ArrayList();

	public void waitForIncomingData() {
		// Waits for incoming data and once received the data will be processed
		// and send to other helpers
		// Sends an acknowledge back to sender
		class RunBack extends Thread {

			RunBack() {
				setDaemon(true);
				start();				
			}

			@Override
			public void run() {

				ArrayList<Message> messageQueue = new ArrayList<>();
				ArrayList ackQueue = new ArrayList();
				int clock = 0;
				int s = 0;

				while (true) {
					try {
						DataIO dioHelper = mConnection.acceptConnect();
						DataInputStream disThisHelper = dioHelper.getDis();

						// Receives Semaphore

						System.out.println("Message Start");
						int hID = disThisHelper.readInt();// 1)Sender helper ID
						System.out.println("Helper ID " + hID);
						int ts = disThisHelper.readInt();// 2)Sender Time Stamp
						System.out.println("Time Stamp " + ts);
						MessageType mt = MessageType.values()[disThisHelper
								.readInt()];// 3)Sender Type
						System.out.println("Message Type " + mt.toString());
						System.out.println("Message End");
						Message m = new Message(hID, mt, ts);
						// Calculates clock
						clock = (clock < ts + 1 ? ts + 2 : ++clock);

						if (MessageType.reqP == mt) {
							// if requestPOP broadcast POP, update clock
							for (int j = 0; j < mNoOfNodes; j++) {
								DataOutputStream dos = mConnection
										.connect2write(mIpAddresses[j],
												mPortNumbers[j]);
								dos.writeInt(mHelperID);// 1) helper id
								dos.writeInt(clock);// 2)Time Stamp
								dos.writeInt(MessageType.POP.ordinal());// 2)Message
																		// Type
								++clock;
							}
						} else if (MessageType.reqV == mt) {
							// if requestVOP broadcast POP, update clock
							for (int j = 0; j < mNoOfNodes; j++) {
								DataOutputStream dos = mConnection
										.connect2write(mIpAddresses[j],
												mPortNumbers[j]);
								dos.writeInt(mHelperID);// 1) helper id
								dos.writeInt(clock);// 2)Time Stamp
								dos.writeInt(MessageType.VOP.ordinal());// 2)Message
																		// Type
								++clock;
							}
						} else if (MessageType.VOP == mt
								|| MessageType.POP == mt) {
							// if POP or VOP save message in correct
							// position of their message queue
							int index = 0;

							for (int j = 0; j < messageQueue.size(); j++) {
								Message m1 = messageQueue.get(j);
								if (m1.timeS > m.timeS) {
									index = j;
									break;
								} else if (m1.timeS == m.timeS) {
									// if both time stamps are equal
									// consider the helper ids
									if (hID < mHelperIDs[j]) {
										index = j;
									} else {
										index = j + 1;
									}
									break;
								} else {
									// last index
									index = j + 1;
								}
							}
							messageQueue.add(index, m);

							// Broadcast ACK and update clock
							for (int j = 0; j < mNoOfNodes; j++) {
								DataOutputStream dos = mConnection
										.connect2write(mIpAddresses[j],
												mPortNumbers[j]);
								dos.writeInt(mHelperID);// 1) helper id
								dos.writeInt(clock);// 2)Time Stamp
								dos.writeInt(MessageType.ACK.ordinal());// 3)Message
																		// Type
								++clock;
							}
						} else if (MessageType.ACK == mt) {
							System.out.println("S value " + s);
							// if ACK
							ackQueue.add(m.timeS);
							// VOP Acknowledge
							for (int i = 0; i < ackQueue.size(); i++) {
								for (int j = 0; j < messageQueue.size(); j++) {
									Message m1 = messageQueue.get(j);
									int timeStamp = (int) ackQueue.get(i);
									if (timeStamp >= m1.timeS
											&& m1.getType() == MessageType.VOP) {
										// Remove the VOP message and increment
										// s
										messageQueue.remove(m1);
										--j;
										++s;
									}
								}
							}							
							// Remove any POP operation if left with
							// decrementing s if s value is greater than 0
							if (s > 0) {
								for (int i = 0; i < ackQueue.size(); i++) {
									for (int j = 0; j < messageQueue.size(); j++) {									
										if (s <= 0) {									
											break;
										}
										Message m1=messageQueue.get(j);
										int timeStamp = (int) ackQueue.get(i);
										if (timeStamp >= m1.timeS && m1.getType() == MessageType.POP) {
																						
											// respond to the pop request
											if (messageQueue.get(j).senderID == mHelperID) {
												System.out
														.println("End of POP");
												DataOutputStream dosReturn = mConnection
														.connect2write(
																mIpAddress,
																returnPort);
												dosReturn.writeInt(clock++);
											}
											
											// Remove from queue
											messageQueue.remove(j);
											--j;
											--s;// semaphore											
										}
									}
								}
							}
						}

					} catch (IOException e) {
						System.out.println(e.getCause() + "here");
					}

				}
			}
		}

		new RunBack();

	}

	public static void main(String args[]) {
		if (args.length == 5) {
			int helperID = Integer.parseInt(args[0]);
			String ipAdd = args[1];
			int portN = Integer.parseInt(args[2]);
			if (5000 + helperID == portN) {
				System.out
						.println("The port number is reserved, use another port number");
				System.exit(0);
			}
			String initIP = args[3];
			int initPN = Integer.parseInt(args[4]);
			try {
				DisSemHelper dsh = new DisSemHelper(helperID, ipAdd, portN,
						initIP, initPN);

				dsh.connectWithInitiator();
				dsh.receiveHelperDetails();
				System.out.println("Worked till here");
				// Semaphore broadcast implementation
				// dsh.connectWithAllHelpers();
				// Wait for incoming data from Workers
				dsh.waitForIncomingData();

				DisSem ds = new DisSem(portN, ipAdd, helperID);
				int clock = 0;
				while (true) {
					System.out
							.println("Enter 1 to Send VOP and 2 to Send POP Test from this Helper");
					int ret = Integer.parseInt(new BufferedReader(
							new InputStreamReader(System.in)).readLine());
					if (ret == 1) {
						// Send VOP here
						ds.requestVOP(clock++);
					} else if (ret == 2) {
						// Send POP here
						ds.requestPOP(clock++);
					}
				}

			} catch (Exception e) {
				System.out.println("Error " + e.getMessage());
			}
		} else {
			System.out
					.println("Please provide: java broadcastsemaphore.DisSemHelper helperid ipaddress portno initiatorip initiatorportno");
		}
	}
}
