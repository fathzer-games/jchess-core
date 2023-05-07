package com.fathzer.jchess.swing;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.fathzer.games.Color;
import com.fathzer.games.Rules;
import com.fathzer.games.Status;
import com.fathzer.jchess.Board;
import com.fathzer.jchess.Move;
import com.fathzer.jchess.ai.JChessEngine;
import com.fathzer.games.clock.Clock;
import com.fathzer.games.clock.ClockSettings;
import com.fathzer.jchess.fischerrandom.FischerRandomRules;
import com.fathzer.jchess.generic.BasicEvaluator;
import com.fathzer.jchess.generic.StandardChessRules;
import com.fathzer.jchess.swing.GameSettings.ColorSetting;
import com.fathzer.jchess.swing.GameSettings.EngineSettings;
import com.fathzer.jchess.swing.GameSettings.Variant;
import com.fathzer.util.Observable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameSession {
	public enum State {
		PAUSED, RUNNING, ENDED
	}
	
	private final GamePanel panel;
	private Rules<Board<Move>, Move> rules;
	private GameSettings settings;
	private Function<Board<Move>, Move> whiteEngine;
	private Function<Board<Move>, Move> blackEngine;
	private int gameCount;
	private Color player1Color;
	private Observable<State> state;
	private Game game;

	public GameSession(GamePanel panel, GameSettings settings) {
		this.panel = panel;
		this.state = new Observable<>(State.ENDED);
		state.addListener(this::onStateChanged);
		panel.getBoard().addPropertyChangeListener(ChessBoardPanel.TARGET, evt -> onMove());
		panel.setResignationHandler(this::resign);
		setSettings(settings);
		gameCount = 0;
	}
	
	private void firstGame() {
		this.gameCount = 0;
		initGame();
		panel.getBoard().setReverted(Color.BLACK.equals(player1Color));
		setEngine(player1Color, getEngine(settings.getVariant(), settings.getPlayer1().getEngine()));
		setEngine(player1Color.opposite(), getEngine(settings.getVariant(), settings.getPlayer2().getEngine()));
		if (onlyHumans()) {
			panel.getBoard().setUpsideDownColor(player1Color.opposite());
		}
		panel.getBoard().setShowPossibleMoves(settings.isShowPossibleMoves());
		panel.getBoard().setTouchMove(settings.isTouchMove());
	}
	
	private void onStateChanged(State old, State current) {
		log.debug("State changed from {} to {}", old,current);
		if (State.RUNNING.equals(current)) {
			game.start();
		} else {
			game.pause();
		}
		if (SwingUtilities.isEventDispatchThread()) {
			swingStateChanged(current);
		} else {
			SwingUtilities.invokeLater(() -> swingStateChanged(current));
		}
	}
	
	private void swingStateChanged(State current) {
		if (State.RUNNING.equals(current)) {
			nextMove();
		} else {
			panel.getBoard().setManualMoveEnabled(false);
		}
	}
	
	private void doRevenge() {
		final Color previous1 = player1Color;
		if (ColorSetting.RANDOM.equals(settings.getPlayer1Color()) && gameCount%2==0) {
			player1Color = settings.getPlayer1Color().getColor();
		} else {
			player1Color = player1Color.opposite();
		}
		gameCount++;
		panel.getBoard().setReverted(Color.BLACK.equals(player1Color));
		if (!previous1.equals(player1Color)) {
			// Switch engines color
			Function<Board<Move>, Move> dummy = whiteEngine;
			whiteEngine = blackEngine;
			blackEngine = dummy;
			if (onlyHumans()) {
				// Change upside down color
				panel.getBoard().setUpsideDownColor(player1Color.opposite());
			}
		}
		initGame();
		setState(State.PAUSED);
		start();
	}
	
	private void initGame() {
		this.game = new Game(rules.newGame(), buildClock());
		this.game.setStartClockAfterFirstMove(settings.isStartClockAfterFirstMove());
		panel.setPlayer1Color(player1Color);
		panel.setClock(game.getClock());
		panel.getBoard().setBoard(game.getBoard());
	}

	private Clock buildClock() {
		if (settings.getClock()!=null) {
			final ClockSettings extraWhite;
			final ClockSettings extraBlack;
			if (Color.WHITE.equals(player1Color)) {
				extraWhite = settings.getPlayer1().getExtraClock();
				extraBlack = settings.getPlayer2().getExtraClock();
			} else {
				extraBlack = settings.getPlayer1().getExtraClock();
				extraWhite = settings.getPlayer2().getExtraClock();
			}
			final ClockSettings common = settings.getClock();
			final Clock clock = new Clock(extraWhite==null?common:extraWhite, extraBlack==null?common:extraBlack);
			clock.addStatusListener(this::timeUp);
			clock.addClockListener(e -> log.debug("Clock {} state changes from {} to {}",e.getClock(), e.getPreviousState(), e.getNewState()));
			if (settings.isStartClockAfterFirstMove()) {
				clock.withStartingColor(Color.BLACK);
			}
			return clock;
		} else {
			return null;
		}
	}
	
	public Function<Board<Move>, Move> getEngine(Variant variant, EngineSettings settings) {
		final JChessEngine engine;
		if (settings==null) {
			engine = null;
		} else if (Variant.STANDARD.equals(variant)) {
			engine = new JChessEngine(StandardChessRules.INSTANCE, new BasicEvaluator(), settings.getLevel());
		} else if (Variant.CHESS960.equals(variant)) {
			engine = new JChessEngine(FischerRandomRules.INSTANCE, new BasicEvaluator(), settings.getLevel());
			engine.setOpenings(null);
		} else {
			throw new IllegalArgumentException("The "+this+" variant does not support engine");
		}
		return engine;
	}

	private boolean onlyHumans() {
		return this.settings.getPlayer1().getEngine()==null && this.settings.getPlayer2().getEngine()==null;
	}
	
	public void addListener(BiConsumer<State,State> listener) {
		this.state.addListener(listener);
	}
	
	public void setEngine(Color color, Function<Board<Move>, Move> engine) {
		if (Color.WHITE.equals(color)) {
			this.whiteEngine = engine;
		} else if (Color.BLACK.equals(color)) {
			this.blackEngine = engine;
		}
	}
	
	private Function<Board<Move>, Move> getEngine(Color color) {
		if (Color.WHITE.equals(color)) {
			return this.whiteEngine;
		} else if (Color.BLACK.equals(color)) {
			return this.blackEngine;
		} else {
			throw new NullPointerException();
		}
	}
	
	public void start() {
		if (State.ENDED.equals(getState())) {
			firstGame();
		}
		setState(State.RUNNING);
	}

	private void nextMove() {
		final Color activeColor = game.getBoard().getActiveColor();
		final Function<Board<Move>, Move> engine = getEngine(activeColor);
		if (engine!=null) {
			log.debug("Engine detected for {}",activeColor);
			panel.getBoard().setManualMoveEnabled(false);
			game.playEngine(engine, this::play);
		} else {
			panel.getBoard().setManualMoveEnabled(true);
		}
	}
	
	private void play(Game game, Move move) {
		SwingUtilities.invokeLater(() -> {
			// WARNING: If a revenge is launched before the engine returns its choice, state can be RUNNING again
			// and the move would be transmitted to the panel if we omitted to check we are still in the same game!
			if (move!=null && game==this.game && State.RUNNING.equals(getState())) {
				log.debug("Transmitting {}'s move {} to panel's board",game.getBoard().getActiveColor(),move);
				panel.getBoard().doMove(move);
			} else {
				log.debug("Ignore move {}, state is {}", move, getState());
			}
		});
	}

	
	public State getState() {
		return this.state.getValue();
	}
	
	public void setState(State state) {
		this.state.setValue(state);
	}

	private void onMove() {
		panel.repaint();
		final Status status = panel.getBoard().getGameState().getStatus();
		if (!Status.PLAYING.equals(status)) {
			// Game is ended
			endOfGame(status);
		} else {
			this.game.onMove();
			nextMove();
		}
	}

	/** This method is called when clock emits a time up event.
	 * <br>Please note that this method could be invoked on a thread that is not the Swing event thread. 
	 * @param status The game status.
	 */
	private void timeUp(Status status) {
		SwingUtilities.invokeLater(()->doTimeUp(status));
	}
	
	private void doTimeUp(Status status) {
		this.setState(State.PAUSED);
		if (JOptionPane.showConfirmDialog(panel, getMessage(status)+" Do you want to continue game without clock?","Time is up",JOptionPane.YES_NO_OPTION)==0) {
			this.setState(State.RUNNING);
		} else {
			endOfGame(status);
		}
	}
	
	private void resign(Color color) {
		if (!getState().equals(State.RUNNING)) {
			return;
		}
		setState(State.PAUSED);
		if (game.getBoard().getActiveColor().equals(color) && JOptionPane.showConfirmDialog(panel, "Are you sure you want to resign?","Resignation",JOptionPane.YES_NO_OPTION)==0) {
			final Status status = Color.WHITE.equals(color) ? Status.BLACK_WON : Status.WHITE_WON;
			endOfGame(status);
		} else {
			setState(State.RUNNING);
		}
	}

	private void endOfGame(final Status status) {
		setState(State.PAUSED);
		log.debug("End of game,  state: {}", getState());
		final String revenge = "Revenge";
		int choice = JOptionPane.showOptionDialog(panel, getMessage(status), "End of game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {revenge,"Enough for today"}, revenge);
		if (choice==0) {
			doRevenge();
		} else {
			setState(State.ENDED);
		}
	}
	
	private String getMessage(Status status) {
		if (Status.DRAW.equals(status)) {
			return "Draw!";
		} else if (Status.BLACK_WON.equals(status)) {
			return "Black wins!";
		} else if (Status.WHITE_WON.equals(status)) {
			return "White wins!";
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public void stop() {
		setState(State.PAUSED);
	}

	public void setSettings(GameSettings settings) {
		if (!State.ENDED.equals(getState())) {
			throw new IllegalStateException("Can't change the game settings dutring the game");
		}
		this.settings = settings;
		this.rules = settings.getVariant().getRules();
		panel.getBoard().setChessRules(rules);
		panel.setPlayer1Human(settings.getPlayer1().getEngine()==null);
		panel.setPlayer2Human(settings.getPlayer2().getEngine()==null);
		player1Color = settings.getPlayer1Color().getColor();
		initGame();
	}
}
