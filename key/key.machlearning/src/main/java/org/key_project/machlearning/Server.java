package org.key_project.machlearning;

import de.uka.ilkd.key.settings.GeneralSettings;
import de.uka.ilkd.key.ui.Verbosity;
import de.uka.ilkd.key.util.CommandLine;
import de.uka.ilkd.key.util.CommandLineException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;

public class Server {

    private static final String PORT_PARAM = "--port";
    private static final String HELP_PARAM = "--help";
    private static final String VERBOSITY_PARAM = "--verbosity";
    private static final String SCREEN_PARAM = "--screen";

    private static final int DEFAULT_PORT = 5533;
    private static final JSONObject SUCCESS =
            new JSONObject(Collections.singletonMap("response", "success"));

    private static KeYConnection keyConnection;

    public static void main(String[] args) throws CommandLineException, IOException {
        CommandLine cl = new CommandLine();
        cl.addOption(PORT_PARAM, "port", "Listen to this port");
        cl.addOption(HELP_PARAM, null, "Print help");
        cl.addOption(VERBOSITY_PARAM, "level", "Detail level of output (0..4)");
        cl.addOption(SCREEN_PARAM, null, "Show KeY window");
        cl.parse(args);

        int verbosity = cl.getInteger(VERBOSITY_PARAM, Verbosity.HIGH);
        keyConnection = new KeYConnection(verbosity, cl.isSet(SCREEN_PARAM));

        if (cl.isSet(HELP_PARAM)) {
            cl.printUsage(System.out);
            return;
        }

        int port = cl.getInteger(PORT_PARAM, DEFAULT_PORT);

        listenTo(port);
    }

    private static void listenTo(int port) throws IOException {

        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            System.err.println("... Waiting for connection on port " + port);
            Socket socket = serverSocket.accept();
            try {
                System.err.println("... Connected to " + socket.toString());
                command(socket);
            } catch(Exception ex) {
                System.err.println("!!! Error in connection.");
                ex.printStackTrace();
                String errorMesg = error(ex.getMessage()).toJSONString() + "\n";
                System.err.println(errorMesg);
                socket.getOutputStream().write(errorMesg.getBytes());
            } finally {
                socket.close();
                System.err.println("... Connection closed. You can reconnect.");
            }
        }
    }

    public static JSONObject error(String message) {
        JSONObject result = new JSONObject();
        result.put("response", "error");
        result.put("message", message);
        return result;
    }

    public static JSONObject error(Exception e) {
        e.printStackTrace();
        JSONObject result = error(e.getMessage());
        result.put("exception", e.getClass().toString());
        return result;
    }

    public static JSONObject success() {
        return SUCCESS;
    }

    public static JSONObject success(String key, Object value) {
        JSONObject result = new JSONObject();
        result.put("response", "success");
        result.put(key, value);
        return result;
    }

    private static void command(Socket socket) throws IOException, ParseException {
        Reader in = new InputStreamReader(socket.getInputStream());
        OutputStream out = socket.getOutputStream();

        JSONParser parser = new JSONParser();
        JSONObject commandObj = (JSONObject) parser.parse(in);

        JSONObject response;

        try {
            response = keyConnection.handle(commandObj);
        } catch(Exception ex) {
            response = error(ex);
        }

        out.write(response.toJSONString().getBytes());
        out.write('\n');
    }
}
