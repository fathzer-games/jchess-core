package com.fathzer.jchess.uci;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import com.fathzer.games.perft.PerfTParser;
import com.fathzer.games.perft.PerfTTestData;
import com.fathzer.games.util.Evaluation;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.CoordinatesSystem;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.jchess.fen.FENParser;
import com.fathzer.jchess.generic.BasicEvaluator;
import com.fathzer.plugin.loader.jar.JarPluginLoader;
import com.fathzer.plugin.loader.utils.FileUtils;

public class JChessUCI extends UCI {
	public static void main(String[] args) {
		final UCI uci = new JChessUCI();
		final JarPluginLoader loader = new JarPluginLoader();
		final Path pluginsFolder = Paths.get("uci-plugins");
		if (Files.isDirectory(pluginsFolder)) {
			try {
				final List<Path> jars = FileUtils.getJarFiles(pluginsFolder, 1);
				for (Path jar:jars) {
					final List<Engine> plugins = loader.getPlugins(jar, Engine.class);
					plugins.forEach(uci::add);
				}
			} catch (IOException e) {
				//TODO Log failure when loading plugins 
				e.printStackTrace();
			}
		}
		uci.run();
	}
	
	public JChessUCI() {
		super(new JChessUCIEngine());
		addCommand(this::speedTest, "st");
	}
	
	@Override
	protected Collection<PerfTTestData> readTestData() {
		try (InputStream stream = JChessUCI.class.getResourceAsStream("/Perft.txt")) {
			return new PerfTParser().withStartPositionPrefix("position fen").withStartPositionCustomizer(s -> s+" 0 1").read(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static class MovesAndMore {
		private final JChessEngine engine;
		private CoordinatesSystem cs;
		private String fen;
		private List<Evaluation<Move>> moves;
		
		MovesAndMore(JChessEngine engine, String fen, int size, int accuracy) {
			this.engine = engine;
			fill(fen, size, accuracy);
		}
		
		void fill(String fen, int size, int accuracy) {
			Board<Move> board = FENParser.from(fen);
			this.cs = board.getCoordinatesSystem();
			moves = engine.getBestMoves(board, size, accuracy);
		}

		private void assertEquals(Object expected, Object actual) {
			if (!expected.equals(actual)) {
				show();
				throw new IllegalArgumentException("Expecting "+expected+" but is "+actual);
			}
		}

		private void assertTrue(boolean value) {
			if (!value) {
				show();
				throw new IllegalArgumentException("Expecting true here");
			}
		}
		
		private void show() {
			System.out.println(fen);
			System.out.println(Evaluation.toString(moves, m -> m.toString(cs)));
		}
	}
	
	private void speedTest(String[] args) {
		final long start = System.currentTimeMillis();
		final JChessEngine engine = new JChessEngine(new BasicEvaluator(), 6);
		if (args.length!=0) {
			engine.setParallelism(Integer.parseInt(args[0]));
		}
		
		// 3 possible Mats in 1 with whites
		final MovesAndMore mv = new MovesAndMore(engine, "7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5", Integer.MAX_VALUE, 0);
		mv.assertEquals(6, mv.moves.size());
		int max = mv.moves.get(0).getValue();
		mv.assertEquals(32757, max);
		mv.assertTrue(mv.moves.get(3).getValue()<max);
		mv.moves.stream().limit(3).forEach(m -> mv.assertEquals(max, (int)m.getValue()));

		// Mat in 1 with blacks
		mv.fill("1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5", Integer.MAX_VALUE, 0);
		mv.assertEquals(7, mv.moves.size());
		mv.assertEquals(32757, (int)mv.moves.get(0).getValue());
		Move m = mv.moves.get(0).getContent();
		mv.assertEquals("c3", mv.cs.getAlgebraicNotation(m.getFrom()));
		mv.assertEquals("c2", mv.cs.getAlgebraicNotation(m.getTo()));
		mv.assertEquals(32737,(int)mv.moves.get(1).getValue());
		mv.assertTrue(mv.moves.get(2).getValue()<10000.0);
		
		// Check in 2
		mv.fill("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1", Integer.MAX_VALUE, 0);
		mv.assertEquals(32747, (int)mv.moves.get(0).getValue());
		mv.assertTrue(mv.moves.get(1).getValue()<mv.moves.get(0).getValue());
		m = mv.moves.get(0).getContent();
		mv.assertEquals("b3", mv.cs.getAlgebraicNotation(m.getFrom()));
		mv.assertEquals("a1", mv.cs.getAlgebraicNotation(m.getTo()));
		
		// Check in 2 with blacks
		mv.fill("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1", Integer.MAX_VALUE, 0);
		mv.assertEquals(32747, (int)mv.moves.get(0).getValue());
		mv.assertTrue(mv.moves.get(1).getValue()<mv.moves.get(0).getValue());
		mv.assertEquals("g6", mv.cs.getAlgebraicNotation(mv.moves.get(0).getContent().getFrom()));
		mv.assertEquals("h8", mv.cs.getAlgebraicNotation(mv.moves.get(0).getContent().getTo()));
		
		
		// Check in 3
		mv.fill("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1", 3, 100);
		mv.assertEquals(19, mv.moves.size());
		m = mv.moves.get(0).getContent();
		mv.assertEquals("d1", mv.cs.getAlgebraicNotation(m.getFrom()));
		mv.assertEquals("d7", mv.cs.getAlgebraicNotation(m.getTo()));
		out("completed in "+(System.currentTimeMillis()-start)+"ms");
	}
}
