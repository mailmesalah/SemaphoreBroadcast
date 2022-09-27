package framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BuyTerminal {
	public static void main(String []args)
	{
		if (args.length != 2) {
				System.out
					.println("usage: java BuyTerminal helperIp helperPort");
			
				System.exit(1);
			}
		DisSem dissem = null;
		System.out.println("Conencting to backend");
		while (true) {

			try {
				if (dissem == null)
					dissem = new DisSem("bla", args[0], Integer.parseInt(args[1]));
				BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
				int personalStock=5;
				while(true)
				{
					System.out.printf("Currently Holding %d units\nSelect an operation:\n1 Buy another unit\n2 Return a unit\n",personalStock);
					int i;
					try{
					i=Integer.parseInt(reader.readLine());
					if(i!=1&&i!=2) throw new Error("Invalid input");
					}catch(Throwable e)
					{
						System.out.println("Invalid input, try again");
						continue;
					}
					if(i==2&&personalStock<=0)
					{
						System.out.println("Personal stock 0, can't return");
						continue;
					}
					if(i==2)
					{
						System.out.println("Attempting return");
						dissem.V();
						personalStock--;
						System.out.println("Done return");
					}
					else if(i==1)
					{
						System.out.println("Attempting purchase");
						dissem.P();
						personalStock++;
						System.out.println("Done purchase");
						
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
			} catch (Throwable e) {
				System.out
						.println("Failed to initialize dissem, will attempt again");

			}

		}
		
		
		
	}
	
}
