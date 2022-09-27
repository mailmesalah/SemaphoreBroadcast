package framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StockTerminal {
	public static void main(String []args)
	{
		if (args.length != 2) {
				System.out
					.println("usage: java StockTerminal helperIp helperPort");
			
				System.exit(1);
			}
		DisSem dissem = null;
		System.out.println("Conencting to backend");
		while (true) {

			try {
				if (dissem == null)
					dissem = new DisSem("bla", args[0], Integer.parseInt(args[1]));
				BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
				while(true)
				{
					System.out.printf("Press Enter to add another unit to stock");
						reader.readLine();
						System.out.println("Increasing stock by 1 unit");
						dissem.V();
						
					}
				}
			catch (Throwable e) {
				System.out
						.println("Failed to initialize dissem, will attempt again");

			}

		}
		
		
		
	}
	
}
