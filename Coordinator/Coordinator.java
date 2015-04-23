package Coordinator;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import twoPCInterface.*; 

import java.util.Scanner;
import java.io.*; //file,bufferWriter,etc

// import compute.Compute;
// import compute.Task;

// import java.util.concurrent.atomic.AtomicInteger;
public class Coordinator implements CoordinatorInterface {
//HARD-CODING: FAILURES IN!!! (just comment them out??)

	private ParticipantInterface[] participants = new ParticipantInterface[2];
    //private ArrayList<ParticipantInterface> participants = new ArrayList<ParticipantInterface>();
    private int numParticipants = 0;  
	private String state = "initial"; 
	//private AtomicInteger voteTally = new AtomicInteger(0);

	private boolean[] voteCommitArray = {false,false}; //new boolean[2]; 
    //private int voteAbortTally; //one abort suffices
    private boolean[] ackCommitArray = {false,false}; //new boolean[2];  
    private boolean[] ackAbortArray = {false,false}; //new boolean[2]; 
    private String fail; 

	private Coordinator() {
        super();
    }

    /*broadcast*/
    private synchronized void broadcastPrepare(){
        logString("About To broadcastPrepare"); readInput();
        for(ParticipantInterface participant : this.participants){
    		this.createThread(participant); 
    	}
    }
    private void broadcastGlobalCommit(){
        logString("About To broadcastCommit"); readInput();
        for(ParticipantInterface participant : this.participants){
            this.createThread(participant); 
        }
    }
    private synchronized void broadcastGlobalAbort(){
        logString("About to broadcastAbort"); readInput();
        for(ParticipantInterface participant : this.participants){
            this.createThread(participant); 
        }
    }

    private synchronized void createThread(ParticipantInterface tempParticipant){
        final ParticipantInterface participant = tempParticipant; 
        if(this.state.equals("abort")){ //UsedForBroadCastAbort
            (new Thread(){ public synchronized void run(){
                try { 
                    logString("participant.receiveAbort() aboutToBeCalled"); 
                    readInput();
                    participant.receiveAbort(); 
                }
                catch (Exception e) { //remote exception == participant siteFailure. 
                    //Coordinator.this.participants.remove(participant); 
                }
            }}).start(); //run-thread
        }

        if(this.state.equals("commit")){ //usedForBroadCastCommit
            (new Thread(){ public synchronized void run(){
                try { 
                    logString("participant.receiveCommit() aboutToBeCalled"); 
                    readInput();
                    participant.receiveCommit(); 
                }
                catch (Exception e) { //remote exception == siteFailure. 
                    //Coordinator.this.participants.remove(participant); 
                }
            }}).start(); //run-thread
        }
        if(this.state.equals("initial")) {//UsedForBroadCastPrepare
            (new Thread(){ public synchronized void run(){
                try { 
                    logString("participant.receivePrepare() aboutToBeCalled"); 
                    readInput();
                    participant.receivePrepare(); 
                }
                catch (Exception e) { //remote exception == siteFailure. 
                    //Coordinator.this.participants.remove(participant); 
                    Coordinator.this.broadcastGlobalAbort();
                }
            }}).start(); //run-thread
        }
    }


    /*Receive*/

    public synchronized void receiveVote(String abortOrCommit, int participantNum){
        logString("receiveVote:" + abortOrCommit); readInput();
        if(abortOrCommit.equals("commit")){
            voteCommitArray[participantNum] = true; 
            if(allVoted(voteCommitArray)){
                setFalse(voteCommitArray); 
                state="commit"; logState(); 
                broadcastGlobalCommit(); 
            }
        }
        else{ //votedAbort
            setFalse(voteCommitArray); state="abort"; logState();
            broadcastGlobalAbort();
        }
    }
    public synchronized void receiveAck(String abortOrCommit, int participantNum){
        logString("receiveAck:" + abortOrCommit); readInput();
        if(abortOrCommit.equals("commit")){
            ackCommitArray[participantNum] = true; 
            if(allVoted(ackCommitArray)){
                state="END_TRANSACTION"; logState(); 
            }
        }
        else{ //ackAbort
            ackAbortArray[participantNum] = true; 
            if(allVoted(ackAbortArray)){
                state="END_TRANSACTION"; logState(); 
            }
        }
    }



    /*********HELPER METHODS******/ 

    private synchronized boolean allVoted(boolean[] arr){
        return (arr[0] == true) && (arr[1] == true); 
    }
    private synchronized void setFalse(boolean[] arr){
        arr[0] = false; arr[1] = false; 
    }

    public synchronized void addParticipant(ParticipantInterface participant, int participantNum){
        logString("addParticipant"); readInput();
        this.participants[participantNum] = participant; 
        this.createThread(participant); 
    }

    private synchronized void lookupParticipants(){
        logString("lookupParticipants"); readInput();
        Registry registry; /*participantRegistry*/ //(assuming both participants have same registry)
        try{
            registry = LocateRegistry.getRegistry(); /*LocateRegistry.getRegistry("localhost");*/
            ParticipantInterface temp0 = 
                (ParticipantInterface) registry.lookup("participant0");
            this.addParticipant(temp0, 0); 
        } catch(Exception e){ //multipleFailures
            //wait for the participant to come back online. 
        }
        try{
            registry = LocateRegistry.getRegistry(); /*LocateRegistry.getRegistry("localhost");*/
            ParticipantInterface temp1 = 
                (ParticipantInterface) registry.lookup("participant1");
            this.addParticipant(temp1, 1); 
        } catch(Exception e){ //multipleFailures
            //wait for the participant to come back online. 
        }
    }

    private synchronized void logState(){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./logs/coordinatorState.txt"), false)); 
            bw.write(this.state);
            System.out.println("this.state= " + this.state); 
            bw.close();
        }catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private synchronized void logString(String mystring){
        try {
            File log = new File("./logs/Coordinator0Log.txt"); 
            if(!log.exists()){log.createNewFile();}
            BufferedWriter bw = new BufferedWriter(new FileWriter(log , true)); 
            bw.write(mystring+"\n");
            System.out.println(mystring); 
            bw.close();
        }catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

   private synchronized void readInput(){ 
        String input = System.console().readLine("\tType:Enter=>continue,fail=>system.exit()"); 
        if(input.equals("fail")){
            System.exit(0); 
        }
    }





	public synchronized static void main(String[] args) {
        // if (System.getSecurityManager() == null) {
        //     System.setSecurityManager(new SecurityManager());
        // }
        Coordinator coordinator = new Coordinator();
        coordinator.fail = args[0]; 
        try { //register the coordinator in the registry
            CoordinatorInterface stub = 
                (CoordinatorInterface) UnicastRemoteObject.exportObject(coordinator, 0);
            Registry registry /*participantRegistry*/ = LocateRegistry.getRegistry(); /*LocateRegistry.getRegistry("localhost");*/
            registry.rebind("coordinator0", stub);
        } catch (RemoteException e/*Exception e*/) {
            System.err.println("ComputeEngine exception:");
            e.printStackTrace();
        }
        try { //Initialize this object...
            File file = new  File("./logs/coordinatorState.txt"); 

            if (file.exists()) { //The site is recovering!!
                Scanner s = new Scanner(file); 
                coordinator.state = s.useDelimiter("\\Z").next();
                s.close(); 
                coordinator.logString("recovering"); coordinator.readInput();

                coordinator.lookupParticipants();
                //lookUpParticipants will use coordinator.state to decide what to do
                //to decide what to do after it's found the participants. 
            }
            else{ //The site is being initialized
                coordinator.state = "initial"; 
                file.createNewFile();
                coordinator.logState();
            }

        } catch (IOException e) { // if any exception occurs it will catch
            e.printStackTrace();
        }
    } //main
}




