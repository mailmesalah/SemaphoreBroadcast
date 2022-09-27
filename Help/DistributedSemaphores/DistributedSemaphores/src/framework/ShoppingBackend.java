package framework;

public class ShoppingBackend {
	public static void main(String [] args)
	{
		if (args.length != 3) {
			System.out
				.println("usage: java ShoppingBackend helperCount coordinatorPort helperPortMin");
		
			System.exit(1);
		}
		final int helperCount=Integer.parseInt(args[0]);
		final int coorPort=Integer.parseInt(args[1]);
		final int minHelperPort=Integer.parseInt(args[2]);
		new Thread(new Runnable() {

			@Override
			public void run() {
				Coordinator coordinator = new Coordinator(coorPort, helperCount);
			}
		}, "coordinator").start();

		for (int i = 0; i < helperCount; i++) {
			DisSemHelper helper = new DisSemHelper("127.0.0.1", coorPort, i,
					minHelperPort + i);
			new Thread(helper, "helper" + i).start();
		}
		System.out.printf("Initial store setup complete,connect upto max %d terminals ",helperCount);
	}
}
