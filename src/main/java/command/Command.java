package command;

import java.util.ArrayList;

public enum Command {
    CLIENTS("both", "core", "clients", "List all clients connected to server"),
    CONNECT("client", "core", "connect", "Connect to the server ex: connect -u root -p root -h 192.168.0.10:5555"),
    HELP("both", "core", "help", "Print a help message"),
    KILL("server", "core", "kill", "kill user ex: kill pc1"),
    QUIT("both", "core", "quit", "Exit application"),
    MY_ID("client", "core", "id", "get your id from the server"),
    START("server", "core", "start", "Configure a machine to listen on a specific port ex: start 5555"),
    CHAT_WITH_USER("client", "chat", "usr", "chat with another user ex: usr pc1 message"),
    CHAT_ON_GROUP("client", "group", "group", "chat with members group ex: group grp1 message"),
    CREATE_GROUP("client", "group", "create", "create group ex: create nameOfYourGroup"),
    DELETE_GROUP("both", "group", "delete", "delete group if you are admin of group ex: delete nameOfGroup"),
    EXIT_GROUP("client", "group", "exit", "exit group ex: exit nameOfGroup"),
    JOIN_GROUP("client", "group", "join", "join group ex: join nameOfGroup"),
    LIST_GROUPS("both", "group", "groups", "list all groups founded on the server"),
    MEMBERS_OF_GROUP("both", "group", "members", "list all members in groups ex: members nameOfGroup"),
    SEND_FILE("client", "file", "file_to", "send file ex: file_to pc1 pathOfYourFile or send_to grp1 pathOfYourFile"),
    PAIR("server", "core", "pair", "get public and private key, you must start the server to get them");

    private String side;
    private String functionality;
    private String command;
    private String description;

    Command(String side, String functionality, String command, String description) {
        this.side = side;
        this.functionality = functionality;
        this.command = command;
        this.description = description;
    }

    public static Command getCommand(String command) {
        Command[] commands = Command.values();
        for (Command value : commands) {
            if (value.getCommand().equals(command))
                return value;
        }
        return null;
    }

    public static Command[] byFunctionality(String type) {
        switch (type) {
            case "core":
                return core();
            case "chat":
                return chat();
            case "group":
                return group();
            case "file":
                return file();
            default:
                return null;
        }
    }

    public static Command[] core() {
        Command[] commands = Command.values();
        ArrayList<Command> out = new ArrayList<>();

        for (Command command : commands)
            if (command.functionality.equals("core"))
                out.add(command);
        return out.toArray(new Command[out.size()]);
    }

    public static Command[] chat() {
        Command[] commands = Command.values();
        ArrayList<Command> out = new ArrayList<>();

        for (Command command : commands)
            if (command.functionality.equals("chat"))
                out.add(command);
        return out.toArray(new Command[out.size()]);
    }

    public static Command[] group() {
        Command[] commands = Command.values();
        ArrayList<Command> out = new ArrayList<>();

        for (Command command : commands)
            if (command.functionality.equals("group"))
                out.add(command);
        return out.toArray(new Command[out.size()]);
    }

    public static Command[] file() {
        Command[] commands = Command.values();
        ArrayList<Command> out = new ArrayList<>();

        for (Command command : commands)
            if (command.functionality.equals("file"))
                out.add(command);
        return out.toArray(new Command[out.size()]);
    }

    public String getSide() {
        return side;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

}
