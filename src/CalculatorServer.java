import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorServer {
    // 프로토콜 메시지 목록 정의
    private static final List<String> PROTOCOLS = new ArrayList<>() {
        {
            add("ADD"); // 덧셈 프로토콜
            add("SUB"); // 뺄셈 프로토콜
            add("MUL"); // 곱셈 프로토콜
            add("DIV"); // 나눗셈 프로토콜
        }
    };

    public static void main(String[] args) throws IOException {
        Scanner scanner;
        int portNum = 7777;
        ServerSocket listener;

        // 서버 정보를 저장한 파일 읽기
        try {
            scanner = new Scanner(new FileInputStream("./server_info.dat"));
            portNum = scanner.nextInt();
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("No file to read.");
        }

        listener = new ServerSocket(portNum);
        System.out.println("The Calculator server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(20);

        while (true) {
            Socket sock = listener.accept();
            pool.execute(new Calculator(sock));
        }
    }

    private static String calculate(String packet) {
        String result = "";
        StringTokenizer stringTokenizer = new StringTokenizer(packet);
        String operator = stringTokenizer.nextToken();

        if (!PROTOCOLS.contains(operator)) {
            result = result + "FAIL BAD_REQUEST"; // 잘못된 요청 처리
        } else {
            List<Integer> request = new ArrayList<>();
            try {
                while (stringTokenizer.hasMoreTokens()) {
                    request.add(Integer.parseInt(stringTokenizer.nextToken()));
                }
            } catch (NumberFormatException e1) {
                result = "FAIL NOT_A_NUMBER"; // 숫자가 아닌 값 처리
                return result;
            }
            if(request.size() == 1){
                result = "FAIL NOT_ENOUGH_ARGUMENT";
                return result;
            }
            switch (operator) {
                case "ADD":
                    int sum = 0;
                    for (int i : request) {
                        sum += i;
                    }
                    result = result + "SUCCESS " + sum; // 덧셈 결과 반환
                    break;
                case "SUB":
                    if (request.size() > 2) {
                        result = result + "FAIL TOO_MANY_ARGUMENTS"; // 잘못된 인수 처리
                        break;
                    }
                    result = result + "SUCCESS " + (request.get(0) - request.get(1)); // 뺄셈 결과 반환
                    break;
                case "MUL":
                    int product = 1;
                    for (int i : request) {
                        product *= i;
                    }
                    result = result + "SUCCESS " + product; // 곱셈 결과 반환
                    break;
                case "DIV":
                    if (request.size() != 2) {
                        result = result + "FAIL TOO_MANY_ARGUMENTS"; // 잘못된 인수 처리
                        break;
                    } else if (request.get(1) == 0) {
                        result = result + "FAIL DIVIDED_BY_ZERO"; // 0으로 나누는 경우 처리
                        break;
                    }
                    result = result + "SUCCESS " + (request.get(0) / request.get(1)); // 나눗셈 결과 반환
                    break;
                default:
                    result = result + "FAIL UNKNOWN_ERROR"; // 알 수 없는 오류 처리
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
            System.out.println("CONNECTED: " + socket);
            try {
                Scanner in = new Scanner(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    String packet = in.nextLine();
                    if (packet.equals("BYE")) {
                        System.out.println("FINISH CALCULATOR.");
                        break;
                    }
                    String result = calculate(packet);
                    out.println(result);
                    out.flush();
                }
            } catch (Exception e) {
                System.out.println("ERROR:" + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("CLOSED: " + socket);
            }
        }
    }
}
