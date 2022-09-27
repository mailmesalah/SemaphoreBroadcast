package broadcastsemaphore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Initiator {
	//All Helpers
	int mHelperIDs[];
	String mIpAddresses[];
	int mPortNumbers[];
	DataInputStream[] disHelpers;
	DataOutputStream[] dosHelpers;
	
	String mIpAddress ="";
	int mPortNo;
	int mNoOfNodes;
	
	Connection mConnection;
	
	public Initiator(String mIpAddress, int mPortNo, int mNoOfNodes) {
		super();
		this.mIpAddress = mIpAddress;
		this.mPortNo = mPortNo;
		this.mNoOfNodes= mNoOfNodes;
		
		mConnection = new Connection(mPortNo);
		mHelperIDs= new int [mNoOfNodes];
		mIpAddresses= new String[mNoOfNodes];
		mPortNumbers = new int[mNoOfNodes];
		disHelpers=new DataInputStream[mNoOfNodes];
		dosHelpers= new DataOutputStream[mNoOfNodes];
	}	
	
	public void connectWithAllHelpers() throws IOException{
		//Connect with all helpers and grabs their details
		for (int i = 0; i < mNoOfNodes; i++) {
			DataIO dio = mConnection.acceptConnect();
			DataInputStream dis = dio.getDis();
			DataOutputStream dos = dio.getDos();
			disHelpers[i]=dis;
			dosHelpers[i]=dos;
			//Reading Helper Details
			mHelperIDs[i]=dis.readInt();//1)Reading Helper ID
			mIpAddresses[i]=dis.readUTF();//2)Reading Helper IPAddress
			mPortNumbers[i]=dis.readInt();//3)Reading Helper Port No					
		}
	}
	
	public void distributeHelperDetails() throws IOException{
		//Distributes all helper details to each helper
		for (int i = 0; i < mNoOfNodes; i++) {
			dosHelpers[i].writeInt(mNoOfNodes);//4)Number Of Nodes
			for (int j = 0; j < mNoOfNodes; j++) {				
					dosHelpers[i].writeInt(mHelperIDs[j]);//5)Helper ID
					dosHelpers[i].writeUTF(mIpAddresses[j]);//6)Helper IP Address
					dosHelpers[i].writeInt(mPortNumbers[j]);//7)Helper Port No
					System.out.println(mHelperIDs[j]);				
			}
		}
	}
	
	public static void main(String args[]) {
		if (args.length == 3) {
			String ipAdd = args[0];
			int portN = Integer.parseInt(args[1]);
			int nOfNodes = Integer.parseInt(args[2]);
			try {
				Initiator init = new Initiator(ipAdd, portN, nOfNodes);
				init.connectWithAllHelpers();
				init.distributeHelperDetails();

				//Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}else{
			System.out.println("Please provide: java broadcastsemaphore.Initiator ipaddress portno noofnodes");
		}
	}
}
