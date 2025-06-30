package pl.edu.uws.lw89233;

import pl.edu.uws.lw89233.managers.EnvManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class API_Gateway {

    private final int SERVER_PORT = Integer.parseInt(EnvManager.getEnvVariable("SERVER_PORT"));

    private final String REGISTRATION_MICROSERVICE_HOST = EnvManager.getEnvVariable("REGISTRATION_MICROSERVICE_HOST");
    private final int REGISTRATION_MICROSERVICE_PORT = Integer.parseInt(EnvManager.getEnvVariable("REGISTRATION_MICROSERVICE_PORT"));

    private final String LOGIN_MICROSERVICE_HOST = EnvManager.getEnvVariable("LOGIN_MICROSERVICE_HOST");
    private final int LOGIN_MICROSERVICE_PORT = Integer.parseInt(EnvManager.getEnvVariable("LOGIN_MICROSERVICE_PORT"));

    private final String POSTS_MICROSERVICE_HOST = EnvManager.getEnvVariable("POSTS_MICROSERVICE_HOST");
    private final int POSTS_MICROSERVICE_PORT = Integer.parseInt(EnvManager.getEnvVariable("POSTS_MICROSERVICE_PORT"));

    private final String LAST_10_POSTS_MICROSERVICE_HOST = EnvManager.getEnvVariable("LAST_10_POSTS_MICROSERVICE_HOST");
    private final int LAST_10_POSTS_MICROSERVICE_PORT = Integer.parseInt(EnvManager.getEnvVariable("LAST_10_POSTS_MICROSERVICE_PORT"));

    private final String FILE_TRANSFER_MICROSERVICE_HOST = EnvManager.getEnvVariable("FILE_TRANSFER_MICROSERVICE_HOST");
    private final int FILE_TRANSFER_MICROSERVICE_PORT = Integer.parseInt(EnvManager.getEnvVariable("FILE_TRANSFER_MICROSERVICE_PORT"));

    public void runServer() throws IOException {
        try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
            System.out.println("API Gateway is running on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = server.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            new API_Gateway().runServer();
        } catch (IOException e) {
            System.err.println("Failed to start API Gateway: " + e.getMessage());
        }
    }

    private class ClientHandler extends Thread {

        private final Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String request;
                while ((request = in.readLine()) != null) {
                    String type = extractRequestType(request);

                    switch (type) {
                        case "registration_request":
                            forwardRequest(request, REGISTRATION_MICROSERVICE_HOST, REGISTRATION_MICROSERVICE_PORT, out);
                            break;
                        case "login_request":
                            forwardRequest(request, LOGIN_MICROSERVICE_HOST, LOGIN_MICROSERVICE_PORT, out);
                            break;
                        case "send_post_request":
                            forwardRequest(request, POSTS_MICROSERVICE_HOST, POSTS_MICROSERVICE_PORT, out);
                            break;
                        case "retrive_last_10_posts_request":
                            forwardRequest(request, LAST_10_POSTS_MICROSERVICE_HOST, LAST_10_POSTS_MICROSERVICE_PORT, out);
                            break;
                        case "get_file_request":
                        case "send_file_request":
                            forwardRequest(request, FILE_TRANSFER_MICROSERVICE_HOST, FILE_TRANSFER_MICROSERVICE_PORT, out);
                            break;
                        default:
                            out.println("Unknown request type: " + type);
                            break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Client communication error: " + e.getMessage());
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        }

        private String extractRequestType(String request) {
            String[] parts = request.split("#");
            for (String part : parts) {
                if (part.startsWith("type:")) {
                    return part.split(":")[1];
                }
            }
            return null;
        }

        private void forwardRequest(String request, String host, int port, PrintWriter clientOut) {
            try (Socket serviceSocket = new Socket(host, port);
                 PrintWriter serviceOut = new PrintWriter(serviceSocket.getOutputStream(), true);
                 BufferedReader serviceIn = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()))) {

                serviceOut.println(request);

                if (request.contains("get_file_request") || request.contains("send_file_request")) {
                    String response;
                    while ((response = serviceIn.readLine()) != null) {
                        clientOut.println(response);

                        if (response.contains("package_number:-1")) {
                            break;
                        }

                        String clientAck = in.readLine();
                        serviceOut.println(clientAck);
                    }
                } else {
                    String response = serviceIn.readLine();
                    clientOut.println(response);
                }

            } catch (IOException e) {
                System.err.println("Error forwarding request to " + host + ":" + port + " - " + e.getMessage());
                clientOut.println("Service unavailable");
            }
        }
    }
}