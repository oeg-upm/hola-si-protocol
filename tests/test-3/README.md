TEST 3: Execution instructions

1. Config files for both clients is in client[num]/data/config/config.json
2. Parameters linker, reducer, increment, special and tautology may be changed for different test configurations
3. Parameter linker should be set to "file" before testing, making it take rdf file as a stable and immutable input throughout the tests
	3.1 linker can also be set to json to read custom initial links, which must be added in the alignments.json file with the propper json structure
4. To run test, open cmd, go to directory where Automation is, and run: 
```
			java -jar Automation.jar [client1 links] [number of iterations]
```
5. Test execution always sets config linker value to "json" 

The data for these tests can be found in the [Anatomy Ontology](https://oaei.ontologymatching.org/2015/anatomy/) section from the OAEI