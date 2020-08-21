package org.key_project.machlearning;

import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.proof.Goal;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.Socket;

public class PythonConnection {

    private static final int PORT = 6767;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public void connect() throws IOException {
        socket = new Socket("localhost", PORT);
        writer = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void close() throws IOException {
        writer.close();
        socket.close();
    }

    public Tactic queryTactic(Goal goal) throws IOException, InterruptedException {

        if(Thread.interrupted())
            throw new InterruptedException();

        Sequent sequent = goal.sequent();

        JSONObject result = new JSONObject();
        result.put("id", goal.node().serialNr());
        result.put("antecedent", KeYConnection.semiSequentToJSON(sequent.antecedent()));
        result.put("succedent", KeYConnection.semiSequentToJSON(sequent.succedent()));
        String query = result.toJSONString();

        writer.println(query);
        writer.flush();

        String tacticString = reader.readLine();
        if (tacticString.equals("-NONE-")) {
            return null;
        }

        Tactic tactic = KeYConnection.TACTICS.get(tacticString);

        if (tactic == null) {
            System.err.println("Known tactics are: " + KeYConnection.TACTICS.keySet());
            throw new IOException("Unknown tactic " + tacticString);
        }

        return tactic;
    }
}
