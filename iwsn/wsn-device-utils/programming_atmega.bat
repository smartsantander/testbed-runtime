echo #### PROGRAMMING WILL BE STARTED NOW... ####
rem prog_avrdude_jtagicemkII_atmega.bat %1
rem avrdude -p m2560 -c jtagmkII -e -U flash:w:%1


rem prog_avrdude_dragon_atmega.bat %1
rem avrdude -p m2560 -c dragon_jtag -e -U flash:w:%1


rem prog_atmeltool_jtagicemkII_atmega.bat %1
JTAGICEmkII\jtagiceii.exe -d ATmega2560 -e -pf -if %1


rem prog_atmeltool_dragon_atmega.bat %1
rem C:\Program Files\Atmel\AVR Tools\AvrDragon\AVRDragon.exe -d ATmega2560 -e -pf -if %1

pause