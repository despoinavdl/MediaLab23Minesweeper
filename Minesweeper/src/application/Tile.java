package application;

import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Tile extends StackPane {
	int x,y;
	boolean hasMine, hasHyperMine, isOpen = false, isMarked = false;
	
	Rectangle border = new Rectangle(MinesweeperApp.TILE_SIZE - 2, MinesweeperApp.TILE_SIZE -2);
	Text text = new Text();
	
	
	public Tile(int x, int y, boolean hasMine, boolean hasHyperMine) {
		this.x = x;
		this.y = y;
		this.hasMine = hasMine;
		this.hasHyperMine = hasHyperMine;
	
		border.setStroke(Color.GREEN);
		border.setFill(Color.BLACK);
		
		text.setFont(Font.font(18));
		text.setText("");
		text.setVisible(false);////////CHANGE TO FALSE
		text.setFill(Color.BLACK);////CHANGE TO BLACK

		getChildren().addAll(border, text);
		
		setTranslateX(x * MinesweeperApp.TILE_SIZE);
		setTranslateY(y * MinesweeperApp.TILE_SIZE);
		
		setOnMouseClicked(event -> {
		    if (event.getButton() == MouseButton.PRIMARY) {
		    	if(!MinesweeperApp.ingame) return;
		    	MinesweeperApp.clicked = true;
		    	open();
		    }
		    else if (event.getButton() == MouseButton.SECONDARY) {
		    	if(!MinesweeperApp.ingame) return;
		    	mark();
		    }
		});
	}
	
	void open() {
		if(isOpen) return;
		
		if(MinesweeperApp.clicked) {
			MinesweeperApp.triesOpen++;
		}
		
		if(this.isMarked) {
			MinesweeperApp.marked--;
			isMarked = false;
			MinesweeperApp.topBar.setText("Mines: " + MinesweeperApp.MINES + "   Marked Tiles: " 
			+ MinesweeperApp.marked + "   Time Left: " 
					+ String.format("%02d:%02d", MinesweeperApp.seconds_left / 60, MinesweeperApp.seconds_left % 60));
		}
		
		if(this.hasMine) {
			MinesweeperApp.gameOver("mine");
			return;
			
		}
		
		isMarked = false;
		isOpen = true;
		text.setVisible(true);
		border.setFill(Color.LIGHTGREEN);
		
		if(text.getText().isEmpty()) {
			MinesweeperApp.clicked = false;
			MinesweeperApp.getNeighbours(this).forEach(Tile::open);
		}
		
		if(--MinesweeperApp.tilesToOpen == 0 && !MinesweeperApp.solved) {
			//victory!
			MinesweeperApp.gameOver("victory");
		}

	}
	
	void openHelp(Tile tile) {
		if(tile.isOpen) return;
		
		if(tile.isMarked) {
			MinesweeperApp.marked--;
			tile.isMarked = false;
			MinesweeperApp.topBar.setText("Mines: " + MinesweeperApp.MINES + 
					"   Marked Tiles: " + MinesweeperApp.marked + "   Time Left: " 
					+ String.format("%02d:%02d", MinesweeperApp.seconds_left / 60, MinesweeperApp.seconds_left % 60));
		}
		
		tile.isMarked = false;
		tile.isOpen = true;
		tile.text.setVisible(true);
		tile.border.setFill(Color.LIGHTGREEN);
		
		if(tile.hasMine) {
			tile.border.setFill(Color.PALEGOLDENROD);
			tile.hasMine = false;
			return;
		}
		
		if(tile.text.getText().isEmpty()) {
			//clicked = false;
			//getNeighbours(tile).forEach(Tile::open);
			tile.isMarked = false;
			tile.isOpen = true;
			tile.text.setVisible(true);
			tile.border.setFill(Color.LIGHTGREEN);
		}
		
		if(--MinesweeperApp.tilesToOpen == 0 && !MinesweeperApp.solved) {
			//victory!
			MinesweeperApp.gameOver("victory");
		}

	}
	
	void mark() {
		if(isMarked) {
			this.border.setFill(Color.BLACK);
			MinesweeperApp.marked--;
			MinesweeperApp.topBar.setText("Mines: " + MinesweeperApp.MINES + 
					"   Marked Tiles: " + MinesweeperApp.marked + "   Time Left: " +
					String.format("%02d:%02d", MinesweeperApp.seconds_left / 60, MinesweeperApp.seconds_left % 60));
			isMarked = false;
			return;
		}
		
		if(MinesweeperApp.marked >= MinesweeperApp.MINES  || this.isOpen) 
			return;
		
		if(this.hasHyperMine && MinesweeperApp.triesOpen <= 3) {
			for(int i = 0; i < MinesweeperApp.X_TILES; i++) {
				openHelp(MinesweeperApp.grid[i][this.y]);
			}
			for(int i = 0; i < MinesweeperApp.Y_TILES; i++) {
				openHelp(MinesweeperApp.grid[this.x][i]);
			}
			return;
		}
		
		this.border.setFill(Color.HOTPINK);
		this.isMarked = true;
		MinesweeperApp.marked++;
		MinesweeperApp.topBar.setText("Mines: " + MinesweeperApp.MINES + 
				"   Marked Tiles: " + MinesweeperApp.marked + "   Time Left: " + 
				String.format("%02d:%02d", MinesweeperApp.seconds_left / 60, MinesweeperApp.seconds_left % 60));
	}
}
