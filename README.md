# ontology-linker TFM

In this project, an implemented P2P backend protocol enables clients to parliament and exchange their ontologies between them, before merging individually both data and reducing it into a uniform ontology that serves as a translation between both clients' ontologies, allowing further data exchange between clients as if both used the same data structures.

With several different reducer algorithms tested in variuos test, this investigation aims to check if this reduction is possible, and, if it is, analize the best settings for some scenarios.

To run a client, first set its config in its config file as desirable, and run it with:
```
java -jar hola-si-protocol.jar [client name] [input links file]
```
The test code and scripts are also included in the **test** directory, together with instructions to run each.