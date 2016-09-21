import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client {
	private Socket socket;
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	OutputStream out;
	InputStream in;

	public Client(String host, int port) throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
		receive();
		send();
	}

	public void send() throws IOException {
		while (true) {
			System.out.print("Enter msg:\n");
			String msg = br.readLine();

			// send msg to server
			try {
				out.write(msg.getBytes());
			} catch (IOException e) {
				socket.close();
			}
		}
	}

	public void receive() {
		new Thread() {
			public void run() {
				byte[] buffer;
				boolean running = true;
				while (running) {
					buffer = new byte[1024];
					try {
						in.read(buffer);
						System.out.println("msg received: " + new String(buffer, StandardCharsets.UTF_8));
					} catch (IOException e) {
					}
				}

			}
		}.start();
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		new Client("localhost", 1234);
	}
}

