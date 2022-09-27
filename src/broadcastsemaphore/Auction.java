package broadcastsemaphore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Auction {
	public static void main(String []args)
	{
		//Initiator
		new Thread(new Runnable(){
			@Override
			public void run() {
				Initiator init = new Initiator("127.0.0.1", 5555, 4);
				try {
					init.connectWithAllHelpers();
					init.distributeHelperDetails();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}							
			}			
		}).start();
		
		//Helper
		ArrayList<DisSemHelper> al = new ArrayList();
		for (int i = 0; i < 4; i++) {
			DisSemHelper dsh = new DisSemHelper(i, "127.0.0.1", 5556+i,"127.0.0.1", 5555);
			try {
				dsh.connectWithInitiator();
				al.add(dsh);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}						
		}
		for (int i = 0; i < 4; i++) {
			try {
				DisSemHelper dsh=al.get(i);
				dsh.receiveHelperDetails();
				dsh.waitForIncomingData();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		
			
		//User
		int localStock=0;
		DisSem ds = new DisSem(5556, "127.0.0.1", 0);
		int clock=0;
		while(true)
		{			
			try {
				System.out.println("Enter 1 for adding to cart, Enter 2 for removing from cart");
				int value=Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
				if(value==1){
					++localStock;					
					System.out.println("Number of Items on Cart Now : "+localStock);
					ds.requestVOP(clock++);
				}else if(value==2){
					if(localStock!=0){
						--localStock;						
						System.out.println("Number of Items on Cart Now : "+localStock);
						ds.requestPOP(clock++);
					}else{
						System.out.println("Cart is empty");
					}
				}else{
					System.out.println("Wrong choice, Try again...");
				}
			
			} catch (NumberFormatException | IOException e) {
				System.out.println();
			}
			
		}
	}
	
}
