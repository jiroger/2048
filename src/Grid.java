import java.util.ArrayList;
import processing.core.PApplet;
import java.math.*;

public class Grid {
	PApplet parent;
	Block[][] block;
	private final int COLS;
	private final int ROWS;
	private int score;

	public Grid(int cols, int rows, PApplet p) {
		COLS = cols;
		ROWS = rows;
		block = new Block[COLS][ROWS];
		parent = p;
		initBlocks(); // initializes all blocks to empty blocks
	}

	public Block getBlock(int col, int row) {
		return block[col][row];
	}

	public void setBlock(int col, int row, int value, boolean changed) {
		block[col][row] = new Block(value, changed);
	}

	public void setBlock(int col, int row, int value) {
		setBlock(col, row, value, false);
	}

	public void setBlock(int col, int row) {
		setBlock(col, row, 0, false);
	}

	public void setBlock(int col, int row, Block b) {
		block[col][row] = b;
	}

	public void initBlocks() {
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				block[i][j] = new Block();
			}
		}
	}

	public boolean isValid(int col, int row) {
		return col < COLS && row < ROWS && col >= 0 && row >= 0;
	}

	public void swap(int col1, int row1, int col2, int row2) {
		Block temp = block[col1][row1];
		block[col1][row1] = block[col2][row2];
		block[col2][row2] = temp;
	}

	public boolean canMerge(int col1, int row1, int col2, int row2) {
		return (block[col1][row1].getValue() == block[col2][row2].getValue()) && block[col1][row1].getValue() > 0;
	}

	public void clearChangedFlags() {
		for (int col = 0; col < COLS; col++) {
			for (int row = 0; row < ROWS; row++) {
				block[col][row].setChanged(false);
			}
		}
	}

	// Is there an open space on the grid to place a new block?
	public boolean canPlaceBlock() {
		return getEmptyLocations().size() > 0;
	}

	public ArrayList<Location> getEmptyLocations() {
		// Put all locations that are currently empty into locs
		ArrayList<Location> locs = new ArrayList<Location>();
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (block[i][j].isEmpty()) {
					locs.add(new Location(i, j));
				}
			}
		}
		return locs;
	}

	public Location selectLocation(ArrayList<Location> locs) {
		int randomLocation = (int) (Math.random() * locs.size());
		// System.out.println(randomLocation + " ok");
		return locs.get(randomLocation);
	}

	// Randomly select an open location to place a block.
	public void placeBlock() {
		Location theOne = selectLocation(getEmptyLocations());
		int twoOrFour = (int) (Math.random() * 8 + 1);
		if (twoOrFour < 7) {
			setBlock(theOne.getCol(), theOne.getRow(), 2);
		} else {
			setBlock(theOne.getCol(), theOne.getRow(), 4);
		}
	}

	// Are there any adjacent blocks that contain the same value?
	public boolean hasCombinableNeighbors() {
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if ((isValid(i, j + 1) && canMerge(i, j, i, j + 1)) || (isValid(i + 1, j) && canMerge(i, j, i + 1, j))
						|| (isValid(i - 1, j) && canMerge(i, j, i - 1, j))
						|| (isValid(i, j - 1) && canMerge(i, j, i, j - 1))) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean someBlockCanMoveInDirection(DIR dir) {
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (dir == DIR.WEST) {
					if (isValid(i - 1, j)
							&& (canMerge(i, j, i - 1, j) || (!block[i][j].isEmpty() && block[i - 1][j].isEmpty()))) {
						return true;
					}
				} else if (dir == DIR.NORTH) {
					if (isValid(i, j - 1)
							&& (canMerge(i, j, i, j - 1) || (!block[i][j].isEmpty() && block[i][j - 1].isEmpty()))) {
						return true;
					}
				} else if (dir == DIR.EAST) {
					if (isValid(i + 1, j)
							&& (canMerge(i, j, i + 1, j) || (!block[i][j].isEmpty() && block[i + 1][j].isEmpty()))) {
						return true;
					}
				} else {
					if (isValid(i, j + 1)
							&& (canMerge(i, j, i, j + 1) || (!block[i][j].isEmpty() && block[i][j + 1].isEmpty()))) {

						return true;
					}
				}
			}
		}
		return false;
	}

	// Computes the number of points that the player has scored
	public void computeScore() {
		score = 0;
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				score += block[i][j].getValue();
			}
		}
	}

	public int getScore() {
		return score;
	}

	public void showScore() {
      parent.textFont(scoreFont);
      parent.fill(#000000);
      parent.text("Score: " + getScore(), width/2, The2048.SCORE_Y_OFFSET);
      parent.textFont(blockFont);
  }

	public void showBlocks() {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				Block b = block[col][row];
				if (!b.isEmpty()) {
					double adjustment = (Math.log(b.getValue()) / Math.log(2)) - 1;
					parent.fill(color(242, 241 - 8 * adjustment, 239 - 8 * adjustment));
					parent.rect(The2048.GRID_X_OFFSET + (The2048.BLOCK_SIZE + The2048.BLOCK_MARGIN) * col,
							The2048.GRID_Y_OFFSET + (The2048.BLOCK_SIZE + The2048.BLOCK_MARGIN) * row,
							The2048.BLOCK_SIZE, The2048.BLOCK_SIZE, The2048.BLOCK_RADIUS);
					parent.fill(color(108, 122, 137));
					parent.text(str(b.getValue()),
							The2048.GRID_X_OFFSET + (The2048.BLOCK_SIZE + The2048.BLOCK_MARGIN) * col
									+ The2048.BLOCK_SIZE / 2,
							The2048.GRID_Y_OFFSET + (The2048.BLOCK_SIZE + The2048.BLOCK_MARGIN) * row
									+ The2048.BLOCK_SIZE / 2 - Y_TEXT_OFFSET);
				} else {
					parent.fill(BLANK_COLOR);
					parent.rect(The2048.GRID_X_OFFSET + (The2048.BLOCK_SIZE + The2048.BLOCK_MARGIN) * col,
							The2048.GRID_Y_OFFSET + (The2048.BLOCK_SIZE + The2048.BLOCK_MARGIN) * row,
							The2048.BLOCK_SIZE, The2048.BLOCK_SIZE, The2048.BLOCK_RADIUS);
				}
			}
		}
	}

	// Copy the contents of another grid to this one
	public void gridCopy(Grid other) {
		for (int i = 0; i < other.COLS; i++) {
			for (int j = 0; j < other.ROWS; j++) {
				block[i][j] = other.getBlock(i, j);
			}
		}
	}

	public boolean isGameOver() {
		return !hasCombinableNeighbors() && getEmptyLocations().size() == 0;
	}

	public void showGameOver() {
		parent.fill(#0000BB);
		parent.text("GAME OVER", The2048.GRID_X_OFFSET + 2*The2048.BLOCK_SIZE + 15, The2048.GRID_Y_OFFSET + 2*The2048.BLOCK_SIZE + 15);
  }

	// public String toString() {
	// String str = "";
	// for (int row = 0; row < ROWS; row++) {
	// for (int col = 0; col < COLS; col++) {
	// str += block[col][row].getValue() + " ";
	// }
	// str += "\n"; // "\n" is a newline character
	// }
	// return str;
	// }
}