package server;

import security.Asymmetric;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Vector;
import java.util.stream.Stream;


public class Server {
    private int port;
    private volatile boolean running;
    private Asymmetric asymmetric;
    private KeyBoardInput keyBoardInput;
    private NetworkInput networkInput;
    private ServerSocket serverSocket;
    private Vector<Client> clients;
    private Vector<Group> groups;


    public Server() {
        running = true;
        clients = new Vector<>();
        groups = new Vector<>();
        keyBoardInput = new KeyBoardInput("server keyboard", this);
        keyBoardInput.start();
    }

    /**
     * method used to set port to start listing
     *
     * @param port int port number
     * @return boolean true if success
     */
    boolean setPort(String port) throws IOException, NumberFormatException {
        if (serverSocket != null)
            return false;

        this.port = Integer.parseInt(port);
        serverSocket = new ServerSocket(this.port);
        networkInput = new NetworkInput("networkInput server", this);
        networkInput.start();

        return true;
    }


    /**
     * method used to generate public key and private key
     *
     * @throws NoSuchAlgorithmException occur
     */
    void generatePair() throws NoSuchAlgorithmException {
        asymmetric = new Asymmetric(1024);
    }

    /**
     * getter public key
     *
     * @return string public key
     */
    PublicKey getPublicKey() {
        return asymmetric.getPublicKey();
    }

    byte[] decrypt(byte[] data) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        return asymmetric.decrypt(data);
    }

    /**
     * getter private key
     *
     * @return private key
     */
    PrivateKey getPrivateKey() {
        return asymmetric.getPrivateKey();
    }

    /**
     * method used to check server status
     *
     * @return boolean if true
     */
    synchronized boolean isRunning() {
        return running;
    }

    /**
     * method used to check the status of network on the server
     *
     * @return boolean if true network is started
     */
    synchronized boolean isNetworkDown() {
        return networkInput == null;
    }


    /**
     * this method used to shutdown network thread if it's running and the keyboard thread.
     * after shutdown the network and keyboard threads the main thread turn off automatically
     *
     * @throws IOException occur
     */
    synchronized void shutdown() throws IOException {
        running = false;

        if (networkInput != null) {
            disconnectAllClients();
            PrintWriter output = new PrintWriter(new Socket(InetAddress.getLocalHost(), port).getOutputStream(), true);
            output.println("");
            output.close();
        }


    }

    synchronized ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * method used to add new user
     *
     * @param c Client to be added
     */
    synchronized void addClient(Client c) {
        for (Client client : clients)
            client.send("server say:new user online!");

        clients.add(c);
    }

    /**
     * remove specific user.you need to identify if to need remove theme using keyboard or network
     *
     * @param c            Client to be deleted
     * @param fromKeyboard boolean if true disconnect using keyboard
     */
    synchronized void removeClient(Client c, boolean fromKeyboard) {
        if (fromKeyboard)
            c.disconnectFromKeyboard();
        c.disconnect();
        clients.remove(c);

        for (Group group : groups)
            group.removeClient(c);

        broadcast("(pc" + c.getId() + ")" + c.getHostName() + " back offline.");
    }

    /**
     * method used to disconnect all clients connected to server
     * it's used when server need to reset or shutdown
     */
    private void disconnectAllClients() {
        for (Client client : clients) client.disconnect();
        clients = new Vector<>();
    }

    /**
     * method used to get number of clients connected
     *
     * @return int number of clients connected
     */
    synchronized int getNumberOfClients() {
        return clients.size();
    }

    /**
     * method used to get specific user using id
     *
     * @param id int id of user
     * @return Client founded or null if not found
     */
    synchronized Client getClientById(int id) {
        for (Client client : clients)
            if (client.getId() == id)
                return client;
        return null;
    }

    /**
     * method list all clients connected to the server
     *
     * @return String contain all clients with styling
     */
    synchronized String displayClients() {
        if (clients.size() == 0)
            return "sorry no user connected to this server";

        StringBuilder out = new StringBuilder();
        StringBuilder separate = new StringBuilder();

        out.append("\nonline clients\n");
        Stream.generate(() -> "=")
                .limit(15)
                .forEach(separate::append);

        out.append(String.format("%-23s", separate.toString())).append("\n").append("\n");

        separate.setLength(0);


        out.append(String.format("%-15s%-35s%-35s%-15s", "id", "hostname", "address", "port")).append("\n");

        out.append(insertHeaderStyle());


        for (Client client : clients)
            out.append(client.toString()).append("\n");


        return out.toString();
    }

    /**
     * fill header style
     *
     * @return String header
     */
    private String insertHeaderStyle() {
        StringBuilder out = new StringBuilder();
        StringBuilder separate = new StringBuilder();

        Stream.generate(() -> "-")
                .limit(5)
                .forEach(separate::append);
        out.append(String.format("%-15s", separate.toString()));
        separate.setLength(0);

        Stream.generate(() -> "-")
                .limit(15)
                .forEach(separate::append);
        out.append(String.format("%-35s", separate.toString()));
        separate.setLength(0);

        Stream.generate(() -> "-")
                .limit(15)
                .forEach(separate::append);
        out.append(String.format("%-35s", separate.toString()));
        separate.setLength(0);

        Stream.generate(() -> "-")
                .limit(5)
                .forEach(separate::append);
        out.append(separate.toString()).append("\n");
        separate.setLength(0);
        return out.toString();
    }

    /**
     * method used to broadcast message foreach user connected to the server
     *
     * @param message String contain of message
     */
    private void broadcast(String message) {
        for (Client client : clients) {
            client.send("server say:" + message);
        }
    }

    /**
     * method used to add new group
     *
     * @param grp Group to be added
     * @return boolean true if added successful
     */
    boolean addGroup(Group grp) {
        return groups.add(grp);
    }

    /**
     * method used to delete group
     *
     * @param client    owner of group if null delete from server
     * @param groupName String name of group
     * @return true if deleted successful
     */
    boolean removeGroup(Client client, String groupName, boolean fromServer) {
        Group group = getGroup(groupName);
        if (group != null) {
            if (client == null || group.isAdministrator(client)) {
                group.destroy(fromServer);
                groups.remove(group);
                return true;

            } else return false;
        } else
            return false;
    }

    /**
     * method to get specific group
     *
     * @param groupName String name of group
     * @return if founded return group else return null
     */
    Group getGroup(String groupName) {
        Group out = null;
        for (Group group : groups) {
            if (group.getName().equals(groupName)) {
                out = group;
                break;
            }
        }
        return out;
    }

    Group getGroupById(int groupId) {
        Group out = null;
        for (Group group : groups) {
            if (group.getId() == groupId) {
                out = group;
                break;
            }
        }
        return out;
    }

    /**
     * check if group exist in the server
     *
     * @param groupId int group id
     * @return true if exist
     */
    boolean groupIsExist(int groupId) {
        for (Group group : groups)
            if (group.getId() == groupId)
                return true;
        return false;
    }

    /**
     * method used to join client on specific group
     *
     * @param client    Client to be added
     * @param groupName String name of group
     */
    boolean joinGroup(Client client, String groupName) {
        Group group = getGroup(groupName);
        if (group != null) {
            return group.addClient(client);
        } else
            return false;

    }

    /**
     * method This method is used to exit the client from the group
     *
     * @param client    to be removed from group
     * @param groupName String name of group
     * @return true if successful exit group
     */
    boolean exitGroup(Client client, String groupName) {
        Group group = getGroup(groupName);
        if (group != null) {
            group.removeClient(client);
            return true;
        }
        return false;
    }

    /**
     * method used to check if group exist before add new group
     *
     * @param name String name of group
     * @return true if exist same name
     */
    boolean containsGroup(String name) {
        for (Group group : groups)
            if (group.getName().equals(name))
                return true;
        return false;
    }

    /**
     * method used when a problem occur in connection between client and server
     *
     * @param client to be removed
     */
    void destroyClient(Client client) {
        for (Group group : groups) {
            if (group.isAdministrator(client))
                group.destroy(true);
            else if (group.isMember(client))
                group.removeClient(client);
        }
    }

    /**
     * method to display all groups
     *
     * @return String contain all groups
     */
    String displayGroups(Client beneficiary) {
        if (groups.size() == 0)
            return "sorry no group created on this server";

        StringBuilder out = new StringBuilder();
        StringBuilder separate = new StringBuilder();

        out.append("\ngroups\n");
        Stream.generate(() -> "=")
                .limit(10)
                .forEach(separate::append);

        out.append(String.format("%-23s", separate.toString())).append("\n").append("\n");

        separate.setLength(0);


        out.append(String.format("%-15s%-35s%-35s%-15s", "id", "group name", "administrator", "joined")).append("\n");
        out.append(insertHeaderStyle());

        for (Group group : groups)
            out.append(group.toString(beneficiary)).append("\n");

        return out.toString();
    }
}
