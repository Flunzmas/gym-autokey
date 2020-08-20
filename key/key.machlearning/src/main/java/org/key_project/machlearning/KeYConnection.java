package org.key_project.machlearning;

import de.uka.ilkd.key.gui.MainWindow;
import de.uka.ilkd.key.logic.Semisequent;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.SequentFormula;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.label.TermLabel;
import de.uka.ilkd.key.logic.op.Operator;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.NotationInfo;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofEvent;
import de.uka.ilkd.key.ui.AbstractMediatorUserInterfaceControl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.key_project.util.collection.ImmutableArray;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class KeYConnection {

    static interface Handler {
        JSONObject handle(JSONObject json) throws Exception;
    }

    private final AbstractMediatorUserInterfaceControl uiCtrl;
    private Proof ongoingProof;

    private Map<String, Handler> commands = new HashMap<>();
    {
        commands.put("load", this::loadFile);
        commands.put("execute", this::executeTactic);
        commands.put("rewind", this::rewind);
        commands.put("features", this::extractFeatures);
        commands.put("ast", this::getAST);
        commands.put("tactics", this::listTactics);
        commands.put("set", this::setProperty);
        commands.put("quit", c -> { System.exit(0); return null; });
        commands.put("print", this::printSequent);
        commands.put("open", this::openGoals);
    }

    public static Map<String, Tactic> TACTICS = new HashMap<>();

    static {
        TACTICS.put("AUTO", new AutoTactic(""));
        TACTICS.put("AUTO_NOSPLIT", new AutoTactic("[StrategyProperty]SPLITTING_OPTIONS_KEY=SPLITTING_OFF"));
        TACTICS.put("MODELSEARCH", new ModelSearchTactic());
        TACTICS.put("NOTHING", (a, b, c, d) -> {});
        TACTICS.put("SMT", new SMTTactic());
        FilterTactic.registerTactics(TACTICS);
    }
    public KeYConnection(int verbosity, boolean interactive) {
        if (interactive) {
            uiCtrl = MainWindow.getInstance(true).getUserInterface();
        } else {
            uiCtrl = new MachLearningUICtrl((byte) verbosity);
        }
    }

    public JSONObject handle(JSONObject jsonCommand) throws Exception {
        String command = Objects.toString(jsonCommand.get("command"));
        if (command == null) {
            throw new IllegalArgumentException("No command set!");
        }

        if (ongoingProof == null && !"load".equals(command) && !"quit".equals(command) &&
                !"tactics".equals(command)) {
            throw new IllegalStateException("Cannot do anything until a problem has been loaded.");
        }

        Handler handler = commands.get(command);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown command: " + command);
        }

        return handler.handle(jsonCommand);
    }

    private JSONObject executeTactic(JSONObject jsonObject) {

        int id;
        try {
            id = Integer.parseInt(jsonObject.get("id").toString());
        } catch(Exception ex) {
            throw new IllegalArgumentException("Missing/Illegal id parameter", ex);
        }

        Goal goal = findGoal(id);

        String tacName = Objects.toString(jsonObject.get("tactic"));
        Tactic tactic = TACTICS.get(tacName);
        if (tactic == null) {
            throw new IllegalArgumentException("Unknown/Missing tactic " + tacName);
        }

        uiCtrl.getProofControl().fireAutoModeStarted(new ProofEvent(ongoingProof));
        try {
            tactic.apply(uiCtrl, ongoingProof, goal, jsonObject);
        } catch (Exception e) {
            return Server.error(e);
        } finally {
            uiCtrl.getProofControl().fireAutoModeStopped(new ProofEvent(ongoingProof));
        }

        /*
        final GoalChooser goalChooser = ongoingProof.getInitConfig().getProfile().getSelectedGoalChooserBuilder().create();
        final ProverCore applyStrategy = new ApplyStrategy(goalChooser);

        applyStrategy.start(ongoingProof, goal);
        */

        return Server.success("ids", filterGoalIds(id));
    }

    private Goal findGoal(int id) {
        for (Goal openGoal : ongoingProof.openGoals()) {
            if (openGoal.node().serialNr() == id) {
                return openGoal;
            }
        }
        throw new NoSuchElementException("Unknown id " + id + " among the goals.");
    }

    private JSONObject openGoals(JSONObject jsonObject) {
        return Server.success("ids", filterGoalIds(1));
    }

    private Node findNode(int id) {
        // Recursion may not been an option due to stack overflow
        Deque<Node> todo = new LinkedList<>();
        todo.push(ongoingProof.root());
        while(!todo.isEmpty()) {
            Node node = todo.pop();
            if (node.serialNr() == id) {
                return node;
            }
            Iterator<Node> it = node.childrenIterator();
            while (it.hasNext()) {
                todo.push(it.next());
            }
        }
        throw new NoSuchElementException("Unknown id " + id + " among the nodes.");
    }

    private JSONObject loadFile(JSONObject obj) throws InterruptedException {
        String filename = Objects.toString(obj.get("filename"));
        System.err.println("Loading: " + filename);

        if (ongoingProof != null) {
            ongoingProof.dispose();
        }

        // The window-bases ui ctrl does this asynchronously.
        // So better wait for it to finish ...
        LoadFinishListener listener = new LoadFinishListener();
        uiCtrl.addProverTaskListener(listener);
        uiCtrl.loadProblem(new File(filename));
        listener.waitFinished();
        uiCtrl.removeProverTaskListener(listener);

        ongoingProof = uiCtrl.getMediator().getSelectedProof();
        while (ongoingProof == null) {
            // TODO Limit this to n iterations
            System.err.println("Wait for (potentially asynchronous) load ...");
            Thread.sleep(400);
            ongoingProof = uiCtrl.getMediator().getSelectedProof();
        }

        return Server.success("ids", filterGoalIds(1));
    }


    // find all goal ids below a given serial number
    private JSONArray filterGoalIds(int below) {
        JSONArray ids = new JSONArray();
        for (Goal goal : ongoingProof.openGoals()) {
            if(below == 1 || isBelow(goal.node(), below))
                ids.add(goal.node().serialNr());
        }
        return ids;
    }

    private static boolean isBelow(Node node, int below) {
        while (node != null) {
            if (node.serialNr() == below) {
                return true;
            }
            node = node.parent();
        }
        return false;
    }

    private JSONObject extractFeatures(JSONObject jsonObject) {
        int id;
        try {
            id = Integer.parseInt(jsonObject.get("id").toString());
        } catch(Exception ex) {
            throw new IllegalArgumentException("Missing/Illegal id parameter", ex);
        }

        Goal goal = findGoal(id);

        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("numberOfFormulas",
                goal.node().sequent().succedent().size() + goal.node().sequent().antecedent().size());
        // ...
        // probably in a class of its own.

        return result;
    }

    private JSONObject rewind(JSONObject jsonObject) {
        int id;
        try {
            id = Integer.parseInt(jsonObject.get("id").toString());
        } catch(Exception ex) {
            throw new IllegalArgumentException("Missing/Illegal id parameter", ex);
        }

        Node node = findNode(id);

        ongoingProof.pruneProof(node);

        return Server.success();
    }

    private JSONObject listTactics(JSONObject jsonObject) {
        JSONArray array = new JSONArray();
        array.addAll(TACTICS.keySet());
        return Server.success("tactics", array);
    }

    private JSONObject getAST(JSONObject jsonObject) {

        int id;
        try {
            id = Integer.parseInt(jsonObject.get("id").toString());
        } catch(Exception ex) {
            throw new IllegalArgumentException("Missing/Illegal id parameter", ex);
        }

        Goal goal = findGoal(id);
        Sequent sequent = goal.sequent();

        JSONObject result = new JSONObject();
        result.put("response", "success");
        result.put("id", id);
        result.put("antecedent", semiSequentToJSON(sequent.antecedent()));
        result.put("succedent", semiSequentToJSON(sequent.succedent()));
        return result;
    }

    private JSONObject printSequent(JSONObject jsonObject) {
        int id;
        try {
            id = Integer.parseInt(jsonObject.get("id").toString());
        } catch(Exception ex) {
            throw new IllegalArgumentException("Missing/Illegal id parameter", ex);
        }

        Goal goal = findGoal(id);
        Sequent sequent = goal.sequent();

        NotationInfo ni = new NotationInfo();
        LogicPrinter p = new LogicPrinter(new ProgramPrinter(null), ni,
                null, true) {
            @Override
            protected ImmutableArray<TermLabel> getVisibleTermLabels(Term t) {
                return new ImmutableArray<>();
            }
        };

        p.setLineWidth(120);
        p.printSequent(sequent);
        String printed = p.result().toString();

        JSONObject result = new JSONObject();
        result.put("response", "success");
        result.put("id", id);
        result.put("sequent", printed);
        return result;

    }


    public static JSONArray semiSequentToJSON(Semisequent semiseq) {
        JSONArray result = new JSONArray();
        Iterator<SequentFormula> it = semiseq.iterator();
        while(it.hasNext()) {
            result.add(termToJSON(it.next().formula()));
        }
        return result;
    }

    public static JSONObject termToJSON(Term term) {
        Operator op = term.op();
        JSONObject result = new JSONObject();
        result.put("op", op.name().toString());
        result.put("op_class", op.getClass().toString());
        JSONArray children = new JSONArray();
        for (Term sub : term.subs()) {
            children.add(termToJSON(sub));
        }
        result.put("children", children);
        return result;
    }

    private JSONObject setProperty(JSONObject command) {

        Object name = command.get("property");
        if (name == null) {
            throw new IllegalArgumentException("Missing 'property' argument");
        }

        Object value = command.get("value");
        if (value == null) {
            throw new IllegalArgumentException("Missing 'value' argument");
        }

        try {
            PropertyDescriptor pd = new PropertyDescriptor(name.toString(), getClass());
            pd.getWriteMethod().invoke(this, value);
        } catch (Exception e) {
            throw new RuntimeException("Error while setting property", e);
        }

        return Server.success();
    }


    public void setMaxSteps(String steps) {
        if (ongoingProof != null) {
            ongoingProof.getSettings()
                    .getStrategySettings().setMaxSteps(Integer.parseInt(steps));
        } else {
            throw new IllegalStateException("No proof loaded");
        }
    }

    public String getMaxSteps() {
        return null;
    }

    public void setTimeout(String timeout) {
        if (ongoingProof != null) {
            ongoingProof.getSettings()
                    .getStrategySettings().setTimeout(Long.parseLong(timeout));
        } else {
            throw new IllegalStateException("No proof loaded");
        }
    }

    public String getTimeout() {
        return null;
    }

}
