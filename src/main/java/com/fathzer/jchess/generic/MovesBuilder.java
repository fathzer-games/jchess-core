package com.fathzer.jchess.generic;

import static com.fathzer.jchess.Piece.*;
import static com.fathzer.jchess.PieceKind.*;
import static com.fathzer.jchess.Direction.*;
import static com.fathzer.games.Color.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.fathzer.games.util.MoveList;
import com.fathzer.games.util.SelectiveComparator;

import lombok.Setter;

public class MovesBuilder {
	private enum Mode {LEGAL, PSEUDO, QUIESCE}

	public interface MoveAdder {
		void setList(List<Move> moves);
		void add(int from, int to);
	}
	
	private static class SimpleMoveAdder implements MoveAdder {
		@Setter
		protected List<Move> list;

		@Override
		public void add(int from, int to) {
			list.add(new BasicMove(from, to));
		}
	}
	
	private static class PromotionAdder extends SimpleMoveAdder {
		@Setter
		private Color playingColor;
		@Override
		public void add(int from, int to) {
			if (playingColor==WHITE) {
				list.add(new BasicMove(from, to, WHITE_KNIGHT));
				list.add(new BasicMove(from, to, WHITE_QUEEN));
				list.add(new BasicMove(from, to, WHITE_ROOK));
				list.add(new BasicMove(from, to, WHITE_BISHOP));
			} else {
				list.add(new BasicMove(from, to, BLACK_KNIGHT));
				list.add(new BasicMove(from, to, BLACK_QUEEN));
				list.add(new BasicMove(from, to, BLACK_ROOK));
				list.add(new BasicMove(from, to, BLACK_BISHOP));
			}
		}
	}
	
	protected final MoveAdder defaultMoveAdder = new SimpleMoveAdder();
	protected final PromotionAdder promotionAdder = new PromotionAdder();

	static class MovesBuilderState {
		List<Move> legalMoves;
		boolean needRefreshLegal;
		MoveList<Move> pseudoLegalMoves;
		boolean needRefreshPseudoLegal;
		Status status;
		
		public MovesBuilderState() {
			legalMoves = new ArrayList<>();
			pseudoLegalMoves = new MoveList<>();
			needRefreshLegal = true;
			needRefreshPseudoLegal = true;
		}

		public void invalidate() {
			this.needRefreshLegal = true;
			this.needRefreshPseudoLegal = true;
			this.status = null;
		}
	}
	
	private static final Collection<Direction> WHITE_PAWN_CATCH_DIRECTIONS = Arrays.asList(NORTH_WEST, NORTH_EAST);
	private static final Collection<Direction> BLACK_PAWN_CATCH_DIRECTIONS = Arrays.asList(SOUTH_WEST, SOUTH_EAST);
	
	protected final ChessBoard board;
	private final BoardExplorer from;
	private final DirectionExplorer to;
	private final Supplier<MoveValidator> mvBuilder;
	private MoveValidator mv;
	private SelectiveComparator<Move> moveComparator;

	private MovesBuilderState state;
	
	public MovesBuilder(ChessBoard board) {
		this.board = board;
		this.from = board.getExplorer();
		this.to = board.getDirectionExplorer();
		this.mvBuilder = new MoveValidatorBuilder(board);
		final Function<Board<Move>, SelectiveComparator<Move>> moveComparatorBuilder = board.getMoveComparatorBuilder();
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
	
	public void setMoveComparator(SelectiveComparator<Move> moveComparator) {
		this.moveComparator = moveComparator;
		if (!state.needRefreshPseudoLegal && moveComparator!=null) {
			state.pseudoLegalMoves.setComparator(moveComparator);
			state.pseudoLegalMoves.sort();
		}
	}

	protected void invalidate() {
		this.state.invalidate();
	}
	
	private void init() {
		this.mv = mvBuilder.get();
		this.promotionAdder.setPlayingColor(board.getActiveColor());
		this.from.reset(0);
	}
	
	private void setMode(Mode mode) {
		final List<Move> moves;
		if (mode==Mode.LEGAL) {
			moves = state.legalMoves;
			moves.clear();
		} else {
			state.pseudoLegalMoves.clear();
			state.pseudoLegalMoves.setComparator(this.moveComparator);
			moves = state.pseudoLegalMoves;
			
		}
		this.defaultMoveAdder.setList(moves);
		this.promotionAdder.setList(moves);
	}

	protected List<Move> getPseudoLegalMoves() {
		if (state.needRefreshPseudoLegal) {
			buildMoves(Mode.PSEUDO);
			if (moveComparator!=null) {
				state.pseudoLegalMoves.sort();
			}
			state.needRefreshPseudoLegal = false;
		}
		return state.pseudoLegalMoves;
	}
	
	protected List<Move> getLegalMoves() {
		if (state.needRefreshLegal) {
			buildMoves(Mode.LEGAL);
			state.needRefreshLegal = false;
		}
		return state.legalMoves;
	}
	
	private void buildMoves(Mode mode) {
		setMode(mode);
		init();
		final Color color = board.getActiveColor();
		if (board.isDoubleCheck()) {
			// If double check, only king can move
			final int kingPosition = board.getKingPosition(color);
			from.reset(kingPosition);
			to.reset(kingPosition);
			addKingMoves(mode);
		} else {
			do {
				if (from.getPiece()!=null && color==from.getPiece().getColor()) {
					addPossibleMoves(mode);
				}
			} while (from.next());
			if (board.getEnPassant()>=0) {
				// Generate en passant moves
				addEnPassantMoves(mode!=Mode.LEGAL);
			}
		}
	}
	
	private void addPossibleMoves(Mode mode) {
		final Piece piece = from.getPiece();
		to.reset(from.getIndex());
		if (KING.equals(piece.getKind())) {
			addKingMoves(mode);
		} else {
			final Direction pinnedDirection = board.getPinnedDetector().apply(from.getIndex());
			if (ROOK.equals(piece.getKind()) || BISHOP.equals(piece.getKind()) || QUEEN.equals(piece.getKind())) {
				if (pinnedDirection==null) {
					for (Direction d:piece.getKind().getDirections()) {
						addAllMoves(d, mv.getOthers());
					}
				} else if (piece.getKind().getDirections().contains(pinnedDirection)) {
					addAllMoves(pinnedDirection, mv.getOthers());
					addAllMoves(pinnedDirection.getOpposite(), mv.getOthers());
				}
			} else if (KNIGHT.equals(piece.getKind())) {
				if (pinnedDirection==null) {
					for (Direction d:KNIGHT.getDirections()) {
						addMove(d, mv.getOthers(), defaultMoveAdder);
					}
				}
			} else if (PAWN.equals(piece.getKind())) {
				if (pinnedDirection==null) {
					addPawnMoves();
				} else {
					addPinnedPawnMoves(pinnedDirection);
				}
			} else {
				throw new IllegalArgumentException("Unknown piece kind: "+piece.getKind());
			}
		}
	}
	
	protected void addKingMoves(Mode mode) {
		final boolean pseudoLegal = mode==Mode.PSEUDO;
		// Legal moves => King can't go to attacked cell, pseudo legal, perform no check
		final BiPredicate<BoardExplorer, BoardExplorer> moveValidator = pseudoLegal ? mv.getOthers() : mv.getKing();
		for (Direction d:KING.getDirections()) {
			addMove(d, moveValidator, defaultMoveAdder);
		}
		// Castlings
		if (board.hasCastling() && !board.isCheck()) {
			// No castlings allowed when you're in check
			if (WHITE==from.getPiece().getColor()) {
				tryCastling(Castling.WHITE_KING_SIDE, pseudoLegal);
				tryCastling(Castling.WHITE_QUEEN_SIDE, pseudoLegal);
			} else {
				tryCastling(Castling.BLACK_KING_SIDE, pseudoLegal);
				tryCastling(Castling.BLACK_QUEEN_SIDE, pseudoLegal);
			}
		}
	}
	private void tryCastling(Castling castling, boolean pseudoLegal) {
		if (board.hasCastling(castling)) {
			final int kingPosition = from.getIndex(); 
			final int kingDestination = board.getKingDestination(castling);
			final int rookPosition = board.getInitialRookPosition(castling);
			final int rookDestination  = kingDestination + castling.getSide().getRookOffset();
			if (areCastlingCellsFree(from, kingDestination, rookPosition, rookDestination) &&
					(pseudoLegal || areCastlingCellsSafe(board.getActiveColor().opposite(), kingPosition, kingDestination, rookPosition))) {
				addCastling(kingPosition, rookPosition, kingDestination, rookDestination);
			}
		}
	}

	/** Adds a castling to the list of moves.
	 * <br>This default implementation creates a move from the king's current position to its destination.
	 * @param kingPosition The current king's position.
	 * @param rookPosition The current rook's position.
	 * @param kingDestination The king's destination.
	 * @param rookDestination The rook's destination.
	 */
	protected void addCastling(int kingPosition, int rookPosition, int kingDestination, int rookDestination) {
		defaultMoveAdder.add(kingPosition, kingDestination);
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
	 * @param rookPosition The rook's starting position
	 * @return true if safe. Please note that the king's cell is not checked in this method because the check state is verified before this method is called.
	 */
	protected boolean areCastlingCellsSafe(Color attacker, int kingPosition, int kingDestination, int rookPosition) {
		final AttackDetector attackDetector = board.getAttackDetector();
		if (kingPosition<kingDestination) {
			for (int i = kingPosition+1; i <= kingDestination; i++) {
				if (attackDetector.isAttacked(i, attacker)) {
					return false;
				}
			}
		} else {
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
			generator = promotionAdder;
		} else {
			generator = defaultMoveAdder;
		}
		final int countAllowed = getPawnMaxMoveLength(black, from.getIndex());
		if (black) {
			// Standard moves (no catch)
			addMoves(SOUTH, countAllowed, mv.getPawnNoCatch(), generator);
			// Catches (excluding En-passant)
			addMove(SOUTH_EAST, mv.getPawnCatch(), generator);
			addMove(SOUTH_WEST, mv.getPawnCatch(), generator);
		} else {
			// Standard moves (no catch)
			addMoves(NORTH, countAllowed, mv.getPawnNoCatch(), generator);
			// Catches (excluding En-passant)
			addMove(NORTH_EAST, mv.getPawnCatch(), generator);
			addMove(NORTH_WEST, mv.getPawnCatch(), generator);
		}
	}

	private void addPinnedPawnMoves(Direction pinnedDirection) {
		final boolean black = BLACK == board.getActiveColor();
		// Take care of promotion when generating move
		final int promotionRow = getPromotionRow(black);
		final MoveAdder generator;
		if (board.getCoordinatesSystem().getRow(from.getIndex())==promotionRow) {
			generator = promotionAdder;
		} else {
			generator = defaultMoveAdder;
		}
		if (pinnedDirection==SOUTH || pinnedDirection==NORTH) {
			final int countAllowed = getPawnMaxMoveLength(black, from.getIndex());
			// No catch moves are allowed and only them
			addMoves(black ? SOUTH : NORTH, countAllowed, mv.getPawnNoCatch(), generator);
		} else if (pinnedDirection==SOUTH_EAST || pinnedDirection==NORTH_WEST) {
			addMove(black ? SOUTH_EAST : NORTH_WEST, mv.getPawnCatch(), generator);
		} else if (pinnedDirection==SOUTH_WEST || pinnedDirection==NORTH_EAST) {
			addMove(black ? SOUTH_WEST : NORTH_EAST, mv.getPawnCatch(), generator);
		}
	}

	private void addEnPassantMoves(boolean ignoreKingSafety) {
		final boolean black = BLACK == board.getActiveColor();
		to.reset(board.getEnPassant());
		if (black) {
			// Catches (excluding En-passant)
			addEnPassantMove(NORTH_EAST, BLACK_PAWN, ignoreKingSafety);
			addEnPassantMove(NORTH_WEST, BLACK_PAWN, ignoreKingSafety);
		} else {
			// Catches (excluding En-passant)
			addEnPassantMove(SOUTH_EAST, WHITE_PAWN, ignoreKingSafety);
			addEnPassantMove(SOUTH_WEST, WHITE_PAWN, ignoreKingSafety);
		}
	}
	
	private void addEnPassantMove(Direction d, Piece expectedPiece, boolean ignoreKingSafety) {
		to.start(d);
		if (to.next() && to.getPiece()==expectedPiece && (ignoreKingSafety || board.isKingSafeAfterMove(to.getIndex(), board.getEnPassant()))) {
			defaultMoveAdder.add(to.getIndex(), board.getEnPassant());
		}
	}
	
	public void addAllMoves(Direction direction, BiPredicate<BoardExplorer, BoardExplorer> validator)  {
		to.start(direction);
		while (to.next()) {
			if (validator.test(from, to)) {
				defaultMoveAdder.add(from.getIndex(), to.getIndex());
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
				moveGenerator.add(from.getIndex(), to.getIndex());
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
			moveGenerator.add(from.getIndex(), to.getIndex());
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
		if (getLegalMoves().isEmpty()) {
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
		// Check promotion is valid
		final Piece promotion = move.getPromotion();
		// Reject promotions if moving piece is not a pawn or promotion is king or pawn or
		// has the wrong color or has not the right destination row
		if (promotion!=null && (piece.getKind()!=PAWN || promotion.getKind()==PAWN || promotion.getKind()==KING
				|| promotion.getColor()!=activeColor || getPromotionRow(activeColor==BLACK)!=board.getCoordinatesSystem().getRow(src))) {
			return false;
		}
		final int target = move.getTo();
		init();
		this.to.reset(src);
		final boolean isCheck = board.isCheck();
		final Piece caught = board.getPiece(target);
		if (piece.getKind()==KING) {
			final Castling castling = board.getCastling(src, target);
			if (castling!=null) {
				if (isCheck || !board.hasCastling(castling)) {
					return false;
				}
				final int kingDestination = board.getKingDestination(castling);
				final int rookPosition = board.getInitialRookPosition(castling);
				final int rookDestination = kingDestination + castling.getSide().getRookOffset();
				this.from.reset(src);
				return areCastlingCellsFree(this.from, kingDestination, rookPosition, rookDestination) &&
						areCastlingCellsSafe(board.getActiveColor().opposite(), src, kingDestination, rookPosition);
			} else {
				return !catchOwnPiece(caught) && !board.isAttacked(target, activeColor.opposite()) && isReachable(KING.getDirections(), target, 1)!=null;
			}
		} else {
			// Test that position is reachable
			final Direction direction = isReachable(piece, src, target, caught);
			if (catchOwnPiece(caught) || direction==null) {
				return false;
			}
			// Test piece is not pinned
			final Direction pinnedDirection = board.getPinnedDetector().apply(src);
			final boolean notPinned = pinnedDirection==null || pinnedDirection==direction || pinnedDirection.getOpposite()==direction;
			final boolean kingSafetyCheck = isCheck || (PAWN==piece.getKind() && target==board.getEnPassant());
			return notPinned && (!kingSafetyCheck || board.isKingSafeAfterMove(src, target));
		}
	}
	
	private boolean catchOwnPiece(Piece caught) {
		// Check piece does not catch its own color piece
		return caught!=null && caught.getColor()==board.getActiveColor();
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
