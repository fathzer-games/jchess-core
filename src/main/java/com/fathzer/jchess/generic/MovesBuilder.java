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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

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
import com.fathzer.jchess.generic.movevalidator.MoveValidator;
import com.fathzer.jchess.generic.movevalidator.MoveValidatorBuilder;

public class MovesBuilder {
	@FunctionalInterface
	public interface MoveAdder {
		void add(List<Move> moves, int from, int to);
	}
	
	private static final MoveAdder DEFAULT = (moves, f, t) -> moves.add(new BasicMove(f,t));
	private static final MoveAdder WHITE_PROMOTION = (moves, f, t) -> {
		moves.add(new BasicMove(f, t, WHITE_KNIGHT));
		moves.add(new BasicMove(f, t, WHITE_QUEEN));
		moves.add(new BasicMove(f, t, WHITE_ROOK));
		moves.add(new BasicMove(f, t, WHITE_BISHOP));
	};
	private static final MoveAdder BLACK_PROMOTION = (moves, f, t) -> {
		moves.add(new BasicMove(f, t, BLACK_KNIGHT));
		moves.add(new BasicMove(f, t, BLACK_QUEEN));
		moves.add(new BasicMove(f, t, BLACK_ROOK));
		moves.add(new BasicMove(f, t, BLACK_BISHOP));
	};

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
			}
			sorted = true;
			return moves;
		}
	}
	
	private static final Collection<Direction> WHITE_PAWN_CATCH_DIRECTIONS = Arrays.asList(NORTH_WEST, NORTH_EAST);
	private static final Collection<Direction> BLACK_PAWN_CATCH_DIRECTIONS = Arrays.asList(SOUTH_WEST, SOUTH_EAST);
	
	private final ChessBoard board;
	private final BoardExplorer from;
	private final DirectionExplorer to;
	private final Supplier<MoveValidator> mvBuilder;
	private MoveValidator mv;
	private Comparator<Move> moveComparator;

	private MovesBuilderState state;
	
	public MovesBuilder(ChessBoard board) {
		this.board = board;
		this.from = board.getExplorer();
		this.to = board.getDirectionExplorer();
		this.mvBuilder = new MoveValidatorBuilder(board);
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
		this.mv = mvBuilder.get();
		this.from.reset(0);
	}

	protected List<Move> getMoves() {
		if (state.needRefresh) {
			init();
			final Color color = board.getActiveColor();
			if (board.isDoubleCheck()) {
				// If double check, only king can move
				final int kingPosition = board.getKingPosition(color);
				from.reset(kingPosition);
				to.reset(kingPosition);
				addKingMoves();
			} else {
				do {
					if (from.getPiece()!=null && color==from.getPiece().getColor()) {
						addPossibleMoves();
					}
				} while (from.next());
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
	
	private void addPossibleMoves() {
		final Piece piece = from.getPiece();
		to.reset(from.getIndex());
		if (ROOK.equals(piece.getKind()) || BISHOP.equals(piece.getKind()) || QUEEN.equals(piece.getKind())) {
			for (Direction d:piece.getKind().getDirections()) {
				addAllMoves(d, mv.getOthers());
			}
		} else if (KNIGHT.equals(piece.getKind())) {
			for (Direction d:KNIGHT.getDirections()) {
				addMove(d, mv.getOthers(), DEFAULT);
			}
		} else if (KING.equals(piece.getKind())) {
			addKingMoves();
		} else if (PAWN.equals(piece.getKind())) {
			addPawnMoves();
		} else {
			throw new IllegalArgumentException("Unknown piece kind: "+piece.getKind());
		}
	}
	
	protected void addKingMoves() {
		// We can think remember the free safe cells could be reused in castling //TODO
		// StandardMoves => King can't go to attacked cell
		for (Direction d:KING.getDirections()) {
			addMove(d, mv.getKing(), DEFAULT);
		}
		// Castlings
		if (!board.isCheck()) {
			// No castlings allowed when you're in check
			if (WHITE==from.getPiece().getColor()) {
				tryCastling(Castling.WHITE_KING_SIDE);
				tryCastling(Castling.WHITE_QUEEN_SIDE);
			} else {
				tryCastling(Castling.BLACK_KING_SIDE);
				tryCastling(Castling.BLACK_QUEEN_SIDE);
			}
		}
	}
	private void tryCastling(Castling castling) {
		if (board.hasCastling(castling)) {
			final int kingPosition = from.getIndex(); 
			final int kingDestination = board.getKingDestination(castling);
			final int rookPosition = board.getInitialRookPosition(castling);
			final int rookDestination  = kingDestination + castling.getSide().getRookOffset();
			if (areCastlingCellsFree(from, kingDestination, rookPosition, rookDestination) &&
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
		DEFAULT.add(moves, kingPosition, kingDestination);
	}
	
	protected boolean areCastlingCellsFree(BoardExplorer exp, int kingDestination, int rookPosition, int rookDestination) {
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
	protected boolean areCastlingCellsSafe(Color attacker, int kingPosition, int kingDestination) {
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
		return blackPlaying?board.getDimension().getHeight()-2:1;
	}
	
	private int getPawnMaxMoveLength(boolean isBlack, int from) {
		final int startRow = isBlack ? 1 : board.getDimension().getHeight()-2;
		return board.getCoordinatesSystem().getRow(from) == startRow ? 2 : 1;
	}

	private void addPawnMoves() {
		final boolean black = BLACK == board.getActiveColor();
		// Take care of promotion when generating move
		final int promotionRow = getPromotionRow(black);
		final MoveAdder generator;
		if (board.getCoordinatesSystem().getRow(from.getIndex())==promotionRow) {
			generator = black ? BLACK_PROMOTION : WHITE_PROMOTION;
		} else {
			generator = DEFAULT;
		}
		final int countAllowed = getPawnMaxMoveLength(black, from.getIndex());
		if (black) {
			// Standard moves (no catch)
			addMoves(Direction.SOUTH, countAllowed, mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			addMove(Direction.SOUTH_EAST, mv.getPawnCatch(), generator);
			addMove(Direction.SOUTH_WEST, mv.getPawnCatch(), generator);
		} else {
			// Standard moves (no catch)
			addMoves(Direction.NORTH, countAllowed, mv.getPawnNoCatch(), generator);
			// Catches (including En-passant)
			addMove(Direction.NORTH_EAST, mv.getPawnCatch(), generator);
			addMove(Direction.NORTH_WEST, mv.getPawnCatch(), generator);
		}
	}
	
	public void addAllMoves(Direction direction, BiPredicate<BoardExplorer, BoardExplorer> validator)  {
		to.start(direction);
		while (to.next()) {
			if (validator.test(from, to)) {
				DEFAULT.add(state.moves, from.getIndex(), to.getIndex());
			}
			if (to.getPiece()!=null) {
				break;
			}
		}
	}

	public void addMoves(Direction direction, int maxIteration, BiPredicate<BoardExplorer, BoardExplorer> validator, MoveAdder moveGenerator)  {
		to.start(direction);
		int iteration = 0;
		while (to.next()) {
			if (validator.test(from, to)) {
				moveGenerator.add(state.moves, from.getIndex(), to.getIndex());
			}
			iteration++;
			if (iteration>=maxIteration || to.getPiece()!=null) {
				break;
			}
		}
	}
	
	public void addMove(Direction direction, BiPredicate<BoardExplorer, BoardExplorer> validator, MoveAdder moveGenerator)  {
		to.start(direction);
		if (to.next() && validator.test(from, to)) {
			moveGenerator.add(state.moves, from.getIndex(), to.getIndex());
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
		final int src = move.getFrom();
		// Check a piece of the active color is moving
		final Piece piece = board.getPiece(src);
		if (piece==null || piece.getColor()!=activeColor) {
			return false;
		}
		// Check piece does not catch its own color piece
		final int target = move.getTo();
		final Piece caught = board.getPiece(target);
		if (caught!=null && caught.getColor()==activeColor) {
			return false;
		}
		// Check promotion is valid
		final Piece promotion = move.getPromotion();
		// Reject promotions if moving piece is not a pawn or promotion is king or pawn or
		// has the wrong color or has not the right destination row
		if (promotion!=null && (piece.getKind()!=PAWN || promotion.getKind()==PAWN || promotion.getKind()==KING
				|| promotion.getColor()!=activeColor || getPromotionRow(activeColor==BLACK)!=board.getCoordinatesSystem().getRow(src))) {
			return false;
		}
		init();
		this.to.reset(src);
		final boolean isCheck = board.isCheck();
		if (piece.getKind()==KING) {
			final Castling castling = board.getCastling(src, target);
			if (castling!=null) {
				if (isCheck || !board.hasCastling(castling)) {
					return false;
				}
				final int rookPosition = board.getInitialRookPosition(castling);
				final int rookDestination  = target + castling.getSide().getRookOffset();
				this.from.reset(src);
				return areCastlingCellsFree(this.from, target, rookPosition, rookDestination) &&
						areCastlingCellsSafe(board.getActiveColor().opposite(), src, target);
			} else {
				return !board.isAttacked(target, activeColor.opposite()) && isReachable(KING.getDirections(), target, 1)!=null;
			}
		} else {
			// Test that position is reachable
			final Direction direction = isReachable(piece, src, target, caught);
			if (direction==null) {
				return false;
			}
			// Test piece is not pinned
			final Direction pinnedDirection = board.getPinnedDetector().apply(src);
			final boolean notPinned = pinnedDirection==null || pinnedDirection==direction || pinnedDirection.getOpposite()==direction;
			final boolean kingSafetyCheck = isCheck || (PAWN==piece.getKind() && target==board.getEnPassant());
			return notPinned && (!kingSafetyCheck || board.isKingSafeAfterMove(src, target));
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
		for (Direction d:dirs) {
			this.to.start(d);
			if (this.to.canReach(to, maxIteration)) {
				return d;
			}
		}
		return null;
	}

	private Direction canNonCatchingPawnReach(boolean isBlack, int from, int to) {
		final int maxMoveLength = getPawnMaxMoveLength(isBlack, from);
		final Direction d = isBlack ? SOUTH : NORTH;
		this.to.start(d);
		return this.to.canReach(to, maxMoveLength) ? d : null;
	}
}
