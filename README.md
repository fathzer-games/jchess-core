[![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/jchess-core)](https://central.sonatype.com/artifact/com.fathzer/jchess-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/fathzer-games/jchess-core/blob/master/LICENSE)
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
- Finish FENParser for Chess960
- Not sure CompactMoveList.sort is really useful, even if it is effectively called (It does not seems to use any capture information
- General things ... if it does not alter performance too much:
    - Is it a good idea to separate ChessBoards and rules? Would it be better to have newGame and getState methods in ChessBoard? It seems we often need rules and Board.  
A way could be to change Rules to something like Game or Board and only have genericity on Move.
Then, we can imagine adding some useful methods to ChessBoard like isCheck.
    - Could it be a good idea to have the ChessBoard able to rewind moves?
    - Move generation improvements:
        - Remove PinnedDetector (and its test) if not used
        - Use bitboards to generate moves faster? My guess is it is faster. Nevertheless, chesslib implementation, which is not very optimized, is not dramatically faster. Moreover, no way to have it work with some chess variants like Capablanca.
        - Think about using multithreading in move generation. It is quite simple except to check if move make king in check as it moves pieces on the board.
- Engine improvements:
    - Perform some Quiescence Search before evaluate move
    - Improve evaluation function
    - Use killer moves in Alpha beta pruning moves sort?

## Known bugs
- Draw by repetition is not implemented
- com.fathzer.jchess.generic.BasicMove is not thread safe
