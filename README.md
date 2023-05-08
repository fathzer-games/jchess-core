![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/jchess-core)
![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fathzer-games_jchess-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fathzer-games_jchess-core)
[![javadoc](https://javadoc.io/badge2/com.fathzer/jchess-core/javadoc.svg)](https://javadoc.io/doc/com.fathzer/jchess-core)

# jchess-core
A chess core library

# Thanks to
The src/main/resources/Perft.txt file was compiled by the author of [this video on perft method](https://www.youtube.com/watch?v=HGpH28hCw7E&t=2s)

## Developer notes

### Tests settings
The *com.fathzer.standard.PerfTTest* class tests moves generation algorithm using a perfT data base.  
The duration and accuracy of this test greatly depends on its search depth.  
This depth is 1 by default (to limit Github's resources consumption - Every push trigger a mvn test action). In order to perform better test, you can set the **perftDepth** system property to a higher value.

*com.fathzer.utils.RandomGeneratorTest* tests that the java.util.Random.Random(long seed) is suitable to generate the bitstrings required to generate [Zobrist keys](https://en.wikipedia.org/wiki/Zobrist_hashing).  
It appears to be the case for all platforms I've tested. So, the test is deactivate by default.  
To activate this test, which is pretty long (8s on my small J4125 powered machine), set the **rndGenTest** system property to true.

## TODO
* Implement detection of draw by repetition
* Perform some Quiescence Search before evaluate move
* Improve evaluation function
* Use bitboards to generate move faster?
* Use killer moves in Alpha beta pruning moves sort?
* Remove PinnedDetector (and its test) if not used

## Known bugs
* com.fathzer.jchess.generic.BasicMove is not thread safe
