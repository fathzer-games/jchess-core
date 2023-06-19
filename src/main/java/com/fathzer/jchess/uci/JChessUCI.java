package com.fathzer.jchess.uci;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.fathzer.games.perft.PerfTParser;
import com.fathzer.games.perft.PerfTTestData;
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
		addCommand(this::doPerfStat,"perf");
	}
	protected void doPerfStat(String[] tokens) {
		if (! (getEngine() instanceof UCIMoveGeneratorProvider)) {
			debug("perf is not supported by this engine");
		}
		final Optional<Integer> depth = parseInt(tokens, 0, null);
		if (depth.isEmpty()) {
			return;
		}
		final AtomicInteger cutTime = new AtomicInteger(Integer.MAX_VALUE);
		final AtomicInteger parallelism = new AtomicInteger(1);
		Arrays.stream(tokens).skip(1).forEach(token -> {
			if (token.startsWith("c")) {
				cutTime.set(parseInt(token.substring(1)));
			} else if (token.startsWith("p")) {
				parallelism.set(parseInt(token.substring(1)));
			} else {
				debug("Token "+token+" ignored");
			}
		});
		doPerfStat(depth.get(), parallelism.get(), cutTime.get());
	}

	private void doPerfStat(int depth, final int parallelism, int cutTime) {
		final MoveGeneratorSpeedTest2 test = new MoveGeneratorSpeedTest2(readTests());
		final TimerTask task = new TimerTask() {
			@Override
			public void run() {
				doStop(null);
			}
		};
		doBackground(() -> {
			final Timer timer = new Timer();
			timer.schedule(task, 1000L*cutTime);
			try {
				final long start = System.currentTimeMillis();
				long sum = test.run(depth, parallelism, getEngine());
				final long duration = System.currentTimeMillis() - start;
				out("perf: "+f(sum)+" moves in "+f(duration)+"ms ("+f(sum*1000/duration)+" mv/s) (using "+parallelism+" thread(s))");
			} finally {
				timer.cancel();
			}
			
		}, test::cancel);
	}
	
	private int parseInt(String string) {
		return parseInt(new String[] {string}, 0, null).get();
	}
	
	private static String f(long num) {
		return NumberFormat.getInstance().format(num);
	}
	
	private static List<PerfTTestData> readTests() {
		try (InputStream stream = MoveGeneratorSpeedTest.class.getResourceAsStream("/Perft.txt")) {
			return new PerfTParser().withStartPositionPrefix("position fen").read(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
