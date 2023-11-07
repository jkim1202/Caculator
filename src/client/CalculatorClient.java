package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CalculatorClient {
    public static void main(String[] args) throws Exception {
        Scanner fileScanner;
        int portNum = 7777;
        String ip = "localhost";

        try {
            fileScanner = new Scanner(new FileInputStream("./server_info.dat"));
            portNum = fileScanner.nextInt();
            ip = fileScanner.nextLine();
            fileScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("No file to read.");
        }

        Socket socket = new Socket(ip, portNum);
        Scanner scanner = new Scanner(System.in);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            System.out.println("계산식을 입력하세요(예: ADD 10 20 30)/종료하려면 BYE를 입력하세요 >> ");
            String outputMessage = scanner.nextLine(); // 키보드에서 수식 입력

            if (outputMessage.equalsIgnoreCase("bye")) {
                out.write(outputMessage.toUpperCase() + "\n"); // "BYE" 문자열 전송
                out.flush();
                break; // 사용자가 "bye"를 입력하면 서버로 전송 후 연결 종료
            }
            out.write(outputMessage.toUpperCase() + "\n"); // 키보드에서 읽은 수식 문자열 전송
            out.flush();

            String inputMessage = in.readLine(); // 서버로부터 계산 결과 수신
            StringTokenizer stringTokenizer = new StringTokenizer(inputMessage);
            String responseType = stringTokenizer.nextToken();
            String answer = stringTokenizer.nextToken();

            if (responseType.equals("FAIL")) {
                System.out.println("Error Message\n" + answer);
            } else {
                System.out.println("ANSWER: " + answer);
            }
        }

        try {
            socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
