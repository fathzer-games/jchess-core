package com.fathzer.jchess.pgn;

import static com.fathzer.games.Status.*;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.fen.FENUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class PGNWriter {
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	
	@AllArgsConstructor
	private static class ResultAndMoves {
		private final Board<Move> board;
		@Getter
		private final List<String> anMoves;
		
		private String getResult() {
			final Status status = board.getStatus();
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

	public List<String> getPGN(PGNHeaders headers, GameHistory history) {
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
		result.add("");
		result.addAll(movesAndResult.getAnMoves());
		return result;
	}
	
	private List<String> getInitialPosition(String variant, GameHistory history) {
		if (variant!=null) {
			final List<String> result = new LinkedList<>();
			result.add(getField("Variant", variant));
			result.add(getField("SetUp", "1"));
			result.add(getField("FEN", FENUtils.to(history.getStartBoard())));
			return result;
		} else {
			return Collections.emptyList();
		}
	}

	private String getField(String field, String content) {
		return String.format("[%s \"%s\"]",field, content.replace('"', '\''));
	}

	private ResultAndMoves getMovesAndResult(GameHistory history) {
		final LinkedList<String> result = new LinkedList<>();
		final MoveAlgebraicNotationBuilder an = new MoveAlgebraicNotationBuilder().withPlayMove(true).withEnPassantSymbol("");
		final Board<Move> board = (Board<Move>) history.getStartBoard().fork();
		final StringBuilder buf = new StringBuilder();
		int moveNumber = -1;
		for (Move move:history.getMoves()) {
			if (board.getMoveNumber()!=moveNumber) {
				if (buf.length()!=0) {
					result.add(buf.toString());
				}
				moveNumber = board.getMoveNumber();
				buf.setLength(0);
				buf.append(moveNumber);
				buf.append(". ");
			} else {
				buf.append(" ");
			}
			buf.append(an.get(board, move));
		}
		if (buf.length()!=0) {
			result.add(buf.toString());
		}
		return new ResultAndMoves(board, result);
	}
}
