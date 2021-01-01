import models.Mail;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

public class EchoClient {
    public static void main(String[] args) {
        System.out.println("args: " + args[0]);
        try {
            String nomeHost = InetAddress.getLocalHost().getHostName();
            System.out.println(nomeHost);

            Socket socket = new Socket(nomeHost, 8189);

            System.out.println("Ho aperto il socket verso il server");
            ObjectInputStream inputStream = null;

            ObjectOutputStream outputStream = null;
            try {

                switch (args[0]) {
                    case "SEND":
                        System.out.println(Arrays.toString(args));
                        outputStream = new ObjectOutputStream(socket.getOutputStream());

                        outputStream.writeUTF("SEND");
                        outputStream.flush();

                        Mail m = new Mail("luca.cognigni@mail.com", "alessio.vannella@mail.com",
                                "prova ad inviare un messaggio dal client", "eh, ciao",
                                null);


                        outputStream.writeObject(m);
                        outputStream.flush();

                        inputStream = new ObjectInputStream(socket.getInputStream());
                        String status = inputStream.readUTF();
                        System.out.println(status);
                        break;

                    case "READ INBOX":
                        outputStream = new ObjectOutputStream(socket.getOutputStream());

                        outputStream.writeUTF("READ INBOX");
                        outputStream.flush();

                        inputStream = new ObjectInputStream(socket.getInputStream());

                        List<Mail> inList = (List<Mail>) inputStream.readObject();
                        System.out.println("Letta una lista di lunghezza " + inList.size());

                        for (Mail ma : inList) {
                            System.out.println(ma.toString());
                        }

                        break;
                    case "READ OUTBOX":
                        outputStream = new ObjectOutputStream(socket.getOutputStream());

                        outputStream.writeUTF("READ OUTBOX");
                        outputStream.flush();

                        inputStream = new ObjectInputStream(socket.getInputStream());

                        List<Mail> outList = (List<Mail>) inputStream.readObject();
                        System.out.println("Letta una lista di lunghezza " + outList.size());

                        for (Mail ma : outList) {
                            System.out.println(ma.toString());
                        }
                        break;

                    case "DELETE":

                        break;

                    default:
                        System.out.println("No command used");
                        break;
                }

            /*

                // INVIO


                List<Mail> mList = (List<Mail>) inputStream.readObject();
                if (mList != null && mList.size() > 0) {
                    for (Mail mail : mList)
                        System.out.println(mail);
                } else {
                    System.out.println("L'oggetto è null");
                }

                outputStream.flush();
            */
            } catch (SocketException se) {
                se.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null)
                    try {
                        inputStream.close();
                        outputStream.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        } catch (Exception e) {

        }
    }
}
