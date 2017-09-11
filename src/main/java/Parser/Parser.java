package Parser;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.support.StringVar;
import org.parboiled.support.Var;

import java.util.*;


@BuildParseTree
public class Parser extends BaseParser<Object> {

    private Set<String> encounteredNodeVariables = new HashSet<>();

    private static final Character TYPE_ATTACHER = ':';
    private static final Character EITHER_STATE_CHARACTER = '?';
    protected static final Set<String> VARIABLE_TYPES = new HashSet<>(Arrays.asList("node", "state"));


    public Rule InputLine() {
        return Sequence(Block(), EOI);
    }

    Rule Block() {
        return Sequence(FirstOf(SequenceFileDeclaration(),NetworkFileDeclaration()),ZeroOrMore(PossibleWhiteSpace(), ZeroOrMore(BlankLine()), PossibleWhiteSpace(), FirstOf(ImportNetwork(),TrackDefinition(), SetInstrumentCommand(), NodeDefinition(), StateDefinition(), NodeNoteAssignment(), FunctionDefinition(), NodeFunctionAssignment(), CommentLine()), PossibleWhiteSpace(), ZeroOrMore(BlankLine()), PossibleWhiteSpace()), FirstOf(PlayCommand(),PlayTracks(),EOI), PossibleWhiteSpace(), ZeroOrMore(BlankLine()));
    }


    //VARIABLES / DEFINITIONS

    /*Rule VariableDefinition(){
        StringVar typeName = new StringVar();
        return Sequence(VariableTypeKeyword(typeName), TypeAttacher(), DefiniteWhiteSpace(), StringWord());
    }

    Rule VariableTypeKeyword(StringVar typeName){
        return Sequence(OneOrMore(Alphanumeric()),typeName.isSet() && match().equals(typeName.get()) || typeName.isNotSet() && typeName.set(match()) && isVariableType(typeName.get()));
    }*/
    Rule CommentLine(){
        return Sequence(
                "//",
                ZeroOrMore(TestNot(Newline()), ANY),
                FirstOf(Newline(), EOI));
    }

    Rule StateDefinition() {
        Var<ArrayList<String>> stateName = new Var<>();
        Var<ArrayList<String>> nodeLabels = new Var<ArrayList<String>>();
        Var<Boolean> stateValue = new Var<Boolean>();
        return Sequence("state", TypeAttacher(), PossibleWhiteSpace(), StateVariableName(stateName), DefiniteWhiteSpace(), StateDataAssignment(nodeLabels, stateValue), PossibleWhiteSpace(),
                push(new State_TreeNode(stateName.get().get(0), nodeLabels.get(), stateValue.get())));
    }


    Rule NodeDefinition() {
        StringVar nodeLabel = new StringVar();
        return Sequence("node", TypeAttacher(), PossibleWhiteSpace(), nodeVariableName(nodeLabel),
                setNodeEncountered(nodeLabel.get()), push(new BooleanNode_Definition_TreeNode(nodeLabel.get())));
    }

    Rule NodeNoteAssignment() {
        StringVar nodeLabel = new StringVar();
        StringVar noteName = new StringVar();
        return Sequence(nodeVariableName(nodeLabel), DefiniteWhiteSpace(),"has note", DefiniteWhiteSpace(), NoteName(noteName), PossibleWhiteSpace(),
                push(new BooleanNode_Note_TreeNode(nodeLabel.get(), noteName.get())));
    }

    /**
     * Rule for assigning a function, with particular inputs, to a node
     * //TODO Allow nesting of functions within other functions
     * @return
     */
    Rule NodeFunctionAssignment() {
        StringVar nodeLabel = new StringVar();
        StringVar functionName = new StringVar();
        Var<ArrayList<String>> inputNodeLabels = new Var<>();
        return Sequence(nodeVariableName(nodeLabel), DefiniteWhiteSpace(), "has function", DefiniteWhiteSpace(), FunctionVariableName(functionName), Bracketed(List(ExistingNodeVariableName(inputNodeLabels))),
                push(new BooleanNode_FunctionAssignment_TreeNode(nodeLabel.get(), functionName.get(), inputNodeLabels.get())));
    }

    Rule SequenceFileDeclaration(){
        return Sequence(String("sequence file"), push(new FileDeclaration_TreeNode(CodeType.Sequence)));
    }

    Rule NetworkFileDeclaration(){
        return Sequence(String("network file"), push(new FileDeclaration_TreeNode(CodeType.Network)));
   }

    Rule PlayCommand() {
        Var<ArrayList<String>> stateName = new Var<>();
        Var<Integer> bufferLength = new Var<>();
        return Sequence("play", DefiniteWhiteSpace(), "from", DefiniteWhiteSpace(), StateVariableName(stateName), DefiniteWhiteSpace(), "for", DefiniteWhiteSpace(), NonZeroInteger(bufferLength),
                push(new Command_TreeNode(CommandType.PLAY_SINGLE_NET, stateName.get().get(0), bufferLength.get())));
    }

    Rule SetInstrumentCommand(){
        //Assumes that null-value is used for percussion instruments
        Var<Integer> instrumentIndex = new Var<>();
        return Sequence("set instrument to", DefiniteWhiteSpace(), FirstOf(PositiveInteger(instrumentIndex),String("percussion")),
                push(new Command_TreeNode(CommandType.INSTRUMENT,instrumentIndex.get())));
    }
    Rule FunctionDefinition() {
        StringVar functionName = new StringVar();
        Var<ArrayList<String>> argumentNames = new Var<ArrayList<String>>();
        Var<ArrayList<ArrayList<Boolean>>> ruleContent = new Var<>();
        Var<ArrayList<Boolean>> ruleOutputs = new Var<>();
        Var<Boolean> defaultRule = new Var<>();
        return Sequence("function", TypeAttacher(), PossibleWhiteSpace(), FunctionVariableName(functionName), FunctionArgumentNameDefinitions(argumentNames), DefiniteWhiteSpace(), "is", PossibleWhiteSpace(), Newline(), PossibleWhiteSpace(),
                MultilineRepeatedRule(FunctionRuleDefinition(ruleContent,ruleOutputs)),PossibleWhiteSpace(),
                FunctionRuleDefault(defaultRule), push(new VertexFunction_TreeNode(functionName.get(),argumentNames.get(),ruleContent.get(),ruleOutputs.get(),defaultRule.get())));
    }

    Rule ImportNetwork(){
        StringVar netFileName = new StringVar();
        return Sequence("import",DefiniteWhiteSpace(),FileName(netFileName),
                push(new Import_TreeNode(netFileName.get())));
    }

    Rule TrackDefinition() {
        Var<ArrayList<TrackPlayType>> playType = new Var<>();
        Var<ArrayList<String>> startStates = new Var<>();
        Var<ArrayList<Integer>> durations = new Var<>();

        StringVar trackName = new StringVar();
        StringVar netFileName = new StringVar();

        return Sequence("track",TypeAttacher(),PossibleWhiteSpace(),FunctionVariableName(trackName),DefiniteWhiteSpace(),"playing",DefiniteWhiteSpace(),FileName(netFileName),DefiniteWhiteSpace(),"is",PossibleWhiteSpace(),Newline(),PossibleWhiteSpace(),
                MultilineRepeatedRule(TrackDefinitionComponent(playType,startStates,durations)),
                        push(new TrackDefinition_TreeNode(trackName.get(), netFileName.get(), playType.get(),startStates.get(),durations.get())));
    }



    Rule StateDataAssignment(Var<ArrayList<String>> nodeLabels, Var<Boolean> stateValue) {
        return Sequence(AssignmentOperator(), DefiniteWhiteSpace(), SquareBracketed(ListWithSingleSeparateEnding(ExistingNodeVariableName(nodeLabels), State(stateValue))));
    }

    Rule FunctionArgumentNameDefinitions(Var<ArrayList<String>> names) {
        return Bracketed(List(FunctionArgumentVariableName(names)));
    }

    Rule FunctionVariableName(StringVar name) {
        return Sequence(VariableName(), name.set(match()));
    }

    Rule NetworkVariableName(StringVar name) {
        return Sequence(VariableName(), name.set(match()));
    }

    Rule FunctionArgumentVariableName(Var<ArrayList<String>> names) {
        return Sequence(VariableName(), names.set(extendStringList(names.get(), match())));
    }

    Rule FunctionDefinitionVariableName(Var<ArrayList<Boolean>> states) {
        return Sequence(AnyState(), states.set(extendBooleanList(states.get(), stateStringToBoolean(match()))));
    }

    Rule FunctionRuleDefinition(Var<ArrayList<ArrayList<Boolean>>> rules, Var<ArrayList<Boolean>> outState) {
        Var<ArrayList<Boolean>> rule = new Var<ArrayList<Boolean>>();
        return Sequence(List(FunctionDefinitionVariableName(rule)), PossibleWhiteSpace(), "-->",
                PossibleWhiteSpace(), States(outState), rules.set(extendBooleanListList(rules.get(),rule.get())));
    }

    Rule TrackDefinitionComponent(Var<ArrayList<TrackPlayType>> playType, Var<ArrayList<String>> startStates, Var<ArrayList<Integer>> durations) {
        return FirstOf(PlayNetInTrack(playType,startStates,durations), RestNetInTrack(playType,startStates,durations));
    }

    Rule PlayNetInTrack(Var<ArrayList<TrackPlayType>> playType, Var<ArrayList<String>> startStates, Var<ArrayList<Integer>> durations){
        return Sequence("play", DefiniteWhiteSpace(), "from", DefiniteWhiteSpace(), StateVariableName(startStates),
                DefiniteWhiteSpace(),"for",DefiniteWhiteSpace(),NonZeroIntegerExtend(durations),playType.set(extendTrackPlayTypeList(playType.get(),TrackPlayType.PLAY)));
    }

    Rule RestNetInTrack(Var<ArrayList<TrackPlayType>> playType, Var<ArrayList<String>> startStates, Var<ArrayList<Integer>> durations){
        return Sequence("rest", DefiniteWhiteSpace(),"for",
                DefiniteWhiteSpace(),NonZeroIntegerExtend(durations),startStates.set(extendStringList(startStates.get(),null)),playType.set(extendTrackPlayTypeList(playType.get(),TrackPlayType.REST)));
    }

    Rule PlayTracks(){
        Var<ArrayList<String>> trackNames = new Var<>();
        return Sequence("play tracks", DefiniteWhiteSpace(),SquareBracketed(List(StateVariableName(trackNames))),
                push(new Command_TreeNode(CommandType.PLAY_TRACKS,trackNames.get())));
    }

    Rule FunctionRuleDefault(Var<Boolean> defaultState) {
        return Sequence("default to", DefiniteWhiteSpace(), State(defaultState));
    }


    Rule nodeVariableName(StringVar nodeLabel) {
        return Sequence(VariableName(), nodeLabel.set(match()));
    }

    Rule ExistingNodeVariableName(Var<ArrayList<String>> nodeLabels) {
        return Sequence(VariableName(), nodeLabels.set(extendStringList(nodeLabels.get(), match())));
    }

    Rule StateVariableName(Var<ArrayList<String>> name) {
        return Sequence(VariableName(), name.set(extendStringList(name.get(),match())));
    }


    //BASIC DATA FORMATS

    Rule ListWithSingleSeparateEnding(Rule listRule, Rule finalRule) {
        return Sequence(listRule, PossibleWhiteSpace(), ZeroOrMore(Sequence(ListDelimiter(), PossibleWhiteSpace(), listRule, PossibleWhiteSpace())), ListPartSeparator(), PossibleWhiteSpace(), finalRule);
    }


    Rule List(Rule listRule) {
        return Sequence(listRule, PossibleWhiteSpace(), ZeroOrMore(Sequence(ListDelimiter(), PossibleWhiteSpace(), listRule, PossibleWhiteSpace())));
    }

    Rule MultilineRepeatedRule(Rule rule) {
        return ZeroOrMore(PossibleWhiteSpace(), rule, PossibleWhiteSpace(), Newline());
    }

    Rule SquareBracketed(Rule innerRule) {
        return Sequence("[", PossibleWhiteSpace(), innerRule, PossibleWhiteSpace(), "]");
    }

    Rule Bracketed(Rule innerRule) {
        return Sequence("(", PossibleWhiteSpace(), innerRule, PossibleWhiteSpace(), ")");
    }

    Rule NoteName(StringVar noteName) {
        return Sequence(Sequence(CharRange('A', 'G'), Optional(FirstOf('#', 'b')), CharRange('0', '8')), noteName.set(match()));
    }

    Rule StringWord() {
        return OneOrMore(Alphanumeric());
    }

    Rule StringLetters() {
        return OneOrMore(Letter());
    }

    @SuppressNode
    Rule Alphanumeric() {
        return FirstOf(Letter(), Digit());
    }

    @SuppressNode
    Rule VariableName() {
        return OneOrMore(FirstOf(Letter(), Digit(),'_',"#"));
    }

    Rule Letter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    Rule Digit() {
        return CharRange('0', '9');
    }

    Rule NonZeroInteger(Var<Integer> value){
        return Sequence(Sequence(CharRange('1', '9'),ZeroOrMore(Digit())),value.set(Integer.parseInt(match())));
    }

    Rule NonZeroIntegerExtend(Var<ArrayList<Integer>> value){
        return Sequence(Sequence(CharRange('1', '9'),ZeroOrMore(Digit())),value.set(extendIntList(value.get(),Integer.parseInt(match()))));
    }
    @SuppressNode
    Rule PositiveInteger(Var<Integer> value){
        return Sequence(OneOrMore(Digit()),value.set(Integer.parseInt(match())));
    }

    @SuppressNode
    Rule FileName(StringVar name){
        return Sequence(ZeroOrMore(TestNot(FirstOf(Newline(),DefiniteWhiteSpace())), ANY),name.set(match()));
    }

    Rule AnyState() {
        return FirstOf(EITHER_STATE_CHARACTER,'0','1');
    }
    Rule State(Var<Boolean> stateValue) {
        return Sequence(AnyOf("01"), stateValue.set(stateStringToBoolean(match())));
    }
    Rule States(Var<ArrayList<Boolean>> stateValues) {
        return Sequence(AnyOf("01"), stateValues.set(extendBooleanList(stateValues.get(),stateStringToBoolean(match()))));
    }

    //CONSTANT PATTERNS

    @SuppressNode
    Rule TypeAttacher() {
        //The thing that follows a type annotation. e.g. node: Node1. The ":" is what I'm calling the 'attacher'.
        return Ch(TYPE_ATTACHER);
    }

    @SuppressNode
    Rule AssignmentOperator() {
        return String("is");
    }

    @SuppressNode
    Rule ListDelimiter() {
        return Ch(',');
    }

    @SuppressNode
    Rule ListPartSeparator() {
        return Ch('|');
    }

    @SuppressNode
    Rule DefiniteWhiteSpace() {
        return OneOrMore(AnyOf(" \t\f"));
    }

    @SuppressNode
    Rule PossibleWhiteSpace() {
        return ZeroOrMore(AnyOf(" \t\f"));
    }

    @SuppressNode
    public Rule BlankLine() {
        return Sequence(Sp(), Newline());
    }

    public Rule Newline() {
        return FirstOf('\n', Sequence('\r', Optional('\n')));
    }

    @SuppressNode
    public Rule Sp() {
        return ZeroOrMore(Spacechar());
    }

    @SuppressNode
    public Rule Spacechar() {
        return AnyOf(" \t");
    }


    //UTILITIES

    //TODO Make generic
    public ArrayList<String> extendStringList(ArrayList<String> existing, String newElement) {
        if (existing == null) {
            ArrayList<String> newList = new ArrayList<>();
            newList.add(newElement);
            return newList;
        } else {
            existing.add(newElement);
            return existing;
        }
    }

    public ArrayList<Boolean> extendBooleanList(ArrayList<Boolean> existing, Boolean newElement) {
        if (existing == null) {
            ArrayList<Boolean> newList = new ArrayList<>();
            newList.add(newElement);
            return newList;
        } else {
            existing.add(newElement);
            return existing;
        }
    }

    public ArrayList<ArrayList<Boolean>> extendBooleanListList(ArrayList<ArrayList<Boolean>> existing, ArrayList<Boolean> newElement) {
        if (existing == null) {
            ArrayList<ArrayList<Boolean>> newList = new ArrayList<>();
            newList.add(newElement);
            return newList;
        } else {
            existing.add(newElement);
            return existing;
        }
    }

    public ArrayList<TrackPlayType> extendTrackPlayTypeList(ArrayList<TrackPlayType> existing, TrackPlayType newElement) {
        if (existing == null) {
            ArrayList<TrackPlayType> newList = new ArrayList<>();
            newList.add(newElement);
            return newList;
        } else {
            existing.add(newElement);
            return existing;
        }
    }

    public ArrayList<Integer> extendIntList(ArrayList<Integer> existing, Integer newElement) {
        if (existing == null) {
            ArrayList<Integer> newList = new ArrayList<>();
            newList.add(newElement);
            return newList;
        } else {
            existing.add(newElement);
            return existing;
        }
    }




    //BOOLEAN CHECKS

    public Boolean stateStringToBoolean(String state) {
        if(Objects.equals(state, "1")){
            return true;
        }else if(Objects.equals(state, "0")){
            return false;
        }else if(Objects.equals(state,EITHER_STATE_CHARACTER)){
            return null;
        }
        return null;
    }

    public boolean isVariableType(String string) {
        return VARIABLE_TYPES.contains(string);
    }

    public boolean setNodeEncountered(String string) {
        encounteredNodeVariables.add(string);
        return true;
    }

    public boolean isNodeEncountered(String string) {
        if (encounteredNodeVariables.contains(string)) {
            System.out.println("Already saw " + string);
        } else {
            System.out.println("Didn't yet see " + string);
        }
        return encounteredNodeVariables.contains(string);
    }


    public void printEncountered() {
        for (String s : this.encounteredNodeVariables) {
            System.out.println(s);
        }
    }

}
