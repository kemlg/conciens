For testing:

Export project as runnable jar file. Select the option that adds the required libraries to the jar file.
Export jar to any folder (i.e. $JAR_FOLDER$/GameDummy.jar)

Open a terminal 1 and cd to $JAR_FOLDER$.
java -jar GameDummy.jar

Openn terminal 2 and do:
telnet localhost:6969

Write text on terminal 2, you should see text going out on terminal 1

When you are done, scape character is ctrl + 5 