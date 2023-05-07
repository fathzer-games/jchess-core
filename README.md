![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/jchess-core)
![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fathzer-games_jchess-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fathzer-games_jchess-core)
[![javadoc](https://javadoc.io/badge2/com.fathzer/jchess-core/javadoc.svg)](https://javadoc.io/doc/com.fathzer/jchess-core)

# jchess-core
A chess core library

# Thanks to
The src/main/resources/perft.txt file was compiled by the author of [this video on perft method](https://www.youtube.com/watch?v=HGpH28hCw7E&t=2s)

## TODO
* Implement detection of draw by repetition
* Perform some Quiescence Search before evaluate move
* Improve evaluation function
* Use bitboards to generate move faster?
* Use killer moves in Alpha beta pruning moves sort?
* Remove PinnedDetector (and its test) if not used

## Known bugs
* com.fathzer.jchess.generic.BasicMove is not thread safe
