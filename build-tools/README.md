To run PMD check or formatter without running everything else do:

`mvn pmd:check -am clean install`

`mvn formatter:format -am clean install`

note: pmd check works best with `<analysisCache>false</analysisCache>` on line 385 on root project pom.xml