package server;

import command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkInput extends Thread {
    private Server server;

    public NetworkInput(String n, Server s) {
        super(n);
        server = s;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }


    @Override
    public void run() {
        Socket socket = null;
        PrintWriter output = null;
        BufferedReader input = null;
        String request;
        try {

            while (!server.isShutdown()) {
                //waite for new client
                socket = server.getServerSocket().accept();
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                request = input.readLine();

                //parse command
                Command cmd = Command.getCommand(request);

                if (cmd != null)
                    switch (cmd) {
                        case CONNECT:
                            OnlineClient c = new OnlineClient(server, socket);
                            server.addClient(c);
                            System.out.println("new client is connected.check it by typing " + Command.WHO.getcommand() + " command");
                            System.out.print("irc > ");
                            socket=null;
                            break;
                        case WHO:
                            break;
                        default:
                            server.getQueue().put("sorry only " + Command.CONNECT.getcommand() + " and " + Command.WHO.getcommand() + "commands working....");
                            break;
                    }
                else
                    server.getQueue().put("sorry only " + Command.CONNECT.getcommand() + " and " + Command.WHO.getcommand() + "commands working....");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("\nClosing connection…");
                if (socket != null)
                    socket.close();
                if (input != null)
                    input.close();
            } catch (IOException ioEx) {
                System.out.println("Unable to disconnect!");
                System.exit(1);
            }
        }
    }
}