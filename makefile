CLASSPATH = "src/:src/clientgroup22:src/org/json:libs/*"

default: 
	javac -g -d bin/ -cp $(CLASSPATH) src/clientgroup22/ClientGroup22.java
	
run:
	@sudo ./run.sh 
	
clean: 
	find bin/ -type f -iname \*.class
	find bin/ -type f -iname \*.class -delete
	#rm bin/MoteDump.txt
