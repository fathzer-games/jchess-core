package com.fathzer.jchess.uci;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ZobristProvider;
import com.fathzer.games.perft.TestableMoveGeneratorSupplier;
import com.fathzer.games.Rules;
import com.fathzer.games.util.PhysicalCores;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.ChessGameState;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.CopyBasedMoveGenerator;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.Piece;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.BasicEvaluator;
import com.fathzer.jchess.generic.StandardChessRules;
import com.fathzer.jchess.standard.CompactMoveList;
import com.fathzer.jchess.uci.option.ComboOption;
import com.fathzer.jchess.uci.option.Option;
import com.fathzer.jchess.uci.option.SpinOption;

public class JChessUCIEngine implements Engine, TestableMoveGeneratorSupplier<Move>, MoveGeneratorSupplier<Move>, MoveToUCIConverter<Move> {
	private static final String BEST_LEVEL = "best";
	private static final String AVERAGE_LEVEL = "average";
	private static final String SILLY_LEVEL = "silly";
	
	private Board<Move> board;
	private JChessEngine engine;
	
	public JChessUCIEngine() {
		engine = new JChessEngine(StandardChessRules.INSTANCE, new BasicEvaluator(), 6);
	}
	
	@Override
	public String getId() {
		return "JChess";
	}
	
	@Override
	public String getAuthor() {
		return "Jean-Marc Astesana (Fathzer)";
	}
	
	@Override
	public Option<?>[] getOptions() {
		return new Option[] {
			new ComboOption("level", this::setLevel, AVERAGE_LEVEL, new LinkedHashSet<>(Arrays.asList(SILLY_LEVEL, AVERAGE_LEVEL, BEST_LEVEL))),
			new SpinOption("thread", this::setParallelism, PhysicalCores.count(), 1, Runtime.getRuntime().availableProcessors())
		};
	}
	
	private void setLevel(String level) {
		final int depth;
		if (SILLY_LEVEL.equals(level)) {
			depth = 4;
		} else if (AVERAGE_LEVEL.equals(level)) {
			depth = 6;
		} else if (BEST_LEVEL.equals(level)) {
			depth = 7;
		} else {
			throw new IllegalArgumentException();
		}
		engine.setDepth(depth);
	}
	
	private void setParallelism(int parallelism) {
		engine.setParallelism(parallelism);
	}
	
	@Override
	public void move(UCIMove move) {
		board.makeMove(toMove(board.getCoordinatesSystem(), move, board.getActiveColor()));
	}
	
	public static Move toMove(CoordinatesSystem cs, UCIMove move, Color color) {
		final int from = cs.getIndex(move.getFrom());
		final int to = cs.getIndex(move.getTo());
		String promotion = move.getPromotion();
		if (promotion!=null && Color.WHITE.equals(color)) {
			// Warning the promotion code is always in lowercase
			promotion = promotion.toUpperCase();
		}
		return toMove(from, to, promotion);
	}
	
	private static Move toMove(int from, int to, String promotionAsFen) {
		final ChessGameState lst = new CompactMoveList();
		if (promotionAsFen==null) {
			lst.add(from, to);
		} else {
			final Optional<Piece> o = Arrays.stream(com.fathzer.jchess.Piece.values()).filter(x->promotionAsFen.equals(x.getNotation())).findAny();
			if (o.isEmpty()) {
				throw new NoSuchElementException(promotionAsFen+" is not a valid piece notation");
			} else {
				lst.add(from, to, o.get());				
			}
		}
		return lst.get(0);
	}

	@Override
	public void setStartPosition(String fen) {
		board = FENParser.from(fen);
	}

	@Override
	public LongRunningTask<UCIMove> go() {
		return new LongRunningTask<>() {
			@Override
			public UCIMove get() {
				final Move best = engine.apply(board);
				board.makeMove(best);
				return toMove(board.getCoordinatesSystem(), best);
			}

			@Override
			public void stop() {
				super.stop();
				engine.interrupt();
			}
		};
	}
	
	private static UCIMove toMove(CoordinatesSystem cs, Move move) {
		final String promotion = move.promotedTo()==null ? null : move.promotedTo().getNotation().toLowerCase();
		return new UCIMove(cs.getAlgebraicNotation(move.getFrom()), cs.getAlgebraicNotation(move.getTo()), promotion);
	}

	@Override
	public MoveGenerator<Move> get() {
		return new MyMoveGenerator(StandardChessRules.PERFT, board);
	}
	
	@Override
	public String toUCI(Move move) {
		return toMove(board.getCoordinatesSystem(), move).toString();
	}

	private static class MyMoveGenerator extends CopyBasedMoveGenerator<Move> implements MoveGenerator<Move>, ZobristProvider {
		public MyMoveGenerator(Rules<Board<Move>, Move> rules, Board<Move> board) {
			super(rules, board);
		}

		@Override
		public long getZobristKey() {
			return getBoard().getKey();
		}
	}

	@Override
	public String getBoardAsString() {
		return board==null ? "no position defined" : board.toString();
	}

	@Override
	public String getFEN() {
		return board==null ? null : FENParser.to(board);
	}
}
