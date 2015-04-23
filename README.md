step0: set the proper path in the policy.policy file. 
step1: run the rmiRegistry
step2: Run the coordinator
step3: Run the two Participants
step4: You're done! If you want to run it again, make sure to remove the generated log files. 

Run RMIRegistry: rmiregistry &

RunCoordinator: 
java -Djava.rmi.server.hostname=127.0.0.1 -Djava.security.debug=access,failure Coordinator/Coordinator dontFail
//"FailBroadCastCommit"

RunClient: 
java -Djava.security.debug=access,failure Participant/Participant 0 notFail
java -Djava.security.debug=access,failure Participant/Participant 1 notFail
//The first argument is the index, the second argument is whether the participant should fail or not. "failwait"
//-Djava.security.debug=access,failure
//-Djava.security.policy=policy.policy

CompileCoordinator: javac ./Coordinator/Coordinator.java
CompileClient: javac ./Participant/Participant.java

To learn more about compiling and running javaRMI programs: http://docs.oracle.com/javase/tutorial/rmi/example.html