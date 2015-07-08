Partial Modification Loader(PML) is a mod/plugin system designed for generic Java application interfacing, utilizing elements of Aspect Oriented Program. (Uses Oracle Java 7 - Dalvik not supported)


This is the premain code for PML - this portion of the code deals with initial setup requirements and contains the API for PML-application communication.

While PML requires usage of the Java Instrumentation API, a standard jre does not support all of the features needed - the Instrumentation code is therefore separated (PML-Agent) so it can be run after the supporting natives have been installed (PML-PreAgent).