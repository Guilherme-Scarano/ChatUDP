package br.edu.ifsuldeminas.sd.sockets.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class UDPClient {

    private static final int TIME_OUT = 5000;
    private static int SERVER_PORT = 4000;
    private static int BUFFER_SIZE = 200;
    private static String KEY_TO_EXIT = "q";

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        String stringMessage = "";

        try {
            InetAddress serverAddress = InetAddress.getLocalHost();
            DatagramSocket datagramSocket = new DatagramSocket();

            while (!stringMessage.equals(KEY_TO_EXIT)) {
                System.out.printf("Escreva uma mensagem (%s para sair): ", KEY_TO_EXIT);
                stringMessage = reader.nextLine();

                if (!stringMessage.equals(KEY_TO_EXIT)) {
                    byte[] message = stringMessage.getBytes();
                    DatagramPacket datagramPacketToSend = new DatagramPacket(
                            message, message.length, serverAddress, SERVER_PORT);
                    datagramSocket.setSoTimeout(TIME_OUT);
                    datagramSocket.send(datagramPacketToSend);

                    // Recebe uma resposta do servidor
                    byte[] responseBuffer = new byte[BUFFER_SIZE];
                    DatagramPacket datagramPacketForResponse = new DatagramPacket(
                            responseBuffer, responseBuffer.length);

                    try {
                        datagramSocket.receive(datagramPacketForResponse);
                        String response = new String(datagramPacketForResponse.getData(), 0, datagramPacketForResponse.getLength());
                        System.out.printf("Resposta do servidor: %s\n", response);

                        // Envia uma resposta para o servidor
                        System.out.print("Escreva uma resposta: ");
                        String responseToServer = reader.nextLine();
                        byte[] responseBytes = responseToServer.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBytes, responseBytes.length, serverAddress, SERVER_PORT);
                        datagramSocket.send(responsePacket);

                        // Recebe uma nova resposta do servidor
                        byte[] newResponseBuffer = new byte[BUFFER_SIZE];
                        DatagramPacket newResponsePacket = new DatagramPacket(
                                newResponseBuffer, newResponseBuffer.length);

                        try {
                            datagramSocket.receive(newResponsePacket);
                            String newResponse = new String(newResponsePacket.getData(), 0, newResponsePacket.getLength());
                            System.out.printf("Resposta do servidor: %s\n", newResponse);
                        } catch (SocketTimeoutException e) {
                            System.out.printf("Sem resposta do servidor de eco UDP.\n");
                        }

                    } catch (SocketTimeoutException e) {
                        System.out.printf("Sem resposta do servidor de eco UDP.\n");
                    }

                } else {
                    System.out.printf("Cliente saindo com %s ...\n", KEY_TO_EXIT);
                }
            }

            datagramSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}