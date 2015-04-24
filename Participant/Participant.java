package Participant;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.lang.*; //for integer conversion. 
import java.io.*;

import twoPCInterface.*; 


public class Participant implements ParticipantInterface {


	private String state = "initial"; 
    private CoordinatorInterface coordinator; 
    private int participantNum; 
    private String fail; 

	public void receivePrepare(){
		this.state = "ready"; 
        logString("receivedPrepare"); 
        String vote = readVoteInput(); 

        try{
            System.out.println("aboutToSendVote:" + vote); 
            coordinator.receiveVote(vote, this.participantNum);
            return;
        }
        catch(Exception e){ //coordinator failure. 
            //block==DoNothing
            logString("receiveVote('commit') Exception"); 
            lookupCoordinator(); 
            // this.receivePrepare();
        }

	}

    public void receiveAbort(){
        this.state = "abort";
        logString("receivedAbort"); readInput(); 
        try{
            System.out.println("aboutToSendAck:" + this.state); 
            coordinator.receiveAck("abort", this.participantNum);
        }
        catch(Exception e){
            logString("coordinator.receiveAck('abort') Exception");
            //lookupCoordinator();
            //this.receiveAbort();
        } //do nothing

    }

    public void receiveCommit(){
        this.state = "commit"; 
        logString("receivedCommit"); readInput();
 

        try{
            System.out.println("aboutToSendAck:" + this.state); 
            coordinator.receiveAck("commit", this.participantNum);
        }
        catch(Exception e){
            logString("coordinator.receiveAck('commit') Exception");
            //lookupCoordinator();
            //this.receiveCommit();
        } 
    }

    private synchronized void logString(String mystring){
        System.out.println("this.state:" +  this.state); 
        try {
            File log = new File("./logs/Participant"+ participantNum + "Log.txt"); 
            if(!log.exists()){log.createNewFile();}
            BufferedWriter bw = new BufferedWriter(new FileWriter(log , true)); 
            bw.write(mystring+"\n");
            System.out.println(mystring); 
            bw.close();
        }catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private synchronized String readVoteInput(){
        String input = System.console().readLine("\tPleaseVote: commit XOR abort"); 
        if(input.equals("abort")){ return "abort"; }
        else if(input.equals("commit")){return "commit";}
        else{
            System.out.println("incorrectInput VoteAgain"); 
            return this.readVoteInput();
        }
    }

    public synchronized void addCoordinator(CoordinatorInterface coordinator0){
        this.coordinator = coordinator0; 
    }


   private synchronized void readInput(){ 
        String input = System.console().readLine("\tType:Enter=>continue,fail=>system.exit()"); 
        if(input.equals("fail")){
            System.exit(0); 
        }
    }

    private synchronized void lookupCoordinator(){
        try {
            Registry registry /*participantRegistry*/ = LocateRegistry.getRegistry(); 
            this.coordinator = 
                (CoordinatorInterface) registry.lookup("coordinator0");
        }
        catch(Exception e){
            System.out.println("Coordinator Couldn't be re-lookedup"); 
        }
    }



    public static void main(String args[]) {
        // if (System.getSecurityManager() == null) {
        //     System.setSecurityManager(new SecurityManager());
        // }
        try { 
            Participant participant = new Participant();
            ParticipantInterface stub = 
                (ParticipantInterface) UnicastRemoteObject.exportObject(participant, 0);
            Registry registry /*participantRegistry*/ = LocateRegistry.getRegistry(); 
            /*LocateRegistry.getRegistry("localhost");LocateRegistry.getRegistry(args[0]);*/

            participant.participantNum = Integer.parseInt(args[0]);

            registry.rebind("participant"+participant.participantNum, stub);
           
            participant.coordinator = 
                (CoordinatorInterface) registry.lookup("coordinator0");

            participant.coordinator.addParticipant(participant, participant.participantNum); 
        } catch (Exception e) {
            System.err.println("ComputePi exception:");
            e.printStackTrace();
        }
    }    
}