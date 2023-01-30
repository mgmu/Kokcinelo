package fr.formiko.kokcinelo;

import fr.formiko.kokcinelo.model.AcidDrop;
import fr.formiko.kokcinelo.model.Ant;
import fr.formiko.kokcinelo.model.Aphid;
import fr.formiko.kokcinelo.model.Creature;
import fr.formiko.kokcinelo.model.GameState;
import fr.formiko.kokcinelo.model.Ladybug;
import fr.formiko.kokcinelo.model.Level;
import fr.formiko.kokcinelo.model.MapItem;
import fr.formiko.kokcinelo.tools.Files;
import fr.formiko.kokcinelo.tools.Musics;
import fr.formiko.kokcinelo.view.GameScreen;
import fr.formiko.kokcinelo.view.MenuScreen;
import fr.formiko.kokcinelo.view.VideoScreen;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * {@summary Controller is the link between View &#38; model.}
 * Because of Seen2D Actor, there is some view item in the model.
 * 
 * @author Hydrolien
 * @version 1.0
 * @since 0.1
 */
public class Controller {
    private GameState gs;
    private App app;
    private boolean spectatorMode;
    private Level level;
    private Set<Creature> toRemove;

    private static Controller controller;

    // CONSTRUCTORS --------------------------------------------------------------
    /**
     * {@summary Main constructor.}
     * 
     * @param app app to send action to
     */
    public Controller(App app) {
        this.app = app;
        controller = this;
        toRemove = new HashSet<Creature>();
        App.log(0, "constructor", "new Controller: " + toString());
    }

    // GET SET -------------------------------------------------------------------
    public static Controller getController() { return controller; }
    public static void setController(Controller controller) { Controller.controller = controller; }
    public GameScreen getGameScreen() { return (GameScreen) getScreen(); }
    public Screen getScreen() { return app.getScreen(); }
    public void setScreen(Screen screen) { app.setScreen(screen); }
    public int getLocalPlayerId() { return gs.getLocalPlayerId(); }
    public boolean isSpectatorMode() { return spectatorMode; }
    public void setSpectatorMode(boolean spectatorMode) { this.spectatorMode = spectatorMode; }
    public int getNumberOfAphids() { return gs.getAphids().size(); }
    public Level getLevel() { return level; }
    public String getLevelId() { return level.getId(); }
    public void addScore(int bonusScore) { gs.getPlayer(getLocalPlayerId()).addScoreForLadybug(-bonusScore); }
    public Creature getPlayerCreature() { return gs.getPlayerCreature(getLocalPlayerId()); }

    // FUNCTIONS -----------------------------------------------------------------

    /**
     * {@summary Call every function that need to be call every frame.}
     * Some function may do nothing if they are not needed.
     */
    public void playAFrame() {
        movePlayer(getLocalPlayerId());
        moveAICreature();
        interact();
        gs.remove(toRemove);
        toRemove.clear();
    }

    public void startApp() { createNewMenuScreen(); }

    private void createNewVideoScreen() { setScreen(new VideoScreen(getLevelId())); }
    /**
     * {@summary Create a new Menu Screen &#38; start music.}
     */
    public void createNewMenuScreen() {
        setScreen(new MenuScreen());
        Musics.playMenuMusic();
    }
    /**
     * {@summary End the current screen.}
     * Current screen is supposed to be a MenuScreen. Other wise it will do nothing.
     */
    public synchronized void endMenuScreen() {
        if (getScreen() != null && getScreen() instanceof MenuScreen) {
            Screen toDispose = getScreen();
            level = ((MenuScreen) (getScreen())).getLevel();
            createNewVideoScreen();
            toDispose.dispose();
        } else {
            App.log(0, "", "getScreen() is not a MenuScreen");
        }
    }
    /**
     * {@summary End the current screen.}
     * Current screen is supposed to be a VideoScreen. Other wise it will do nothing.
     */
    public synchronized void endVideoScreen() {
        if (getScreen() != null && getScreen() instanceof VideoScreen) {
            Screen toDispose = getScreen();
            createNewGame();
            toDispose.dispose();
        } else {
            App.log(0, "", "getScreen() is not a VideoScreen");
        }
    }

    /**
     * {@summary End the current screen.}
     * Current screen is supposed to be a GameScreen. Other wise it will do nothing.
     */
    public synchronized void endGameScreen() {
        if (getScreen() != null && getScreen() instanceof GameScreen) {
            Screen toDispose = getScreen();
            createNewMenuScreen();
            toDispose.dispose();
        } else {
            App.log(0, "", "getScreen() is not a GameScreen");
        }
    }

    /**
     * {@summary Update zoom of camera.}
     * 
     * @param amountY zoom amount
     */
    public void addZoom(float amountY) {
        amountY *= -1;
        // getCamera().zoom += amountY * 0.05f;
        getCamera().zoom *= (1 - amountY / 20);
        if (getCamera().zoom < 0.1f) {
            getCamera().zoom = 0.1f;
        }
    }

    /**
     * {@summary Move the player Creature &#38; synchonize Camera and masks.}
     * 
     * @param playerId id of the player to move
     */
    public void movePlayer(int playerId) {
        Creature c = gs.getPlayerCreature(playerId);
        c.moveFront();
        c.moveIn(gs.getMapWidth(), gs.getMapHeight());
        // synchonize things that depend of c position
        synchronizeCamera(c);

        if (gs.getMapActorFg() != null) {
            gs.getMapActorFg().setX(c.getCenterX() - gs.getMapActorFg().getWidth() / 2);
            gs.getMapActorFg().setY(c.getCenterY() - gs.getMapActorFg().getHeight() / 2);
            gs.getMapActorFg().setVisible(!isSpectatorMode());
        }
        c.goTo(getVectorStageCoordinates(Gdx.input.getX(), Gdx.input.getY()));
    }

    /**
     * {@summary Move all AI Creature.}
     */
    public void moveAICreature() {
        Creature playerCreature = getPlayerCreature();
        // Iterate over all Creatures in one loop
        for (Creature c : allCreatures()) {
            if (!c.equals(playerCreature)) {
                c.moveAI(gs);
            }
        }
    }

    /**
     * {@summary Synchronize the camera with the given Creature.}
     * 
     * @param c creature to synchronize with
     */
    public void synchronizeCamera(Creature c) {
        getCamera().position.x = c.getCenterX();
        getCamera().position.y = c.getCenterY();
    }
    /**
     * {@summary Create a new Game.}
     * Create the GameState with Game data.
     * Set current Screen as a new GameScreen.
     */
    private void createNewGame() {
        App.log(0, "Need to start new Game");
        int gameTime = 60;
        setSpectatorMode(false);
        Musics.setMusic("Waltz of the Night 1min");
        switch (getLevelId()) {
        case "1K":
            gs = GameState.builder().setAphidNumber(100).setLadybugNumber(1).setMapHeight(2000).setMapWidth(2000).setLevel(getLevel())
                    .build();
            break;
        case "2K":
            gs = GameState.builder().setAphidNumber(100).setLadybugNumber(1).setRedAntNumber(3).setMapHeight(2000).setMapWidth(2000)
                    .setLevel(getLevel()).build();
            break;
        case "2F":
            gs = GameState.builder().setAphidNumber(100).setLadybugNumber(2).setRedAntNumber(1).setMapHeight(2000).setMapWidth(2000)
                    .setLevel(getLevel()).build();
            break;
        case "3K":
            gs = GameState.builder().setAphidNumber(100).setLadybugNumber(1).setGreenAntNumber(3).setMapHeight(2000).setMapWidth(2000)
                    .setLevel(getLevel()).build();
            break;
        case "3F":
            gs = GameState.builder().setAphidNumber(100).setLadybugNumber(2).setGreenAntNumber(1).setMapHeight(2000).setMapWidth(2000)
                    .setLevel(getLevel()).build();
            break;
        default:
            App.log(3, "levelId not found, use default levelId (1K)");
            gs = GameState.builder().setAphidNumber(100).setLadybugNumber(1).setMapHeight(2000).setMapWidth(2000).setLevel(getLevel())
                    .build();
            break;
        }
        app.setScreen(new GameScreen(app));
        Musics.play();
        App.log(1, "start new Game");
        // app.getGameMusic().setPosition(178.1f - gameTime); // end at 178
        getGameScreen().resume();
        getGameScreen().setGameTime(gameTime);
        App.log(1, "new Game started");
    }
    public void restartGame() { createNewGame(); }
    public void updateActorVisibility(int playerId) { gs.updateActorVisibility(playerId, spectatorMode); }
    public Iterable<Creature> allCreatures() { return gs.allCreatures(); }
    public Iterable<Actor> allActors() { return gs.allActors(); }
    public boolean isAllAphidGone() { return gs.isAllAphidGone(); }
    public boolean isAllLadybugGone() { return gs.isAllLadybugGone(); }
    // public void removeActorFromStage(Actor actor) { actor.remove(); }

    /**
     * {@summary Let Creature interact with each other.}
     * It let ladybugs eat aphids and if they do, update player score &#38; play matching sound.
     */
    public void interact() {
        if (ladybugsEat()) {
            getGameScreen().setPlayerScore(gs.getScore());
        }
        antsHit();
        antsShoot();
        acidDropsHit();
    }

    /**
     * {@summary Let ladybugs eat aphids.}
     * 
     * @return true if a ladybug have interact
     */
    public boolean ladybugsEat() {
        boolean haveInteract = false;
        for (Ladybug ladybug : gs.getLadybugs()) {
            for (Aphid aphid : gs.getAphids()) {
                if (ladybug.hitBoxConnected(aphid)) {
                    haveInteract = true;
                    playSound("crock", ladybug);
                    toRemove.add(aphid);
                    // ladybug.addScorePoints(aphid.getGivenPoints());
                    gs.getPlayer(getLocalPlayerId()).addScoreForLadybug(aphid.getGivenPoints());
                    // System.out.println("Eating " + aphid);
                }
            }
        }
        return haveInteract;
    }
    /**
     * {@summary Let ant hit ladybug.}
     * 
     * @return true if a ladybug have interact
     */
    public boolean antsHit() {
        boolean haveInteract = false;
        for (Ant ant : gs.getAnts()) {
            for (Ladybug ladybug : gs.getLadybugs()) {
                if (ant.hitBoxConnected(ladybug)) {
                    if (ant.canHit()) {
                        haveInteract = true;
                        playSound("hit", ant);
                        ant.hit(ladybug);
                        if (ladybug.getLifePoints() < 0f) {
                            toRemove.add(ladybug);
                        }
                    }
                    break;
                }
            }
        }
        return haveInteract;
    }
    /**
     * {@summary Let ant shoot ladybug.}
     * 
     * @return true if a ladybug have interact
     */
    public boolean antsShoot() {
        boolean haveInteract = false;
        for (Ant ant : gs.getAnts()) {
            Ladybug target = (Ladybug) ant.closestCreature(gs.getLadybugs());
            if (target != null && ant.canShoot()) {
                ant.shoot(target);
                playSound("shoot", ant);
                // Create new acid drop
                AcidDrop ad = new AcidDrop(ant.getCenterX(), ant.getCenterY(), ant.getRotation(), ant.distanceTo(target),
                        ant.getShootPoints());
                gs.getAcidDrops().add(ad);
                getGameScreen().getStage().addActor(ad.getActor());
                haveInteract = true;
            }
        }
        return haveInteract;
    }

    /**
     * {@summary Let acid drops hit.}
     * 
     * @return true if an acide drops have interact
     */
    public boolean acidDropsHit() {
        boolean haveInteract = false;
        for (AcidDrop acidDrop : gs.getAcidDrops()) {
            if (acidDrop.getDistanceBeforeHit() < 0) {
                // TODO it should hit any living Creature (not only ladybug)
                for (Ladybug ladybug : gs.getLadybugs()) {
                    if (ladybug.hitBoxConnected(acidDrop)) {
                        haveInteract = true;
                        playSound("splatch", ladybug);
                        // App.log(1, "Acid drop " + acidDrop.getId() + " hit ladybug " + ladybug.getId());
                        acidDrop.hit(ladybug);
                        if (ladybug.getLifePoints() < 0f) {
                            toRemove.add(ladybug);
                        }
                        // App.log(1, "lb have been hit " + ladybug);
                    }
                    toRemove.add(acidDrop);
                }
            }
        }
        return haveInteract;
    }

    /**
     * {@summary End game by launching sound &#38; end game menu.}
     */
    public void gameOver() {
        App.log(1, "gameOver");
        if (getGameScreen().isStop()) {
            return;
        }
        // Musics.dispose();
        setSpectatorMode(true);
        getGameScreen().stopAfterNextDraw();
        // if player play as ant, his score is 0 if he stop game before time is up.
        if (getPlayerCreature() instanceof Ant && getGameScreen().isTimeUp()) {
            gs.setScore(0);
        }
        boolean haveWin = gs.getScore() >= gs.getMaxScore() / 2;
        app.playEndGameSound(haveWin);
        getGameScreen().createEndGameMenu(gs.getScore(), gs.getMaxScore(), haveWin);
        saveScoreInFile();
    }

    /**
     * {@summary Pause game or resume depening of current state.}
     * It pause move of creature & music.
     */
    public void pauseResume() {
        if (getGameScreen().isPause()) {
            removeEscapeMenu();
            getGameScreen().resume();
            Musics.play();
        } else {
            getGameScreen().pause();
            Musics.pause();
            getGameScreen().createEscapeMenu();
        }
    }
    public void removeEscapeMenu() { getGameScreen().removeEscapeMenu(); }

    public void dispose() { app.dispose(); }


    // Files ----------------------------------------------------------------------------------------------
    /**
     * {@summary Save a string in a file.}
     * 
     * @param fileName       name of the file
     * @param contentToWrite content to write into the file
     * @param append         if true, append the content to the file, else overwrite the content of the file
     */
    public void saveStringInFile(String fileName, String contentToWrite, boolean append) {
        App.log(0, "FILES", "Save \"" + contentToWrite + "\" into " + fileName + " append=" + append);
        FileHandle file = Gdx.files.absolute(Files.getDataPath() + fileName);
        file.writeString(contentToWrite, append);
    }
    /**
     * {@summary Read a string from a file.}
     * 
     * @param fileName name of the file
     * @return content of the file
     */
    public String readStringInFile(String fileName) {
        App.log(0, "FILES", "Read the content of " + fileName);
        FileHandle file = Gdx.files.absolute(Files.getDataPath() + fileName);
        if (file.exists()) {
            return file.readString();
        } else {
            return null;
        }
    }
    private static String getScoresFileName() { return "scores.csv"; }
    /**
     * {@summary Save the score of the current game.}
     * It save the levelId, the score and the time of the game.
     */
    public void saveScoreInFile() {
        saveStringInFile(getScoresFileName(), gs.getLevelId() + "," + gs.getPercentScore() + "," + System.currentTimeMillis() + "\n", true);
    }
    /**
     * {@summary Save all important data.}
     */
    public void saveData() {
        Map<String, String> map = App.getDataMap();
        map.put("lastDatePlayed", System.currentTimeMillis() + "");
        long toAddTimePlayed = System.currentTimeMillis() - Long.parseLong(map.get("startPlaying"));
        App.log(1, "FILES", "Added time played: " + toAddTimePlayed);
        long timePlayed;
        if (map.containsKey("timePlayed")) {
            timePlayed = Long.parseLong(map.get("timePlayed"));
        } else {
            timePlayed = 0;
        }
        map.put("timePlayed", "" + (timePlayed + toAddTimePlayed));
        Files.saveInFile("data.yml", map);
        App.log(1, "FILES", "Saved data: " + map);
    }
    /**
     * {@summary Load all important data.}
     */
    public Map<String, String> loadData() {
        Map<String, String> map;
        try {
            map = Files.loadFromFile("data.yml");
        } catch (Exception e) {
            map = new HashMap<String, String>();
            map.put("firstDatePlayed", System.currentTimeMillis() + "");
            String l = Locale.getDefault().getLanguage();
            if (!App.SUPPORTED_LANGUAGE.contains(l)) {
                l = "en";
            }
            map.put("language", l);
        }
        map.put("startPlaying", System.currentTimeMillis() + "");
        App.log(1, "FILES", "Loaded data: " + map);
        return map;
    }
    /**
     * {@summary Return the best score of a level.}
     * 
     * @param levelId id of the level
     * @return best score of the level
     */
    public int getBestScore(String levelId) {
        String scores = readStringInFile(getScoresFileName());
        if (scores == null) {
            return 0;
        }
        int max = 0;
        for (String line : scores.split("\n")) {
            String[] data = line.split(",");
            if (data[0].equals(levelId)) {
                max = Math.max(max, Integer.parseInt(data[1]));
            }
        }
        return max;
    }
    /**
     * {@summary Return the last score of a level.}
     * 
     * @param levelId id of the level
     * @return last score of the level
     */
    public int getLastScore(String levelId) {
        String scores = readStringInFile(getScoresFileName());
        if (scores == null) {
            return 0;
        }
        int score = 0;
        long maxTime = 0;
        for (String line : scores.split("\n")) {
            String[] data = line.split(",");
            if (data[0].equals(levelId)) {
                long time = Long.parseLong(data[2]);
                if (maxTime < time) {
                    maxTime = time;
                    score = Integer.parseInt(data[1]);
                }
            }
        }
        return score;
    }
    /**
     * {@summary Load the unlocked levels.}
     */
    public Set<String> loadUnlockedLevels() {
        HashSet<String> unlockedLevels = new HashSet<String>();
        String scores = readStringInFile(getScoresFileName());
        if (scores == null) {
            unlockedLevels.add("1K");
        } else {
            for (String line : scores.split("\n")) {
                String[] data = line.split(",");
                String levelId = data[0];
                unlockedLevels.add(levelId);
                int num = Integer.parseInt(levelId.substring(0, 1));
                unlockedLevels.add((num + 1) + levelId.substring(1, 2));
                if (num == 1) {
                    unlockedLevels.add((num + 1) + "F");
                }
            }
        }
        App.log(1, "FILES", "Unlocked levels: " + unlockedLevels);
        return unlockedLevels;
    }

    public void playSound(String fileName, MapItem soundSource) {
        Creature soundTarget = getPlayerCreature();
        float volume = Math.max(1f - soundTarget.distanceTo(soundSource) / soundTarget.getHearRadius(), 0f) / 2;
        // float pan = (soundTarget.getActor().getX() - soundSource.getActor().getX()) / soundTarget.getHearRadius();
        float pan = 0f;
        App.log(1, "Sound " + fileName + " " + volume + " " + pan);
        app.playSound(fileName, volume, 0f);
    }


    /**
     * {@summary Return current used camera.}
     * 
     * @return current used camera
     */
    private OrthographicCamera getCamera() {
        // return gScreen.camera;
        return GameScreen.getCamera();
    }
    /**
     * {@summary Return a vector of coordinate from screen x, y to stage x, y.}
     * 
     * @param x x screen coordinate
     * @param y y screen coordinate
     * @return Vector of coordinate from screen x, y to stage x, y
     */
    private Vector2 getVectorStageCoordinates(float x, float y) {
        return getGameScreen().getStage().screenToStageCoordinates(new Vector2(x, y));
    }

}
