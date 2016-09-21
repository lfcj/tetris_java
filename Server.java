import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Server {
	private ServerSocket server;
	private Semaphore mutex = new Semaphore(1, true);
	private List<Socket> client_sockets = new ArrayList<Socket>();
	private boolean running;

	public Server(int port) throws IOException, InterruptedException {
		server = new ServerSocket(port);
		running = true;
		listen();
	}

	public void listen() throws IOException, InterruptedException {
		while (running) {
			Socket client_sckt = server.accept();
			// add socket to list protected by semaphores
			mutex.acquire();
			client_sockets.add(client_sckt);
			mutex.release();
			new ServerThread(client_sckt).run();
		}
		server.close();
	}

	public class ServerThread implements Runnable {
		private Socket client_socket;
		private InputStream in;
		byte[] buffer;
		boolean running_server_thread = true;

		public ServerThread(Socket c_s) throws IOException {
			this.client_socket = c_s;
			this.in = client_socket.getInputStream();
		}

		private void receive() throws IOException, InterruptedException {
			buffer = new byte[1024];
			in.read(buffer);
			System.out.println("From the other side:" + new String(buffer, StandardCharsets.UTF_8));
			broadcast();
		}

		private void broadcast() throws InterruptedException{
			mutex.acquire();
			// if no clients, stop thread
			running_server_thread = !client_sockets.contains(client_socket);
			Iterator<Socket> iterator = client_sockets.iterator();
			Socket cs;
			while (iterator.hasNext()) {
				cs = iterator.next();
				try {
					cs.getOutputStream().write(buffer);
				} catch (IOException e) {
					// Socket is broken.					
					try {
						cs.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					client_sockets.remove(cs);
					iterator = client_sockets.iterator();
				}
			}
			mutex.release();
		}

		@Override
		public void run() {
			while (running_server_thread) {
				try {
					receive();
				} catch (IOException | InterruptedException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		 new Server(1234);
//		List<Integer> test = new ArrayList<Integer>();
//		 test.add(1);
//		for (Integer e : test) {
//			System.out.println(e);
//		}
	}

}

