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
