package framework;

import java.io.IOException;

import org.junit.Test;

public class SystemTest {

	public static void main(String[] args) throws IOException,
			InterruptedException {
		final int helperCount = 4;

		new Thread(new Runnable() {

			@Override
			public void run() {
				Coordinator coordinator = new Coordinator(9900, helperCount);
			}
		}, "coordinator").start();

		for (int i = 0; i < helperCount; i++) {
			DisSemHelper helper = new DisSemHelper("127.0.01", 9900, i,
					9910 + i);
			new Thread(helper, "helper" + i).start();
		}
		DisSem dissem = null;
		new Thread(new Runnable() {

			@Override
			public void run() {
				DisSem dissem = null;
				while (true) {

					try {
						if (dissem == null)
							dissem = new DisSem("bla", "127.0.0.1", 9911);
						dissem.P();
						Thread.sleep(5000);
						dissem.V();
						Thread.sleep(2000);
						dissem.P();
						System.out.println("Second user done processing");
						return;
					} catch (IOException | InterruptedException e) {

						e.printStackTrace();
					} catch (Throwable e) {
						System.out
								.println("Failed to initialize dissem, will attempt again");

					}

				}
			}
		}).start();
		while (true) {
			try {
				if (dissem == null)
					dissem = new DisSem("bla", "127.0.0.1", 9910);
				Thread.sleep(3000);
				dissem.V();
				Thread.sleep(3000);
				dissem.P();
				Thread.sleep(9000);
				dissem.V();
				dissem.V();
				Thread.sleep(900000);
				System.out.println("First user done processing");
				return;
			} catch (Throwable e) {
				System.out
						.println("Failed to initialize dissem, will attempt again");
			}
		}

	}

}
