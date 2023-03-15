package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class MinesweeperApp extends Application {
	
	static final int TILE_SIZE = 40;
	static int X_TILES = 9;
	static int Y_TILES = 9;
	
	static int W = X_TILES * TILE_SIZE;
	static int H = Y_TILES * TILE_SIZE;
	
	
	static Tile[][] grid = new Tile[X_TILES][Y_TILES];
	
	static int DIFFICULTY = 1;
	static int MINES = 9;
	static int SECONDS = 120;
	static int HYPERMINE = 0;
	
	BorderPane startScene;
	static Scene scene;
	static Scene gameScene;
	static Label topBar;
	static Timeline timeline;
	static Button restartButton;
	Button closeDetailsButton;
	Button cancelCreateButton;
	Button confirmCreateButton;
	Button cancelLoadButton;
	Button confirmLoadButton;
	Button chooseFileButton;
	static Popup overPopup;
	static Popup roundsPopup;
	static Popup createPopup;
	static Popup loadPopup;
	
	static int triesOpen, marked, seconds_left, tilesToOpen, gameIter=0;
	static boolean victory, solved, clicked, ingame;
	
	static String game0[] = {"", "", "", "", ""};
	static String game1[] = {"", "", "", "", ""};
	static String game2[] = {"", "", "", "", ""};
	static String game3[] = {"", "", "", "", ""};
	static String game4[] = {"", "", "", "", ""};
	
	static TextField scenarioID = new TextField("SCENARIO-ID");
	static TextField difficulty = new TextField("Difficulty (1 or 2)");
	static TextField minesNum = new TextField("Number of mines");
	static TextField hasHyper = new TextField("Hypermine? (y/n)");
	static TextField timeAvail = new TextField("Time Available (in seconds)");
	
	static FileChooser filechooser = new FileChooser();
	static File selectedFile;
	
	MenuBar menubar;
	Menu application, details;
	MenuItem createGame, loadGame, startGame, exitGame, rounds, solution;
	
	private BorderPane createContent() {
		
		ingame = true;
		victory = true;
		solved = false;
		clicked = true;
		triesOpen = 0; //tracks successful tries
		marked = 0;
		seconds_left = SECONDS;
		
		X_TILES = DIFFICULTY==1 ? 9 : 16;
		Y_TILES = DIFFICULTY==1 ? 9 : 16;
		grid = new Tile[X_TILES][Y_TILES];
		
		W = X_TILES * TILE_SIZE;
		H = Y_TILES * TILE_SIZE;
		
		tilesToOpen = X_TILES*Y_TILES - MINES;
		
		
		BorderPane borderpane = new BorderPane();
		
		borderpane.setTop(menubar); //menubar
		
		topBar = new Label("Mines: " + MINES + "   Marked Tiles: " + marked + "   Time Left: " + String.format("%02d:%02d", seconds_left / 60, seconds_left % 60));
		borderpane.setCenter(topBar);
		
		
		timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			seconds_left--;
			if(seconds_left <= 0) { //game over
				timeline.stop();
				gameOver("time");
			}
			topBar.setText("Mines: " + MINES + "   Marked Tiles: " + marked + "   Time Left: " + String.format("%02d:%02d", seconds_left / 60, seconds_left % 60));
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
		
		
		Pane board = new Pane(); //gameboard
		board.setPrefSize(W, H);
		
		
		for(int y = 0; y < Y_TILES; y++) {
			for(int x = 0; x < X_TILES; x++) {
				Tile tile = new Tile(x, y, false, false);
					
				grid[x][y] = tile;
				board.getChildren().add(tile);
			}
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("./medialab/mines.txt"));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		int m = MINES;
		if(HYPERMINE==1) m--;
		while(m > 0) {
			int randomX = ThreadLocalRandom.current().nextInt(0, X_TILES);
			int randomY = ThreadLocalRandom.current().nextInt(0, Y_TILES);
			
			if(grid[randomX][randomY].hasMine) continue;
			else {
				grid[randomX][randomY].hasMine = true;
				grid[randomX][randomY].text.setText("X");
				m--;
				
				//write to mines.txt
				try {
					writer.write(String.valueOf(randomX) + "," + String.valueOf(randomY) + ",0\n");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		
		int randomX, randomY;
		if(HYPERMINE==1) {
			do {
				randomX = ThreadLocalRandom.current().nextInt(0, X_TILES);
				randomY = ThreadLocalRandom.current().nextInt(0, Y_TILES);
			}
			while(grid[randomX][randomY].hasMine);
			grid[randomX][randomY].hasHyperMine = true;
			grid[randomX][randomY].hasMine = true; //hypermine is a mine
			grid[randomX][randomY].text.setText("H");
			
			//update mines.txt
			try {
				writer.write(String.valueOf(randomX) + "," + String.valueOf(randomY) + ",1\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for(int y = 0; y < Y_TILES; y++) {
			for(int x = 0; x < X_TILES; x++) {
				Tile tile = grid[x][y];
				
				if(tile.hasMine) continue;
					
				long mines = getNeighbours(tile).stream().filter(t -> t.hasMine).count();
				
				if(mines > 0) 
					tile.text.setText(String.valueOf(mines));
			}
		}
		
		borderpane.setBottom(board);
		
		return borderpane;
	}
	
	private Label historyFormat(String game[], int gameIndex) {
		Label gameLabel = new Label("Game " + gameIndex + "  Mines: " + game[0] + "  Tries: " + game[1] 
				+ "  Time Played: " + game[2] + "  Winner: " + game[3]);
		return gameLabel;
	}
	
	private static void historyHelp(String game[]) {
		game[0] = String.valueOf(MINES); //Number of Mines
		if(triesOpen!=0) triesOpen--;
		game[1] = String.valueOf(triesOpen); //Successful tries
		game[2] = String.format("%02d:%02d", (SECONDS - seconds_left) / 60, (SECONDS - seconds_left) % 60); //Time played
		if(victory) 
			game[3] = "Player";
		else game[3] = "Computer";
		//game[3] = String.valueOf(victory); //Victory(Player) or Defeat(Computer) (true or false)
	}
	
	//called after ending a game successfully
	private static void addHistory() {
		switch(gameIter) {
			case 1: 
				historyHelp(game1);
				break;
			case 2: 
				historyHelp(game2);
				break;
			case 3: 
				historyHelp(game3);
				break;
			case 4: 
				historyHelp(game4);
				break;
			default: 
				historyHelp(game0);
				gameIter = 0;
		}
		
		gameIter++;
	}
	
	public static void gameOver(String cause) {
		//if(!victory) return; ////////??????????
		if(!ingame) return;
		
		ingame = false;
		
		timeline.stop();
		victory = false;
		
		overPopup = new Popup();
		Label causeLabel = new Label();
		
		if(cause=="solution") {
			solved = true;
			addHistory();
		}
		else {
			if(cause=="victory") {
				causeLabel = new Label("Congrats! You won!");
				victory = true;
			}
			else if(cause=="mine") 
				causeLabel = new Label("Oops you clicked on a mine!");
			else if(cause=="time") 
				causeLabel = new Label("Time out!");
			
			addHistory();
			
			VBox gameOverPane = new VBox(causeLabel, restartButton);
			gameOverPane.setAlignment(Pos.BASELINE_CENTER);
			
			StackPane content = new StackPane(new Rectangle(300,70,Color.ALICEBLUE), gameOverPane);
			
			overPopup.getContent().add(content);
			overPopup.setAutoHide(true);
			overPopup.show(gameScene.getWindow());
		}
	}
	
	private int[] validateScenario(String scenario) throws InvalidDescriptionException, InvalidValueException {
		//System.out.println("validating " + scenario);
		int[] conf = {0,0,0,0};
		try {
			BufferedReader reader = new BufferedReader(new FileReader("./medialab/" + scenario));
			
			int diff = Integer.parseInt(reader.readLine());
			int mines = Integer.parseInt(reader.readLine());
			int secs = Integer.parseInt(reader.readLine());
			int hypMine = Integer.parseInt(reader.readLine());
			reader.close();
			
			//System.out.println(String.valueOf(diff) + " " + String.valueOf(mines) + 
			//		" " + String.valueOf(secs) + " " + String.valueOf(hypMine));
			
			conf[0] = diff;
			conf[1] = mines; 
			conf[2] = secs;
			conf[3] = hypMine; 
			
			switch(diff) {
				case(1):
					if(mines < 9 || mines > 11) 
						throw new InvalidValueException("Number of mines must be between 9-11 for this difficulty!");
					if(secs < 120 || secs > 180)
						throw new InvalidValueException("Available seconds must be between 120-180 for this difficulty!");
					if(hypMine != 0)
						throw new InvalidValueException("Hypermine is not available for this difficulty!");
					break;
					
				case(2):
					if(mines < 35 || mines > 45) 
						throw new InvalidValueException("Number of mines must be between 35-45 for this difficulty!");
					if(secs < 240 || secs > 360)
						throw new InvalidValueException("Available seconds must be between 240-360 for this difficulty!");
					if(hypMine != 0 && hypMine!= 1)
						throw new InvalidValueException("Invalid hypermine value!");
					break;
					
				default:
					throw new InvalidValueException("Value for difficulty must be 1 or 2!");
			
			}
			
		} catch (NumberFormatException e) {
			throw new InvalidDescriptionException("Please choose a valid file format.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return conf;
	}
	

	
	public static List<Tile> getNeighbours(Tile tile) {
		List<Tile> neighbours = new ArrayList<>();
		
		int[] points = new int[] {
				-1, -1, //top left
				-1,  0, //left
				-1,  1, //bottom left
				 0, -1, //top
				 0,  1, //bottom
				 1, -1, //top right
				 1,  0, //right
				 1,  1, //bottom right		
		};
		
		for(int i = 0; i < points.length; i++) {
			int dx = points[i];
			int dy = points[++i];
			
			int newX = tile.x + dx;
			int newY = tile.y + dy;
			
			if(newX >= 0 && newX < X_TILES && newY >= 0 && newY < Y_TILES) {
				neighbours.add(grid[newX][newY]);
			}
		}
		
		return neighbours;
	} 
	
	@Override
	public void start(Stage stage) throws Exception {
		ingame = false;
		
		//closeRoundsButton
		closeDetailsButton = new Button("Close");
		closeDetailsButton.setOnAction(event -> {
			roundsPopup.hide();
			});
		
		//cancelCreateButton
		cancelCreateButton = new Button("Cancel");
		cancelCreateButton.setOnAction(event -> {
			scenarioID.setText("SCENARIO-ID");
			difficulty.setText("Difficulty (1 or 2)");
			minesNum.setText("Number of mines");
			hasHyper.setText("Hypermine? (y/n)");
			timeAvail.setText("Time Available (in seconds)");
			
			createPopup.hide();
			});
		
		//confirmCreateButton
		confirmCreateButton = new Button("Confirm");
		confirmCreateButton.setOnAction(event -> {
			//create a file with name scenarioID
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter("./medialab/" + scenarioID.getText() + ".txt"));
				if(hasHyper.getText().equals("y")) 
					hasHyper.setText("1");
				else if(hasHyper.getText().equals("n"))
					hasHyper.setText("0");
				else
					hasHyper.setText("invalid");
					

				writer.write(difficulty.getText() + "\n" + minesNum.getText() + "\n" + timeAvail.getText() + "\n" + 
				hasHyper.getText());
				
				writer.close();
				
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			scenarioID.setText("SCENARIO-ID");
			difficulty.setText("Difficulty (1 or 2)");
			minesNum.setText("Number of mines");
			hasHyper.setText("Hypermine? (y/n)");
			timeAvail.setText("Time Available (in seconds)");
			
			createPopup.hide();
		});
		
		//chooseFileButton
		chooseFileButton = new Button("Choose SCENARIO-ID");
		chooseFileButton.setOnAction(event -> {
			filechooser.setTitle("Choose SCENARIO-ID");
			filechooser.setInitialDirectory(new File("./medialab"));
			filechooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
			selectedFile = filechooser.showOpenDialog(loadPopup);
		});
		
		//confirmLoadButton
		confirmLoadButton = new Button("Confirm");
		confirmLoadButton.setOnAction(event -> {
			if(selectedFile!=null) {
				int[] configurations = null;
				try {
					configurations = validateScenario(selectedFile.getName());
				}
				catch(InvalidDescriptionException e) {
					//System.out.print(e.errorMessage);
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Invalid Description Alert");
					alert.setHeaderText(null);
					alert.getDialogPane().setStyle("-fx-font-size: 10pt;");
					alert.setContentText(e.errorMessage);
					alert.initOwner(loadPopup.getScene().getWindow());
					alert.showAndWait();
				}
				catch(InvalidValueException e) {
					//System.out.print(e.errorMessage);
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Invalid Value Alert");
					alert.setHeaderText(null);
					alert.getDialogPane().setStyle("-fx-font-size: 10pt;");
					alert.setContentText(e.errorMessage);
					alert.initOwner(loadPopup.getScene().getWindow());
					alert.showAndWait();
				}
				if(configurations!=null) {
					DIFFICULTY = configurations[0];
					MINES = configurations[1];
					SECONDS = configurations[2];
					HYPERMINE = configurations[3];
				}
				loadPopup.hide();
			}
		});
		
		//cancelLoadButton
		cancelLoadButton = new Button("Cancel");
		cancelLoadButton.setOnAction(event -> {
			loadPopup.hide();
			});
		
		startScene = new BorderPane();
		
		
		menubar = new MenuBar();
		
		application = new Menu("Application");
		createGame = new MenuItem("Create");
		loadGame = new MenuItem("Load");
		startGame = new MenuItem("Start");
		exitGame = new MenuItem("Exit");
		
		application.getItems().addAll(createGame, loadGame, startGame, exitGame);
		
		details = new Menu("Details");
		rounds = new MenuItem("Rounds");
		solution = new MenuItem("Solution");
		
		details.getItems().addAll(rounds, solution);
		
		menubar.getMenus().addAll(application, details);
		
		startScene.setTop(menubar);
		
		Rectangle fillScreen = new Rectangle(W, H);
		
		startScene.setCenter(fillScreen);
		
		
		restartButton = new Button("Play Again");
		restartButton.setOnAction(event -> {
			BorderPane root = new BorderPane();
			root.setTop(menubar);
			root.setCenter(fillScreen);
			scene = new Scene(root, W, H+50);
			stage.setScene(scene);
			stage.setTitle("Medialab Minesweeper");
			overPopup.hide();
		});
		
		createGame.setOnAction(e -> {
			
			createPopup = new Popup();
			
	        // Create the VBox to hold the text fields
	        HBox hBox = new HBox(cancelCreateButton, confirmCreateButton);
	        VBox vBox = new VBox(10, scenarioID, difficulty, minesNum, hasHyper, timeAvail, hBox);
	        vBox.setPadding(new Insets(10));
	        
	        // Create the rectangle background
	        Rectangle rectangle = new Rectangle(250, 220, Color.LIGHTGRAY);
	        StackPane stackPane = new StackPane(rectangle, vBox);
	        
	        createPopup.getContent().add(stackPane);
			createPopup.show(stage);
			
		});
		
		loadGame.setOnAction(e -> {
			if(ingame) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.getDialogPane().setStyle("-fx-font-size: 10pt;");
				alert.setContentText("You can't use that feature while playing");

				alert.showAndWait();
				return;
			}
			
			loadPopup = new Popup();
			
			HBox hBox = new HBox(cancelLoadButton, confirmLoadButton);
			hBox.setSpacing(20);
			VBox vBox = new VBox(chooseFileButton, hBox);
			vBox.setPadding(new Insets(100));
			vBox.setSpacing(20);
			
			Rectangle rectangle = new Rectangle(200, 110, Color.LIGHTGRAY);
	        StackPane stackPane = new StackPane(rectangle, vBox);
	        
	        loadPopup.getContent().add(stackPane);
			loadPopup.show(stage);//scene.getWindow()
			
		});
		
		startGame.setOnAction(e -> {
			
			gameScene = new Scene(createContent(), W, H+50);
			
			stage.setScene(gameScene);
			stage.setTitle("Medialab Minesweeper");
			
		});
		
		exitGame.setOnAction(e -> {
			Platform.exit();
		});
		
		rounds.setOnAction(e -> {
			//timeline.stop();
			roundsPopup = new Popup();
			 VBox detailsVBox = new VBox(historyFormat(game0, 1), historyFormat(game1, 2),
					 historyFormat(game2, 3), historyFormat(game3, 4), historyFormat(game4, 5), closeDetailsButton);
			 detailsVBox.setAlignment(Pos.BASELINE_CENTER);
				
			StackPane content = new StackPane(new Rectangle(360,130,Color.ALICEBLUE), detailsVBox);
				
			roundsPopup.getContent().add(content);
				
			roundsPopup.show(stage);
			 
		});
		
		solution.setOnAction(e -> { 
			if(!ingame) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.getDialogPane().setStyle("-fx-font-size: 10pt;");
				alert.setContentText("You can't use that feature while playing");

				alert.showAndWait();
				return;
			}
			solved = true;
			for(int y = 0; y < Y_TILES; y++) {
				for(int x = 0; x < X_TILES; x++) {
					Tile tile = grid[x][y];
					tile.openHelp(tile);
				}
			}
			gameOver("solution");
		});
		
		scene = new Scene(startScene, W, H+50);
		
		stage.setScene(scene);
		stage.setTitle("Medialab Minesweeper");
		stage.show();

	}
	public static void main(String[] args) {
		launch(args);
	}
}
