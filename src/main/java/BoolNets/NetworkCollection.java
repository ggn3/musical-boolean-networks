package BoolNets;

import Parser.DialogMaker;
import Sound.SoundMaker;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Collects together all the networks loaded by a particular sequence file, providing access to a
 * common {@link SoundMaker} instance and a {@link NetworkSequencer} to handle sequencing and sound production.
 */
public class NetworkCollection {

    private static SoundMaker soundMaker;

    private static HashMap<String,BooleanNetwork> networkVariableNames = new HashMap<>();

    private static NetworkSequencer networkSequencer;

    public NetworkCollection(SoundMaker soundMaker){
        this.soundMaker = soundMaker;
        this.networkSequencer = new NetworkSequencer();
    }

    /**
     * Add a network to the collection, assigning it a unique variable-style name.
     * @param name The unique name for the network
     * @param net The {@link BooleanNetwork} instance being added
     * @throws Exception When the given name has already been used.
     */
    public void addNetwork(String name, BooleanNetwork net) throws Exception {
        if(networkVariableNames.containsKey(name)){
            throw new Exception("The network name "+name+" is already in use.");
        }else{
            networkVariableNames.put(name,net);
        }
    }

    public BooleanNetwork getNetwork(String name) throws Exception {
        if(!networkVariableNames.containsKey(name)){
            BooleanNetwork newNetwork = new BooleanNetwork(soundMaker, name);
            addNetwork(name,newNetwork);
            return newNetwork;
        }
        return networkVariableNames.get(name);
    }

    /**
     * Add a new track to the network sequencer. See {@link NetworkSequencer}.
     * @param trackName A name for the track.
     * @param networkName The variable name of the network to add
     * @throws Exception
     */
    public void addTrack(String trackName, String networkName) throws Exception {
        BooleanNetwork net = getNetwork(networkName);
        networkSequencer.addTrack(trackName,net);
    }

    public Iterator<BooleanNetwork> getNetworksIterator(){
        return this.networkVariableNames.values().iterator();
    }

    public SoundMaker getSoundMaker() {
        return soundMaker;
    }

    public NetworkSequencer getNetworkSequencer() {
        return networkSequencer;
    }

    public void clearEverything(){
        for(BooleanNetwork bn : networkVariableNames.values()){
            bn.clearEverything();
        }
        networkSequencer.clear();
        networkVariableNames.clear();
    }
}
