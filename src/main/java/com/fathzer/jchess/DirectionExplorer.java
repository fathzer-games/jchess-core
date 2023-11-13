package com.fathzer.jchess;

/** A class that allows to browse board content in a specific direction.
 */
public interface DirectionExplorer extends BoardExplorer {
	/** Starts exploration in a direction.
	 * <br>The current position is reseted to {@link #getStartPosition()} 
	 * @param direction The direction to explore
	 */
	void start(Direction direction);
	
	/** Gets the start cell's index of the exploration.
	 * @return a integer
	 * @see #getIndex getIndex() to get the current index.
	 */
	int getStartPosition();
	
	/** Check whether a cell can be reach from the current position without encountering a piece. 
	 * @param toIndex The index to reach
	 * @param maxIteration The maximum number of steps to make to reach the <i>toIndex</i> cell.
	 * @return true if <i>toIndex</i> can be reached.
	 */
	default boolean canReach(int toIndex, int maxIteration) {
		int iteration = 0;
		while (next()) {
			iteration++;
			if (getIndex()==toIndex) {
				return true;
			}
			if (iteration>=maxIteration || getPiece()!=null) {
				break;
			}
		}
		return false;
	}
}
