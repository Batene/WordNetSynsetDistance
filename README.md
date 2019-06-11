# Finding the k shortest pathes between two WordNet synsets

todo: possibility to pass path to config.properties file instead of hard-coded path 

## Description
This project retrieves the $k$ shortest pathes between two synsets in WordNet using the implementation of 

@inproceedings{Savenkov:2017:CKS:3132218.3132239,
 author = {Savenkov, Vadim and Mehmood, Qaiser and Umbrich, J\"{u}rgen and Polleres, Axel},
 title = {Counting to K or How SPARQL1.1 Property Paths Can Be Extended to Top-k Path Queries},
 booktitle = {Proceedings of the 13th International Conference on Semantic Systems},
 series = {Semantics2017},
 year = {2017},
 isbn = {978-1-4503-5296-3},
 location = {Amsterdam, Netherlands},
 pages = {97--103},
 numpages = {7},
 url = {http://doi.acm.org/10.1145/3132218.3132239},
 doi = {10.1145/3132218.3132239},
 acmid = {3132239},
 publisher = {ACM},
 address = {New York, NY, USA},
 keywords = {SPARQL, k shortest paths, querying RDF data, routing},
} 

\url{https://bitbucket.org/vadim_savenkov/topk-pfn/src/master/}


## Prerequisites:

0) Java 1.8

1) Jena (https://jena.apache.org/) jars: add the jars to the build path and then to deployment assembly
-> properties -> Java Build Path -> Add jar -> choose the jars
-> Deployment Assembly -> Add -> Java Build Path entries -> choose the jar

2) Tomcat 8.5

3) tomee plus 7

To add the server: 
Choose New -> Others -> Server -> Server -> Next -> Apache -> Tomcat v8.5 -> Next -> Browse (Tomcat installation directory) -> choose the installation directory of tomee plus 7 -> Next -> Select and then Add the Project -> Finish

4) topk-pfn and a jar which is created when running mvn install of it:
Note: the mvn install does not work properly with the current version of topk-pfn, so you need this work-around:

a) add dependency to pom.xml
    <dependency>
    <groupId>com.github.jsonld-java</groupId>
    <artifactId>jsonld-java</artifactId>
    <version>0.12.4</version>
	</dependency>

b) deactivate tests in pom.xml : 
<properties>
        <maven.test.skip>true</maven.test.skip>
</properties>

and run mvn install

or : run mvn install -Dmaven.test.skip=true 


After doing that, right-click on project restful-java-topk -> properties -> Deployment Assembly -> Add -> choose topk-pfn  or or dd the generted jr file 
topk-pfn\target\lib\path-1.0-SNAPSHOT.jar :
-> properties -> Java Build Path -> Add jar -> choose the jar topk-pfn\target\lib\path-1.0-SNAPSHOT.jar
-> properties -> Deployment Assembly -> Add -> Java Build Path entries -> choose the jar topk-pfn\target\lib\path-1.0-SNAPSHOT.jar
furthermore!!! add also the jar generated after running mvn install of the topk-pfn project: 
topk-pfn\target\lib\topk-ce0c13a378.jar (or is this jar alone sufficient?)!!!! 


i) -> properties -> Java Build Path -> Add jar -> choose the jar topk-pfn\target\lib\topk-ce0c13a378.jar
ii) -> properties -> Deployment Assembly -> Add -> Java Build Path entries -> choose the jar topk-pfn\target\lib\topk-ce0c13a378.jar


5) Virtuoso Sparql Endpoint with wordnet 
(e.g. create your virtuoso endpoint and load wordnet into a grahp)

6) virtuoso-jena jars (virt_jena3.jar & virtjdbc4.jar) which come with the VirtJenaProvider ( http://vos.openlinksw.com/owiki/wiki/VOS/VirtJenaProvider): 
first add to build path, then to deployment assembly, see above

7) edit the config.properties file with your own paramters, e.g. if your synsets have another prefix than http://wordnet-rdf.princeton.edu/wn31/ , 
edit in config.properties file (here, the key wordnetSynsetPrefix) before starting to query



## Usage

- either run in Eclipse or export the project as war file

- either query via a browser or use curl on command line:


(if you created a war file, you first need to start the server in order to run it: 
start tomee-plus: go to the installation directory and call: on Windows : bin\startup.bat, on linux: bin\}startup.sh)

Command line usage: e.g. curl -X GET "http://localhost:8080/restful-java-topk/query?s=100017402-n&t=111996783-n&k=3"
Browser usage: open e.g. http://localhost:8080/restful-java-topk/query?s=100017402-n&t=111996783-n&k=3
this will return you the 3 shortest pathes from the synset http://wordnet-rdf.princeton.edu/wn31/100017402-n
to http://wordnet-rdf.princeton.edu/wn31/111996783-n
