[![Maven Central](https://img.shields.io/maven-central/v/com.fathzer/jchess-core)](https://central.sonatype.com/artifact/com.fathzer/jchess-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](https://github.com/fathzer-games/jchess-core/blob/master/LICENSE)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fathzer-games_jchess-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fathzer-games_jchess-core)
[![javadoc](https://javadoc.io/badge2/com.fathzer/jchess-core/javadoc.svg)](https://javadoc.io/doc/com.fathzer/jchess-core)

# jchess-core
A chess core library

## Developer notes

### Tests settings
The *com.fathzer.standard.PerfTTest* class tests moves generation algorithm using a perfT data base.  
The duration and accuracy of this test greatly depends on its search depth.  
This depth is 1 by default (to limit Github's resources consumption - Every push trigger a mvn test action). In order to perform better test, you can set the **perftDepth** system property to a higher value.

*com.fathzer.utils.RandomGeneratorTest* tests that the java.util.Random.Random(long seed) is suitable to generate the bitstrings required to generate [Zobrist keys](https://en.wikipedia.org/wiki/Zobrist_hashing).  
It appears to be the case for all platforms I've tested. So, the test is deactivate by default.  
To activate this test, which is pretty long (8s on my small J4125 powered machine), set the **rndGenTest** system property to true.

### Some performance tips
- Why not using bitmaps?  
The idea is to have a library that can also be used for non 8x8 chess variants, like [Capablanca chess](https://en.wikipedia.org/wiki/Capablanca_chess). The bitmap based [chesslib](https://github.com/bhlangonijr/chesslib) library, which is not really optimized (it generates all moves and then verify they are legal), is currently about 40% faster than this one: 21.8M moves/s vs 15.4M moves/s using this project's perft data set with a depth of 4 using 4 threads on my J4125 Mini PC.
- Some optimization implemented  
    - This library uses a 8x10 [mailbox representation](https://www.chessprogramming.org/Mailbox) (with borders pseudo pieces at each side of the rows). This allow 20% speed improvement compared to a basic 8x8 representation.
    - Most of the time there's no check and no pinned piece. Then, no need to check if moves leave the king attacked (except for king and *en-passant* moves).
    - If a piece is pinned, no need to test if other pieces moves leave the king attacked (except for king and *en-passant* moves).
    - If double check situations, only king can move.
    - The old style code is (sometime) better:  
Some code may seem not very elegant as it uses "old fashion" *for* structures instead of streams. Remember, Stream is cool, but a little bit slower than *for* (stream allocate an object, and object allocation is the usually the enemy of performance). For instance removing streams from AttackDetector class increases speed by 30%!
- Some unexplored optimization ideas
    - When a piece is pinned, only moves in direction of the attacking piece or in the opposite direction are valid.
    - When a knight is pinned, it can't move.
    - In check situations, the only valid moves are the one that intersects the attacks (or catch the piece).

## TODO
- UCI stuff needs to be tested (especially if stop works with JChessEngine).
- Finish FENParser for Chess960
- Implement Capablanca chess
- General things ... if it does not alter performance too much:
    - Move generation improvements:
        - Use bitboards to generate moves faster? My guess is it is faster. Nevertheless, chesslib implementation, which is not very optimized, is not dramatically faster. Moreover, no way to have it work with some chess variants like Capablanca.
        - Think about using multithreading in move generation. It is quite simple except to check if move make king in check as it moves pieces on the board.
- Engine improvements:
    - Perform some Quiescence Search before evaluate move
    - Improve evaluation function
    - Use killer moves in Alpha beta pruning moves sort?

## Known bugs
- JChessUCIEngine play move at the end of go, which I think is a mistake
