all:			DownUnderClient.class DownUnderServer.class \
			DownUnder.class DownUnderInterface.class

DownUnder.class:		DownUnder.java DownUnderInterface.class
			@javac DownUnder.java

DownUnderInterface.class:	DownUnderInterface.java
			@javac DownUnderInterface.java

DownUnderClient.class:	DownUnderClient.java
			@javac DownUnderClient.java

DownUnderServer.class:	DownUnderServer.java
			@javac DownUnderServer.java

run:			all
			@java DownUnderServer &
			@sleep 1
			@java DownUnderClient

clean:
			@rm -f *.class *~

info:
			@echo "(c) Sidney Reis (03 mai. 2017)"

