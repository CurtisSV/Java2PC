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

	public void receivePrepare(){
		this.state = "ready"; 
        logString("receivedPrepare"); 
		try{coordinator.receiveVote("commit", this.participantNum);}
		catch(Exception e){ //coordinator failure. 
            //block==DoNothing
            logString("receivePrepareException"); 
		} 
	}

    public void receiveAbort(){
        logString("receivedAbort"); 
        this.state = "abort"; 
        try{coordinator.receiveAck("abort", this.participantNum);}
        catch(Exception e){} //do nothing

    }

    public void receiveCommit(){
        logString("receivedCommit"); 
        this.state = "commit"; 
        try{coordinator.receiveAck("commit", this.participantNum);}
        catch(Exception e){} //do nothing. 
    }

    private synchronized void logString(String mystring){
        try {
            File log = new File("./Participant"+ participantNum + "Log.txt"); 
            if(!log.exists()){log.createNewFile();}
            BufferedWriter bw = new BufferedWriter(new FileWriter(log , true)); 
            bw.write(mystring+"\n");
            bw.close();
        }catch (Exception ex) {
            System.out.println(ex.toString());
        }    
    }



    public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
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