package twoPCInterface; 

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoordinatorInterface extends Remote {
	public void receiveVote(String abortOrCommit, int participantNum) throws RemoteException;
	public void receiveAck(String abortOrCommit, int participantNum) throws RemoteException;
	public void addParticipant(ParticipantInterface participant, int participantNum) throws RemoteException;
}