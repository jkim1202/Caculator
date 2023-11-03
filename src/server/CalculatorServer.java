package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorServer {
    // protocol message
    private static final List<String> PROTOCOLS = new ArrayList<>(){{
        add("ADD");
        add("SUB");
        add("MUL");
        add("DIV");
        }
    };
    public static void main(String[] args) throws IOException {
        Scanner scanner;
        int portNum = 7777;

        // server_info.dat 에서 port번호 읽기
        try{
            scanner = new Scanner(new FileInputStream("./server_info.dat"));
            portNum = scanner.nextInt();
            scanner.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }

        ServerSocket listener = new ServerSocket(portNum);
        System.out.println("The Calculator server is running...");
        ExecutorService pool = Executors.newFixedThreadPool (20);
        while (true) {
            Socket sock = listener.accept();
            pool.execute(new Calculator(sock));
        }
    }
    private static String calculate(String packet){
        String result = "";
        StringTokenizer stringTokenizer = new StringTokenizer(packet);
        String operator = stringTokenizer.nextToken();
        if(!PROTOCOLS.contains(operator)){
            result = result + "FAIL BAD_REQUEST";
        }
        else{
            List<Integer> request = new ArrayList<>();
            while(stringTokenizer.hasMoreTokens()){
                request.add(Integer.parseInt(stringTokenizer.nextToken()));
            }
            switch (operator) {
                case "ADD":
                    int sum = 0;
                    for(int i : request){
                        sum += i;
                    }
                    result = result + "SUCCESS " + sum;
                    break;
                case "SUB":
                    if(request.size()!=2){
                        result = result + "FAIL TOO_MANY_ARGUMENTS";
                        break;
                    }
                    result = result + "SUCCESS " + (request.get(0) - request.get(1));
                    break;
                case "MUL":
                    int product = 1;
                    for(int i : request){
                        product *= i;
                    }
                    result = result + "SUCCESS " + product;
                    break;
                case "DIV":
                    if(request.size()!=2){
                        result = result + "FAIL TOO_MANY_ARGUMENTS";
                        break;
                    } else if (request.get(1)==0) {
                        result = result + "FAIL DIVIDED_BY_ZERO";
                        break;
                    }
                    result = result + "SUCCESS " + (request.get(0) / request.get(1));
                    break;
                default:
                    result = result + "FAIL UNKNOWN_ERROR";
            }
        }

        return result;
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
                while(true){
                    String packet = in.nextLine();
                    if(packet.equals("DONE")){
                        System.out.println("Finish Calculator.");
                        break;
                    }
                    String result = calculate(packet);
                    out.println(result);
                    out.flush();
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

