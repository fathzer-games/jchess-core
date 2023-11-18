package com.fathzer.jchess.generic;

import static com.fathzer.jchess.Piece.*;
import static com.fathzer.jchess.PieceKind.*;
import static com.fathzer.jchess.Direction.*;
import static com.fathzer.games.Color.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntPredicate;

import com.fathzer.games.Color;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.BoardExplorer;
import com.fathzer.jchess.Castling;
import com.fathzer.jchess.Direction;
import com.fathzer.jchess.DirectionExplorer;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.PieceKind;
import com.fathzer.jchess.generic.InternalMoveBuilder.MoveGenerator;

public class MovesBuilder {
	static class MovesBuilderState {
		private static final int MAX_POSSIBLE_MOVES = 218;
		List<Move> moves;
		boolean sorted;
		boolean needRefresh;
		Status status;
		
		public MovesBuilderState() {
			moves = new ArrayList<>(MAX_POSSIBLE_MOVES);
			needRefresh = true;
		}

		public void invalidate() {
			this.moves.clear();
			this.needRefresh = true;
			this.status = null;
		}
		
		public List<Move> sorted(Comparator<Move> comparator) {
			if (!sorted && comparator!=null) {
				moves.sort(comparator);
				sorted = true;
			}
			return moves;
		}
	}
	
	private static final Collection<Direction> WHITE_PAWN_CATCH_DIRECTIONS = Arrays.asList(NORTH_WEST, NORTH_EAST);
	private static final Collection<Direction> BLACK_PAWN_CATCH_DIRECTIONS = Arrays.asList(SOUTH_WEST, SOUTH_EAST);
	
	private final ChessBoard board;
	private InternalMoveBuilder tools;
	private Comparator<Move> moveComparator;
	private MovesBuilderState state;
	
	public MovesBuilder(ChessBoard board) {
		this.board = board;
		final Function<Board<Move>, Comparator<Move>> moveComparatorBuilder = board.getMoveComparatorBuilder();
		if (moveComparatorBuilder!=null) {
			this.moveComparator = moveComparatorBuilder.apply(board);
		}
		this.state = new MovesBuilderState();
	}
	
	public void saveTo(ChessBoardState chessboardState) {
		chessboardState.moveBuidlerState = state;
	}
	
	public void restoreFrom(ChessBoardState chessboardState) {
		state = chessboardState.moveBuidlerState;
	}
	
	public void setMoveComparator(Comparator<Move> moveComparator) {
		this.moveComparator = moveComparator;
		if (state.moves!=null && moveComparator!=null && state.sorted) {
			state.moves.sort(moveComparator);
		}
	}

	protected void invalidate() {
		this.state.invalidate();
	}
	
	private void init() {
		if (tools==null) {
			tools = new InternalMoveBuilder(board);
		}
		tools.init(state.moves);
	}

	protected List<Move> getMoves() {
		if (state.needRefresh) {
			init();
//System.out.println("Computing move list for "+FENUtils.to(board));
			final Color color = board.getActiveColor();
			final BoardExplorer exp = tools.getFrom();
			if (tools.getCheckCount()>1) {
				// If double check, only king can move
				final int kingPosition = board.getKingPosition(color);
				exp.reset(kingPosition);
				tools.getTo().reset(kingPosition);
				addKingMoves(tools);
			} else {
				do {
					if (exp.getPiece()!=null && color==exp.getPiece().getColor()) {
						addPossibleMoves(tools);
					}
				} while (exp.next());
			}
			state.sorted = false;
			state.needRefresh = false;
		}
		return state.moves;
	}
	
	protected List<Move> getPseudoLegalMoves() {
		// Ensure moves is computed
		getMoves();
		return state.sorted(moveComparator);
	}
	
	private void addPossibleMoves(InternalMoveBuilder tools) {
		final Piece piece = tools.getFrom().getPiece();
		tools.getTo().reset(tools.getFrom().getIndex());
		if (ROOK.equals(piece.getKind()) || BISHOP.equals(piece.getKind()) || QUEEN.equals(piece.getKind())) {
			for (Direction d:piece.getKind().getDirections()) {
				tools.addAllMoves(d, tools.mv.getDefault());
			}
		} else if (KNIGHT.equals(piece.getKind())) {
			for (Direction d:KNIGHT.getDirections()) {
				tools.addMove(d, tools.mv.getDefault());
			}
		} else if (KING.equals(piece.getKind())) {
			addKingMoves(tools);
		} else if (PAWN.equals(piece.getKind())) {
			addPawnMoves(tools);
		} else {
			throw new IllegalArgumentException("Unknown piece kind: "+piece.getKind());
		}
	}
	
	private void addKingMoves(InternalMoveBuilder tools) {
		// We can think remember the free safe cells could be reused in castling //TODO
		// StandardMoves => King can't go to attacked cell
		for (Direction d:KING.getDirections()) {
			tools.addMove(d, tools.mv.getKing());
		}
		// Castlings
		if (tools.getCheckCount()==0) {
			// No castlings allowed when you're in check
			if (WHITE==tools.getFrom().getPiece().getColor()) {
				tryCastling(tools, Castling.WHITE_KING_SIDE);
				tryCastling(tools, Castling.WHITE_QUEEN_SIDE);
			} else {
				tryCastling(tools, Castling.BLACK_KING_SIDE);
				tryCastling(tools, Castling.BLACK_QUEEN_SIDE);
			}
		}
	}
	private void tryCastling(InternalMoveBuilder tools, Castling castling) {
		if (board.hasCastling(castling)) {
			final int kingPosition = tools.getFrom().getIndex(); 
			final int kingDestination = board.getKingDestination(castling);
			final int rookPosition = board.getInitialRookPosition(castling);
			final int rookDestination  = kingDestination + castling.getSide().getRookOffset();
			if (areCastlingCellsFree(tools.getFrom(), kingDestination, rookPosition, rookDestination) &&
					areCastlingCellsSafe(board.getActiveColor().opposite(), kingPosition, kingDestination)) {
				addCastling(state.moves, kingPosition, rookPosition, kingDestination, rookDestination);
			}
		}
	}

	/** Adds a castling to the list of moves.
	 * <br>This default implementation creates a move from the king's current position to its destination.
	 * @param moves The moves list in which to add the move.
	 * @param kingPosition The current king's position.
	 * @param rookPosition The current rook's position.
	 * @param kingDestination The king's destination.
	 * @param rookDestination The rook's destination.
	 */
	protected void addCastling(List<Move> moves, int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		moves.add(new BasicMove(kingPosition, kingDestination));
	}
	
	private boolean areCastlingCellsFree(BoardExplorer exp, int kingDestination, int rookPosition, int rookDestination) {
		final int kingPosition = exp.getIndex();
		final int min = Math.min(Math.min(kingPosition, kingDestination), Math.min(rookPosition, rookDestination));
		final int last = Math.max(Math.max(kingPosition, kingDestination), Math.max(rookPosition, rookDestination));
		exp.reset(min);
		for (int i=min; i<=last; i = exp.getIndex()) {
			if (i!=kingPosition && i!=rookPosition && exp.getPiece()!=null) {
				exp.reset(kingPosition);
				return false;
			}
			exp.next();
		}
		exp.reset(kingPosition);
		return true;
	}
	
	/** Checks the positions that should be safe (not attacked) to have the castling allowed.
	 * @param attacker The color of the attacker of cell 
	 * @param kingPosition Current king's position
	 * @param kingDestination King's destination
	 * @return true if safe. Please note that the king's cell is not checked in this method because the check state is verified before this method is called.
	 */
	private boolean areCastlingCellsSafe(Color attacker, int kingPosition, int kingDestination) {
		final AttackDetector attackDetector = board.getAttackDetector();
		if (kingPosition<kingDestination) {
			for (int i = kingPosition+1; i <= kingDestination; i++) {
				if (attackDetector.isAttacked(i, attacker)) {
					return false;
				}
			}
		} else if (kingPosition!=kingDestination) {
			// Warning, in chess960, king can stay at in position during castling
			for (int i = kingDestination; i < kingPosition; i++) {
				if (attackDetector.isAttacked(i, attacker)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private int getPromotionRow(boolean blackPlaying) {
		return blackPlaying?board.getDimension().getHeight()-1:0;
	}
	
	private int getPawnMaxMoveLength(boolean isBlack, int from) {
		final int startRow = isBlack ? 1 : board.getDimension().getHeight()-2;
		return board.getCoordinatesSystem().getRow(from) == startRow ? 2 : 1;
	}

	private void addPawnMoves(InternalMoveBuilder tools) {
		final boolean black = BLACK == tools.getFrom().getPiece().getColor();
		// Take care of promotion when generating move
		final int promotionRow = getPromotionRow(black);
		final IntPredicate promoted = i -> board.getCoordinatesSystem().getRow(i)==promotionRow;
		final MoveGenerator generator = (m, f, t) -> {
			if (promoted.test(t)) {
				m.add(new BasicMove(f, t, black ? BLACK_KNIGHT : WHITE_KNIGHT));
				m.add(new BasicMove(f, t, black ? BLACK_QUEEN : WHITE_QUEEN));
				m.add(new BasicMove(f, t, black ? BLACK_ROOK : WHITE_ROOK));
				m.add(new BasicMove(f, t, black ? BLACK_BISHOP : WHITE_BISHOP));
			} else {
				m.add(new BasicMove(f, t));
			}
		};
		final int countAllowed = getPawnMaxMoveLength(black, tools.getFrom().getIndex());
		if (black) {
			// Standard moves (no catch)
			tools.addMoves(Direction.SOUTH, countAllowed, tools.mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			tools.addMove(Direction.SOUTH_EAST, tools.mv.getPawnCatch(), generator);
			tools.addMove(Direction.SOUTH_WEST, tools.mv.getPawnCatch(), generator);
		} else {
			// Standard moves (no catch)
			tools.addMoves(Direction.NORTH, countAllowed, tools.mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			tools.addMove(Direction.NORTH_EAST, tools.mv.getPawnCatch(), generator);
			tools.addMove(Direction.NORTH_WEST, tools.mv.getPawnCatch(), generator);
		}
	}
	
	protected Status getStatus() {
		if (state.status==null) {
			state.status=buildStatus();
		}
		return state.status;
	}

	private Status buildStatus() {
		if (board.isInsufficientMaterial() || board.getHalfMoveCount()>=100 || board.isDrawByRepetition()) {
			return Status.DRAW;
		}
		if (getMoves().isEmpty()) {
			if (board.isCheck()) {
				return board.getActiveColor().equals(WHITE) ? Status.BLACK_WON : Status.WHITE_WON;
			} else {
				return Status.DRAW;
			}
		} else {
			return Status.PLAYING;
		}
	}

	protected boolean isLegal(Move move) {
		final Color activeColor = board.getActiveColor();
		final int from = move.getFrom();
		// Check a piece of the active color is moving
		final Piece piece = board.getPiece(from);
		if (piece==null || piece.getColor()!=activeColor) {
			return false;
		}
		// Check piece does not catch its own color piece
		final int to = move.getTo();
		final Piece caught = board.getPiece(to);
		if (caught!=null && caught.getColor()==activeColor) {
			return false;
		}
		// Check promotion is valid
		final Piece promotion = move.getPromotion();
		// Reject promotions if moving piece is not a pawn or promotion is king or pawn or
		// has the wrong color or has not the right destination row
		if (promotion!=null && (piece.getKind()!=PAWN || promotion.getKind()==PAWN || promotion.getKind()==KING
				|| promotion.getColor()!=activeColor || getPromotionRow(activeColor==BLACK)!=board.getCoordinatesSystem().getRow(to))) {
			return false;
		}
		init();
		tools.getTo().reset(from);
		if (piece.getKind()==KING) {
			final Castling castling = board.getCastling(from, to);
			if (castling!=null) {
				if (!board.hasCastling(castling)) {
					return false;
				}
				if (tools.getCheckCount()!=0) {
					return  false;
				}
				final int rookPosition = board.getInitialRookPosition(castling);
				final int rookDestination  = to + castling.getSide().getRookOffset();
				if (!areCastlingCellsFree(tools.getFrom(), to, rookPosition, rookDestination) ||
						!areCastlingCellsSafe(board.getActiveColor().opposite(), from, to)) {
					return false;
				}
			} else if (board.getAttackDetector().isAttacked(to, activeColor.opposite())) {
				return false;
			}
			return isReachable(KING.getDirections(), to, 1)!=null;
		} else {
			// Test that position is reachable
			final Direction direction = isReachable(piece, from, to, caught);
			if (direction==null) {
				return false;
			}
			// Test piece is not pinned
			final Direction pinnedDirection = tools.getPinnedDirection(from);
			return pinnedDirection==null || pinnedDirection==direction || pinnedDirection.getOpposite()==direction; 
		}
	}
	

	private Direction isReachable(Piece piece, int from, int to, Piece caught) {
		final PieceKind kind = piece.getKind();
		if (kind==PAWN) {
			final boolean isBlack = piece.getColor()==BLACK;
			if (to==board.getEnPassant()) {
				caught = isBlack ? WHITE_PAWN : BLACK_PAWN;
			}
			if (caught==null) {
				return canNonCatchingPawnReach(isBlack, from, to);
			} else {
				return isReachable(isBlack ? BLACK_PAWN_CATCH_DIRECTIONS : WHITE_PAWN_CATCH_DIRECTIONS, to, 1);
			}
		} else {
			return isReachable(kind.getDirections(), to, kind.isSliding()?Integer.MAX_VALUE:1);
		}
	}

	private Direction isReachable(Collection<Direction> dirs, int to, int maxIteration) {
		final DirectionExplorer explorer = tools.getTo();
		for (Direction d:dirs) {
			explorer.start(d);
			if (explorer.canReach(to, maxIteration)) {
				return d;
			}
		}
		return null;
	}

	private Direction canNonCatchingPawnReach(boolean isBlack, int from, int to) {
		final int maxMoveLength = getPawnMaxMoveLength(isBlack, from);
		final Direction d = isBlack ? SOUTH : NORTH;
		final DirectionExplorer explorer = tools.getTo();
		explorer.start(d);
		return explorer.canReach(to, maxMoveLength) ? d : null;
	}
}
