package com.fathzer.jchess.pgn;

import static com.fathzer.games.Status.*;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.GameHistory;
import com.fathzer.games.GameHistory.TerminationCause;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.jchess.fen.FENUtils;

public abstract class AbstractPGNWriter<M, B extends MoveGenerator<M>> {
	//TODO why DATE_FORMAT is public
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	
	private static record ResultAndMoves (Status status, List<String> anMoves) {
		private String getResult() {
			if (status==DRAW) {
				return "1/2-1/2";
			} else if (status==WHITE_WON) {
				return "1-0";
			} else if (status==BLACK_WON) {
				return "0-1";
			} else {
				return "*";
			}
		}
	}

	public List<String> getPGN(PGNHeaders headers, GameHistory<M, B> history) {
		final List<String> initialPosition = getInitialPosition(headers.getVariant(), history);
		final List<String> result = new LinkedList<>();
		result.add(getField("Event", headers.getEvent()));
		result.add(getField("Site", headers.getSite()));
		result.add(getField("Date", DATE_FORMAT.format(headers.getDate())));
		final Long round = headers.getRound();
		result.add(getField("Round", round==null?"?":round.toString()));
		result.add(getField("White", headers.getWhiteName()));
		result.add(getField("Black", headers.getBlackName()));
		final ResultAndMoves movesAndResult = getMovesAndResult(history);
		result.add(getField("Result", movesAndResult.getResult()));
		result.addAll(initialPosition);
		final TerminationCause termination = history.getTerminationCause();
		if (termination!=TerminationCause.NORMAL) {
			result.add(getField("Termination", termination.toString().replace('_', ' ').toLowerCase()));
		}
		final String timeControl = headers.getTimeControl();
		if (!"?".equals(timeControl)) {
			result.add(getField("TimeControl", headers.getTimeControl()));
		}
		result.add("");
		result.addAll(movesAndResult.anMoves());
		return result;
	}
	
	protected abstract String getFEN(B board);
	
	private List<String> getInitialPosition(String variant, GameHistory<M, B> history) {
		final var fen = getFEN(history.getStartBoard());
		final var fenField = getField("FEN", fen);
		final var setupField = getField("SetUp", "1");
		if (variant!=null) {
			final List<String> result = new LinkedList<>();
			result.add(getField("Variant", variant));
			result.add(setupField);
			result.add(fenField);
			return result;
		} else {
			//TODO remove this use of JChess method in order to put this class in another library/module
			return FENUtils.NEW_STANDARD_GAME.equals(fen) ? Collections.emptyList() : Arrays.asList(setupField, fenField);
		}
	}

	private String getField(String field, String content) {
		return String.format("[%s \"%s\"]",field, content.replace('"', '\''));
	}

	private ResultAndMoves getMovesAndResult(GameHistory<M, B> history) {
		final LinkedList<String> result = new LinkedList<>();
		@SuppressWarnings("unchecked")
		final B board = (B) history.getStartBoard().fork();
		final StringBuilder buf = new StringBuilder();
		int moveNumber = -1;
		for (M move:history.getMoves()) {
			if (getMoveNumber(board)!=moveNumber) {
				if (buf.length()!=0) {
					result.add(buf.toString());
				}
				moveNumber = getMoveNumber(board);
				buf.setLength(0);
				buf.append(moveNumber);
				buf.append(". ");
			} else {
				buf.append(" ");
			}
			buf.append(getAlgebraicNotation(move, board));
		}
		if (buf.length()!=0) {
			result.add(buf.toString());
		}
		return new ResultAndMoves(history.getStatus(), result);
	}
	
	protected abstract int getMoveNumber(B board);
	
	protected abstract String getAlgebraicNotation(M move, B board);
}
