package Parser;

import BoolNets.BooleanNetwork;
import BoolNets.NetworkCollection;
import BoolNets.VertexFunction;
import Sound.NetTrack;
import Utilities.IO;
import Utilities.Pair;
import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Position;
import Sound.SoundMaker;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;

public class Interpreter {

    ArrayList<String> visitedFiles = new ArrayList<>();
    Stack<CodeType> codeTypeStack = new Stack<CodeType>();

    public void clear() {
        visitedFiles.clear();
    }

    public void parseAndRun(String fileLocation, SoundMaker soundMaker, NetworkCollection netCollection) throws Exception {

        //Run the parser on the given inputs
        String input = IO.readStringFromFile(fileLocation);
        File file = new File(fileLocation);
        String fileName = file.getName();
        String fileDirectory = file.getParent();

        if (input != null) {
            Parser parser = Parboiled.createParser(Parser.class);
            ReportingParseRunner rpr = new ReportingParseRunner(parser.InputLine());
            ParsingResult<?> result = rpr.run(input);


            //Print any parser Errors
            if (!result.parseErrors.isEmpty()) {
                System.out.println(ErrorUtils.printParseError(result.parseErrors.get(0)));
                int start = result.parseErrors.get(0).getStartIndex();
                Position pos = result.parseErrors.get(0).getInputBuffer().getPosition(start);
                showError(fileName, pos.line, pos.column);
            } else {
                System.out.println(printNodeTree(result) + '\n');

                //Reorder the stack of parsing results.
                //TODO Find a better way of ordering/interpreting the parse results.
                Stack interpreterStack = new Stack();

                while (!result.valueStack.isEmpty()) {
                    interpreterStack.push(result.valueStack.pop());
                }

                //Run the interpreter on each parsed result
                while (!interpreterStack.isEmpty()) {
                    Object value = interpreterStack.pop();
                    interpret(value, soundMaker, netCollection, fileDirectory, fileName);

                }

            }
        }
    }

    /**
     * Evaluate the semantics of a "parse tree" node, running the appropriate Java code as required.
     *
     * @param value
     * @param nets
     * @throws Exception
     */
    private void interpret(Object value, SoundMaker soundMaker, NetworkCollection nets, String currentFileDirectory, String currentFileName) throws Exception {

        if (value instanceof FileDeclaration_TreeNode) {
            CodeType ct = ((FileDeclaration_TreeNode) value).getCodeType();
            codeTypeStack.push(ct);
            return;
        }

        switch (codeTypeStack.peek()) {
            case Sequence:
                interpretSequenceCode(value, soundMaker, nets, currentFileDirectory);
                break;
            case Network:
                interpretBooleanNetworkCode(value, nets, currentFileName);
                break;
        }


    }

    private void interpretSequenceCode(Object value, SoundMaker soundMaker, NetworkCollection netCollection, String filesDirectory) throws Exception {
        if (value instanceof Import_TreeNode) {
            String fileName = ((Import_TreeNode) value).getFileName();
            if (!visitedFiles.contains(fileName)) {
                visitedFiles.add(fileName);
                parseAndRun(filesDirectory + File.separator + fileName, soundMaker, netCollection);
                codeTypeStack.pop();
            }
        } else if (value instanceof TrackDefinition_TreeNode) {
            String netName = ((TrackDefinition_TreeNode) value).getNetFileName();
            String trackName = ((TrackDefinition_TreeNode) value).getTrackName();
            ArrayList<TrackPlayType> playTypes = ((TrackDefinition_TreeNode) value).getPlayTypes();
            ArrayList<String> startStates = ((TrackDefinition_TreeNode) value).getStartStates();
            ArrayList<Integer> durations = ((TrackDefinition_TreeNode) value).getDurations();

            netCollection.addTrack(trackName, netName);

            for (int i = 0; i < playTypes.size(); i++) {
                TrackPlayType playType = playTypes.get(i);
                switch (playType) {
                    case PLAY:
                        netCollection.getNetworkSequencer().getTrackByName(trackName).addSequenceElement(startStates.get(i), durations.get(i));
                        break;
                    case REST:
                        netCollection.getNetworkSequencer().getTrackByName(trackName).addSequenceElement(null, durations.get(i));
                }
            }
        } else if (value instanceof Command_TreeNode) {
            switch (((Command_TreeNode) value).getCommandType()) {
                case PLAY_TRACKS:
                    ArrayList<String> tracks = ((Command_TreeNode) value).getTracks();
                    netCollection.getNetworkSequencer().flagPlayTracks(tracks);
                    break;
                default:
                    throw new Exception("Invalid command in sequence file.");

            }
        }
    }

    private void interpretBooleanNetworkCode(Object value, NetworkCollection networkCollection, String currentFileName) throws Exception {

        BooleanNetwork net = networkCollection.getNetwork(currentFileName);

        if (value instanceof BooleanNode_Definition_TreeNode) {
            String label = ((BooleanNode_Definition_TreeNode) value).getNodeLabel();
            net.addNode(false, null, label);
            //net.printConnections();
        } else if (value instanceof BooleanNode_Note_TreeNode) {
            String nodeLabel = ((BooleanNode_Note_TreeNode) value).getNodeLabel();
            String nodeNote = ((BooleanNode_Note_TreeNode) value).getNoteName();
            net.setNoteForNamedNode(nodeLabel, nodeNote);
        } else if (value instanceof VertexFunction_TreeNode) {
            String name = ((VertexFunction_TreeNode) value).getVertexFunctionName();
            String[] argumentNames = (String[]) ((VertexFunction_TreeNode) value).getArgumentNames().toArray(new String[]{});
            Boolean defaultOut = ((VertexFunction_TreeNode) value).getDefaultOutput();

            ArrayList<ArrayList<Boolean>> ruleIns = ((VertexFunction_TreeNode) value).getFunctionRuleInputs();
            if (ruleIns == null) {
                ruleIns = new ArrayList<>();
            }

            ArrayList<Boolean[]> ruleIns2 = new ArrayList<>();
            for (int i = 0; i < ruleIns.size(); i++) {
                ruleIns2.add(ruleIns.get(i).toArray(new Boolean[]{}));
            }

            ArrayList<Boolean> ruleOut = ((VertexFunction_TreeNode) value).getFunctionRuleOutputs();
            VertexFunction vf = net.addVertexFunction(argumentNames.length, name, argumentNames, defaultOut);

            Pair<ArrayList<Boolean[]>, ArrayList<Boolean>> expandedData = vf.expandLazyRules(ruleIns2, ruleOut);
            vf.setRules(expandedData.a, expandedData.b);

        } else if (value instanceof BooleanNode_FunctionAssignment_TreeNode) {
            String nodeLabel = ((BooleanNode_FunctionAssignment_TreeNode) value).getNodeLabel();
            String functionLabel = ((BooleanNode_FunctionAssignment_TreeNode) value).getFunctionLabel();
            ArrayList<String> inputNodeLabels = ((BooleanNode_FunctionAssignment_TreeNode) value).getInputNodeLabels();
            net.setNamedNodeVertexFunction(nodeLabel, functionLabel, inputNodeLabels);
        } else if (value instanceof State_TreeNode) {
            String name = ((State_TreeNode) value).getStateName();
            ArrayList<String> nodeLabels = ((State_TreeNode) value).getNodeLabels();
            boolean stateSetting = ((State_TreeNode) value).getStateSetting();


            net.addStartState(name, nodeLabels, stateSetting);
        } else if (value instanceof Command_TreeNode) {
            switch (((Command_TreeNode) value).getCommandType()) {
                case INSTRUMENT:
                    net.setInstrumentIndex((((Command_TreeNode) value).getInstrumentIndex()));
                    break;
                case PLAY_SINGLE_NET:
                    net.setStartState(((Command_TreeNode) value).getStateName());
                    net.playFor(((Command_TreeNode) value).getBufferLength());

                    //TimeUnit.SECONDS.sleep(10);
                    break;
                case PLAY_TRACKS:
                    throw new Exception("Invalid command 'play tracks' in network file '" + currentFileName + "'");

            }
        } else {
            throw new Exception("Unrecognised code. Class:" + value.getClass());
        }
    }

    public void showError(String fileName, int startIndex, int endIndex) {
        String errorText = "Code error in file '" + fileName + "' at line " + Integer.toString(startIndex) + ", column " + Integer.toString(endIndex) + ".";
        DialogMaker.showErrorDialog("Parsing Error", errorText);
    }

}
