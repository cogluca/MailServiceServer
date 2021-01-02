import models.Mail;
import models.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EchoClient {
    public static final String sessID = "123Stella";
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
                    case "LOGIN":
                        outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeUTF("LOGIN");
                        outputStream.flush();

                        outputStream.writeUTF(sessID);
                        outputStream.flush();

                        outputStream.writeObject(new User("alessio.vannella@mail.com"));
                        //outputStream.writeUTF("alessio.vannella@mail.com");
                        outputStream.flush();

                        inputStream = new ObjectInputStream(socket.getInputStream());
                        System.out.println(inputStream.readUTF());
                        break;

                    case "SEND":
                        System.out.println(Arrays.toString(args));
                        outputStream = new ObjectOutputStream(socket.getOutputStream());

                        outputStream.writeUTF("SEND");
                        outputStream.flush();

                        outputStream.writeUTF(sessID);
                        outputStream.flush();

                        User sender = new User("luca.cognigni@mail.com");
                        List<User> receiver = new ArrayList<>();
                        receiver.add(new User("alessio.vannella@mail.com"));

                        Mail m = new Mail(sender, receiver,
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

                    case "LOGOUT":
                        outputStream = new ObjectOutputStream(socket.getOutputStream());

                        outputStream.writeUTF("LOGOUT");
                        outputStream.flush();

                        System.out.println("Sto facendo un logou");
                        outputStream.writeUTF(sessID);
                        outputStream.flush();

                        break;

                    default:
                        System.out.println("No command used");
                        break;
                }

            } catch (IOException se) {
                se.printStackTrace();
            } finally {

                if (socket != null)
                    try {
                        if(inputStream!=null) inputStream.close();
                        if(outputStream!=null) outputStream.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        } catch (Exception ignored) {

        }
        //logout();
    }


    public static void logout() {
        String nomeHost = null;
        try {
            nomeHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Socket socket = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            socket = new Socket(nomeHost, 8189);
            System.out.println("Ho aperto il socket verso il server");
            in = new ObjectInputStream(socket.getInputStream());

            out = new ObjectOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            System.out.println("logoU");
            out.writeUTF("LOGOUT");
            out.flush();
            out.writeUTF(sessID);
            out.flush();
        }
        catch (Exception ignored) {}

    }
}
