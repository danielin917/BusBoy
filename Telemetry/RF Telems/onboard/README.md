##Telemetry 

###Install
 - pip install pyzmq
 - pip install pyserial

###To Run
If you are running the root data bus, simply type the command:
 - python launcher.py vehicledatacenter.txt

Everyone else: edit the file client.txt so that the IP address listed as one of the commands under "Data Bus" matches the IP address of the root data bus. Then run:
 - python launcher.py client.txt