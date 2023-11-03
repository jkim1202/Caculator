import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorServer {
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(7777);
        System.out.println("The capitalization server is running...");
        ExecutorService pool = Executors.newFixedThreadPool (20);
        while (true) {
            Socket sock = listener.accept();
            pool.execute(new Calculator(sock));
        }
    }
    private static class Calculator implements Runnable {
        private Socket socket;
        Calculator(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            try {
                Scanner in = new Scanner(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream (), true);
                while (in.hasNextLine()) {
                    out.println(in.nextLine().toUpperCase());
                }
            }
            catch (Exception e) {
                System.out.println("Error:" + socket);
            }
            finally {
                try { socket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Closed: " + socket);
            }
        }
    }
}

