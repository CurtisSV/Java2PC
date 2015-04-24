package twoPCInterface; 

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ParticipantInterface extends Remote {
	public void receivePrepare() throws RemoteException;
	public void receiveAbort() throws RemoteException;
	public void receiveCommit() throws RemoteException;
	public void addCoordinator(CoordinatorInterface coordinator0) throws RemoteException; 
}