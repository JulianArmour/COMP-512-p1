all: java.policy compile-server-rmi

java.policy: FORCE
	@echo "Creating server java policy"
	@echo "grant codebase \"file:`pwd`/-\" {" > java.policy
	@echo "permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

FORCE: ;

RMIInterface.jar: Server/Interface/IResourceManager.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/IResourceManager.java Server/Transaction/InvalidTransaction.java Server/Transaction/TransactionAborted.java
	jar cvf RMIInterface.jar Server/Interface/IResourceManager.class Server/Transaction/InvalidTransaction.java Server/Transaction/TransactionAborted.java

compile-server-rmi: Server/RMI/*.java Server/Interface/*.java Server/Common/*.java Server/LockManager/*.java Server/Transaction/*.java
	javac Server/RMI/*.java Server/Interface/*.java Server/Common/*.java Server/LockManager/*.java Server/Transaction/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class
	rm -f RMIInterface.jar
	rm -f java.policy 
