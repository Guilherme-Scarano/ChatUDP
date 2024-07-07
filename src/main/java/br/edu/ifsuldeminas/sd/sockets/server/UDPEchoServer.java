package br.edu.ifsuldeminas.sd.sockets.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class UDPEchoServer {

    private static byte[] BUFFER_OVER_FLOW_MESSAGE = "Dados acima do tamanho.".getBytes();
    private static int MIN_BUFFER_SIZE = 100;
    private static DatagramSocket datagramSocket = null;
    private static byte[] incomingBuffer = null;
    private static int portNumber;
    private static int bufferSize;
    private static boolean isRunning = false;
    private static final Scanner reader = new Scanner(System.in);

    public static void start(int portNumber, int bufferSize) throws UDPEchoServerException {
        validateAttributes(portNumber, bufferSize);
        assignAttributes(portNumber, bufferSize);

        try {
            prepare();
            run();
        } catch (IOException ioException) {
            isRunning = false;
            throw new UDPEchoServerException("Houve algum erro ao executar o servidor de eco UDP.", ioException);
        } finally {
            closeResources();
            System.out.println("Servidor parou devido a erros.");
        }
    }

    public static void stop() {
        if (isRunning) {
            closeResources();
            isRunning = false;
            System.out.println("Servidor parado.");
        } else {
            System.out.println("Servidor já está parado.");
        }
    }

    private static void validateAttributes(int portNumber, int bufferSize) {
        if (portNumber <= 1024) {
            throw new IllegalArgumentException("O servidor UDP não pode usar portas reservadas.");
        }

        if (bufferSize < MIN_BUFFER_SIZE) {
            throw new IllegalArgumentException(String.format("O buffer de mensagem precisa ser maior que %d", MIN_BUFFER_SIZE));
        }
    }

    private static void assignAttributes(int portNumber, int bufferSize) {
        UDPEchoServer.portNumber = portNumber;
        UDPEchoServer.bufferSize = bufferSize;
    }

    private static void prepare() throws SocketException {
        if (isRunning) {
            stop();
        }
        datagramSocket = new DatagramSocket(portNumber);
        incomingBuffer = new byte[bufferSize];
    }

    private static void run() throws IOException {
        System.out.printf("Servidor de eco rodando em '%s:%d' ...\n", InetAddress.getLocalHost().getHostAddress(), portNumber);
        isRunning = true;

        while (true) {
            DatagramPacket received = receive();
            String receivedMessage = new String(received.getData(), 0, received.getLength());
            System.out.printf("Mensagem: '%s' de '%s'\n", receivedMessage, received.getAddress().getHostAddress());

            System.out.print("Escreva uma resposta: ");
            String responseMessage = reader.nextLine();
            byte[] responseBytes = responseMessage.getBytes();
            reply(received, responseBytes, responseBytes.length);

            DatagramPacket clientResponsePacket = receive();
            String clientResponseMessage = new String(clientResponsePacket.getData(), 0, clientResponsePacket.getLength());
            System.out.printf("Mensagem: '%s' de '%s'\n", clientResponseMessage, clientResponsePacket.getAddress().getHostAddress());

            System.out.print("Escreva uma mensagem: ");
            String newResponseMessage = reader.nextLine();
            byte[] newResponseBytes = newResponseMessage.getBytes();
            reply(clientResponsePacket, newResponseBytes, newResponseBytes.length);
        }
    }

    private static DatagramPacket receive() throws IOException {
        DatagramPacket received = new DatagramPacket(incomingBuffer, incomingBuffer.length);
        datagramSocket.receive(received);
        return received;
    }

    private static void reply(DatagramPacket received, byte[] message, int bufferSize) throws IOException {
        DatagramPacket reply = new DatagramPacket(message, bufferSize, received.getAddress(), received.getPort());
        datagramSocket.send(reply);
        System.out.printf("Servidor respondendo %s:%d\n", received.getAddress().getHostAddress(), received.getPort());
    }

    private static void closeResources() {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
        datagramSocket = null;
    }
}