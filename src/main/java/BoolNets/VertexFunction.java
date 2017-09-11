package BoolNets;

import Utilities.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class VertexFunction {
    private int arity;
    private String name;
    private String[] argumentNames;
    private boolean defaultValue;
    private Boolean[] outputs;

    /**
     * Create a new vertex function which has the given number of inputs
     * @param arity The number of inputs to this function
     */
    public VertexFunction(int arity, String name, String[] argumentNames, boolean defaultValue){
        this.arity = arity;
        this.name = name;
        this.argumentNames = argumentNames;
        this.defaultValue = defaultValue;
        Boolean[] a = new Boolean[(int) Math.pow(2,arity)];
        Arrays.fill(a,defaultValue);
        this.outputs = a;
    }

    /**
     * Set the output value to be returned by the function when presented with the given ordered array of input values
     * @param inputs The ordered array of input values for the function
     * @param output The value to which the given ordered array of inputs should map
     */
    public void setRule(Boolean[] inputs, Boolean output){
        if(inputs.length != this.getArity()){
            throw new IllegalArgumentException("The given input array does not match the arity of this vertex function. Inputs: " + Integer.toString(inputs.length) + ", Expected: " + Integer.toString(getArity()));
        }else{
            this.outputs[getOutputIndex(inputs)] = output;
        }
    }

    /**
     * Apply {@link #setRule(Boolean[], Boolean)} to all the given input arrays. Assumes the two input lists have equal size.
     * @param inputsCollection A collection of arrays of input values
     * @param outputsCollection A collection of outputs corresponding to each array of input values
     */
    public void setRules(ArrayList<Boolean[]> inputsCollection, ArrayList<Boolean> outputsCollection){
        for(int i = 0; i<inputsCollection.size(); i++){
            setRule(inputsCollection.get(i), outputsCollection.get(i));
        }
    }

    /**
     * When given an array of inputs with some values set to null, duplicate that array and replace null with 'true' in one
     * copy and 'false' in the other copy. Do so recursively, expanding the input collection of arrays from their implicit form
     * (involving possible 'nulls') to their explicit form.
     * @param ruleInputs The ArrayList of Boolean[] arrays representing inputs to the function
     * @param ruleOutputs The ArrayList of Boolean values representing outputs corresponding to each input array.
     * @return A Pair containing a list of Boolean[] arrays of explicit input values and a list
     * of Boolean values representing their corresponding outputs
     */
    public Pair<ArrayList<Boolean[]>,ArrayList<Boolean>> expandLazyRules(ArrayList<Boolean[]> ruleInputs, ArrayList<Boolean> ruleOutputs){
        ArrayList<Boolean[]> newRules = new ArrayList<>();
        ArrayList<Boolean> newOutputs = new ArrayList<>();
        boolean moreNulls = false;
        for(int b=0; b<ruleInputs.size(); b++){
            Boolean[] currentRule = ruleInputs.get(b);
            Boolean[] newB1 = new Boolean[currentRule.length];
            Boolean[] newB2 = new Boolean[currentRule.length];
            boolean split = false;
            for(int i = 0; i<currentRule.length; i++){
                Boolean value = currentRule[i];
                if(value == null){
                    if(!split) {
                        newB1[i] = true;
                        newB2[i] = false;
                        split = true;
                    }else{
                        moreNulls=true;
                    }
                }else{
                    newB1[i] = value;
                    newB2[i] = value;
                }
            }
            newRules.add(newB1);
            newOutputs.add(ruleOutputs.get(b));
            if(split){
                newRules.add(newB2);
                newOutputs.add(ruleOutputs.get(b));
            }
        }
        if(moreNulls){
            Pair<ArrayList<Boolean[]>,ArrayList<Boolean>> recurse = expandLazyRules(newRules,newOutputs);
            newRules = recurse.a;
            newOutputs = recurse.b;

        }
        return new Pair(newRules,newOutputs);
    }

    /**
     * @return The arity of this function (i.e. its number of inputs)
     */
    public int getArity(){
        return this.arity;
    }

    /**
     * @param inputs The ordered array of input values, the location of whose corresponding output value is to be found.
     * @return The index in the output array for the output corresponding to the given ordered array of input values.
     */
    private int getOutputIndex(Boolean[] inputs){
        int index = 0;
        for(int i = 0; i<inputs.length; i++){
            if(inputs[i]){
                index += Math.pow(2,i);
            }
        }
        return index;
    }

    /**
     * Find and return the value for this vertex function when the input values are the node states at the given transport position
     * @param inputEntities The nodes whose values should be taken as inputs to this function
     * @param transportPosition The position of the transport at which to find the states of the input nodes
     * @return The value for the vertex function when the input nodes are in the states they are in at the given transport position
     */
    public Boolean evaluate(BooleanNode[] inputEntities, int transportPosition){
        if(inputEntities.length != this.getArity()){
            throw new IllegalArgumentException("The given input array does not match the arity of this vertex function. Inputs: " + Integer.toString(inputEntities.length) + ", Expected: " + Integer.toString(getArity()));
        }else {
            Boolean[] inputs = new Boolean[getArity()];
            for (int i = 0; i < getArity(); i++) {
                inputs[i] = inputEntities[i].getStateAtTransportPosition(transportPosition);
            }
            return this.outputs[getOutputIndex(inputs)];
        }
    }

    /**
     * Null all of the properties of this vertex function instance
     */
    public void clear(){
        this.name = null;
        this.argumentNames = null;
        this.outputs = null;
    }

    public String getName() {
        return name;
    }

    public void printRules(){
        System.out.println("Vertex Function '"+this.name+"':");
        for(int i = 0; i<this.outputs.length; i++){
            System.out.print(new StringBuilder(Integer.toBinaryString(i)).reverse().toString());
            System.out.print(" -> ");
            System.out.print(this.outputs[i]);
            System.out.println();
        }
    }

}
