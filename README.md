step1: run the rmiRegistry
step2: Run the coordinator
step3: Run the two Participants
step4: You're done! If you want to run it again, make sure to remove the generated log files. 

Run RMIRegistry: rmiregistry &

RunCoordinator: 
java -Djava.rmi.server.hostname=127.0.0.1 -Djava.security.policy=policy.policy Coordinator/Coordinator

RunClient: 
java -Djava.security.policy=policy.policy Participant/Participant 0
java -Djava.security.policy=policy.policy Participant/Participant 1

CompileCoordinator: javac ./Coordinator/Coordinator.java
CompileClient: javac ./Participant/Participant.java

To learn more about compiling and running javaRMI programs: http://docs.oracle.com/javase/tutorial/rmi/example.html