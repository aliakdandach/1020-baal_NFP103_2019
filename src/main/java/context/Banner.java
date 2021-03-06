package context;

import command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * this class is non functionality it's just for styling
 */
public class Banner {
    private static char FUNCTIONALITY_SEPARATOR = '=';
    private static char HEADER_SEPARATOR = '-';

    /**
     * this method to read file(banner.txt) and print the output on the terminal.It's used at startup
     */
    public static void loadBanner() {
        InputStream fis = Banner.class.getClassLoader().getResourceAsStream("banner.txt");
        byte[] data = new byte[0];
        try {
            assert fis != null;
            data = new byte[fis.available()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.read(data);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String banner;
        banner = new String(data, StandardCharsets.UTF_8);
        System.out.println(banner);
    }


    /**
     * this method used to print the help message.it's take side as parameter and print help message correspond (server or user)
     *
     * @param side (server,user)
     */
    public static void adjustHelpMessage(String side) {
        int commandLength = 15;
        int descriptionLength = 80;
        //print core commands
        System.out.println(insertModule("core", commandLength, descriptionLength, side));
        //print chat commands
        if (side.equals("client"))
            System.out.println(insertModule("chat", commandLength, descriptionLength, side));
        //print group commands
        System.out.println(insertModule("group", commandLength, descriptionLength, side));
        //print file commands
        if (side.equals("client"))
            System.out.println(insertModule("file", commandLength, descriptionLength, side));
    }

    private static String insertModule(String functionality, int commandLength, int descriptionLength, String side) {

        return "\n" +
                insertFunctionality(functionality) +
                insertHeader(commandLength, descriptionLength) +
                insertDetails(Objects.requireNonNull(Command.byFunctionality(functionality)), side);
    }

    /**
     * method to print header of functionality
     *
     * @param functionality String used to identify
     * @return String functionality header
     */
    private static String insertFunctionality(String functionality) {
        StringBuilder out = new StringBuilder();

        out.append(String.format("%-7s", functionality));
        out.append("commands");
        out.append("\n");

        Stream.generate(() -> FUNCTIONALITY_SEPARATOR)
                .limit(15)
                .forEach(out::append);

        out.append("\n");
        out.append("\n");

        return out.toString();
    }

    /**
     * method used to print header commands and there description
     *
     * @param commandLength     int number of '-' under command word
     * @param descriptionLength int number of '-' under description word
     * @return String header
     */
    private static String insertHeader(int commandLength, int descriptionLength) {
        StringBuilder out = new StringBuilder();
        StringBuilder separate = new StringBuilder();

        out.append(String.format("%-23s", "Command"));
        out.append("Description");
        out.append("\n");

        Stream.generate(() -> HEADER_SEPARATOR)
                .limit(commandLength)
                .forEach(separate::append);

        out.append(String.format("%-23s", separate.toString()));

        separate.setLength(0);

        Stream.generate(() -> HEADER_SEPARATOR)
                .limit(descriptionLength)
                .forEach(separate::append);
        out.append(separate.toString());

        out.append("\n");
        return out.toString();
    }

    /**
     * method used to print details foreach command
     *
     * @param commands array of Commands to be printed
     * @param side     String server or user
     * @return String of details
     */
    private static String insertDetails(Command[] commands, String side) {
        StringBuilder out = new StringBuilder();

        for (Command command : commands)
            if (command.getSide().equals(side) || command.getSide().equals("both")) {
                out.append(String.format("%-23s", command.getCommand()));
                out.append(command.getDescription());
                out.append("\n");
            }
        out.append("\n");
        return out.toString();
    }
}
