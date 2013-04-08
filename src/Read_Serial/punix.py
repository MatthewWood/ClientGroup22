#!/usr/bin/python
# -*- coding: ascii -*-

#Simple program to get sensor data captured by serialdump-linux.c (provided by 
# Adam Dunkels @SICS) from sensor mote.
#Executes serialdump as a sub process and pipes the output here 
#Usage python unix.py /dev/ttyUSB0
#To turn debugging off: python -O <args ...>
#Author: Dominic Follett

import sys, subprocess, shlex, signal

def signal_handler(signal, frame):
    print " \nExiting..."
    sys.exit()

def getSensorData(cmd):
	if __debug__:
		print "Executing " + cmd
		
	#split string to determine the correct tokenization for 'args' to popen on unix	
	args = shlex.split(cmd)
	
	#check args
	if __debug__:
		print args
		
	#execute cmd as subprocess	
	p = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
	
	while(True):
		retcode = p.poll() #returns None while subprocess is running
		line = p.stdout.readline() 
		
		# begin --- your handling can go here
		print line
		# end --- your handling can go here
		
		if(retcode is not None):
			break


#handle keyboard interrupts cleanly
signal.signal(signal.SIGINT, signal_handler)
			
# get port adddr
try:
	portAddr = sys.argv[1]
except IndexError, e:
	print "You need to specify the port address. Error :"
	sys.exit(e)

#path of serialdump-linux - best to keep the program in the same directory and leave this
SERIALDUMP_LINUX = "./serialdump-linux";

if __debug__:
	print "listening on communication port: " + portAddr

# concat the entire command  -  the baud rate is typically 115200 so you shouldn't need to alter this
fullCommand = SERIALDUMP_LINUX + " " + "-b115200" + " " + portAddr
                
getSensorData(fullCommand)

