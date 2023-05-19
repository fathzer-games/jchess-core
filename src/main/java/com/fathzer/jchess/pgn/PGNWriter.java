package com.fathzer.jchess.pgn;

import static com.fathzer.games.Status.*;

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import com.fathzer.games.Rules;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.GameHistory;
import com.fathzer.jchess.Move;

public class PGNWriter {
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

	public List<String> getPGN(PGNHeaders headers, GameHistory history) {
		final LinkedList<String> result = new LinkedList<>();
		result.add(getField("Event", headers.getEvent()));
		result.add(getField("Site", headers.getSite()));
		result.add(getField("Date", DATE_FORMAT.format(headers.getDate())));
		final Long round = headers.getRound();
		result.add(getField("Round", round==null?"?":round.toString()));
		result.add(getField("White", headers.getWhiteName()));
		result.add(getField("Black", headers.getBlackName()));
		result.addAll(getMovesAndResult(history));
		return result;
	}
	
	private String getField(String field, String content) {
		return String.format("[%s \"%s\"]",field, content.replace('"', '\''));
	}

	private List<String> getMovesAndResult(GameHistory history) {
		final LinkedList<String> result = new LinkedList<>();
		final MoveAlgebraicNotation an = new MoveAlgebraicNotation(history.getRules()).withPlayMove(true);
		final Board<Move> board = history.getStartBoard().create();
		board.copy(history.getStartBoard());
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
		result.addFirst("");
		result.addFirst(getField("Result", getResult(history.getRules(), board)));
		return result;
	}
	
	private String getResult(Rules<Board<Move>, Move> rules, Board<Move> board) {
		final Status state = rules.getState(board).getStatus();
		if (state==DRAW) {
			return "1/2-1/2";
		} else if (state==WHITE_WON) {
			return "1-0";
		} else if (state==BLACK_WON) {
			return "1-0";
		} else {
			return "*";
		}
	}
}
