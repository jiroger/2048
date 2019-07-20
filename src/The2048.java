import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;

public class The2048 extends PApplet {

	public static void main(String[] args) {
		PApplet.main("The2048");
	}

	public static final int GRID_X_OFFSET = 15; // distance from left to start drawing grid
	public static final int GRID_Y_OFFSET = 85; // distance from top to start drawing grid
	public static final int BLOCK_SIZE = 120; // width and height of a block
	public static final int BLOCK_MARGIN = 15; // separation between blocks
	public static final int BLOCK_RADIUS = 5; // for making blocks look slightly rounded in corners
	public static final int Y_TEXT_OFFSET = 7; // for centering the numbers when drawn on blocks
	public static final int GRID_SIZE = 4; // number of rows and columns
	public static final int COLS = GRID_SIZE;
	public static final int ROWS = GRID_SIZE;
	public static PFont blockFont;

	public static final int SCORE_Y_OFFSET = 36;
	public static PFont scoreFont;

	public final int BACKGROUND_COLOR = color(189, 195, 199);
	public final int BLANK_COLOR = color(203, 208, 210);
	public Grid grid = new Grid(COLS, ROWS, this);
	public Grid backup_grid = new Grid(COLS, ROWS, this);

	// all of the animations (e.g. when key pressed, blocks need to visuallly move)
	// to be carried out are stored in anims.
	public ArrayList<Animation> anims = new ArrayList<Animation>();
	// number of movements to complete a block animation
	public final int TICK_STEPS = 20;
	// counter for number of iterations have been used to animate block movement
	// if animation_ticks == TICK_STEPS, then display is not moving blocks
	public int animation_ticks = TICK_STEPS;

	@Override
	public void settings() {
		size(555, 625);
	}

	@Override
	public void setup() {
		background(BACKGROUND_COLOR);
		noStroke();

		blockFont = createFont("LucidaSans", 50);
		textFont(blockFont);
		textAlign(CENTER, CENTER);

		scoreFont = createFont("LucidaSans", 42);

		grid.placeBlock(); // start with 2 blocks on the board
		grid.placeBlock();
		for (int i = 0; i < grid.getEmptyLocations().size(); i++) {
			System.out
					.println(grid.getEmptyLocations().get(i).getRow() + " " + grid.getEmptyLocations().get(i).getCol());
		}

		// System.out.print(grid.canMerge(0,0,1,1));

		System.out.println(grid.hasCombinableNeighbors());
		// System.out.println(grid.someBlockCanMoveInDirection(DIR.NORTH));

		backup_grid.gridCopy(grid); // save grid in backup_grid in case undo is needed
	}

	@Override
	public void draw() {
		background(BACKGROUND_COLOR);
		grid.computeScore();
		grid.showScore();
		grid.showBlocks();

		// don't show GAME OVER during an animation
		if (animation_ticks == TICK_STEPS && grid.isGameOver()) {
			grid.showGameOver();
		}

		if (animation_ticks < TICK_STEPS) {
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					fill(BLANK_COLOR);
					rect(GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN) * col,
							GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN) * row, BLOCK_SIZE, BLOCK_SIZE, BLOCK_RADIUS);
				}
			}

			// iterate on the anims arraylist to update graphics
			for (int i = 0; i < anims.size(); i++) {
				Animation a = anims.get(i);
				float col = (float) (1.0 * ((a.getToCol() - a.getFromCol()) * animation_ticks) / TICK_STEPS
						+ a.getFromCol());
				// must use float instead of double because processing's fill and rect methods
				// only accept floats
				float row = (float) (1.0 * ((a.getToRow() - a.getFromRow()) * animation_ticks) / TICK_STEPS
						+ a.getFromRow());
				double adjustment = (log(a.getFromValue()) / log(2)) - 1;
				fill(color(242, (int) (241 - 8 * adjustment), (int) (239 - 8 * adjustment)));
				rect(GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN) * col,
						GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN) * row, BLOCK_SIZE, BLOCK_SIZE, BLOCK_RADIUS);
				fill(color(108, 122, 137));
				text(str(a.getFromValue()), GRID_X_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN) * col + BLOCK_SIZE / 2,
						GRID_Y_OFFSET + (BLOCK_SIZE + BLOCK_MARGIN) * row + BLOCK_SIZE / 2 - Y_TEXT_OFFSET);
			}
			animation_ticks += 1;
		}
	}

	public void gameUpdate(DIR direction) {
		// MOVEMENT SECTION
		Grid newGrid = new Grid(COLS, ROWS, this);
		newGrid.gridCopy(grid); //
		anims = new ArrayList<Animation>();

		// EAST-WEST movement
		if (direction == DIR.WEST || direction == DIR.EAST) {
			int startingCol = direction == DIR.EAST ? GRID_SIZE - 1 : 0;
			int endingCol = direction == DIR.EAST ? -1 : GRID_SIZE;
			int colAdjust = direction == DIR.EAST ? 1 : -1;

			for (int row = 0; row < ROWS; row++) {
				for (int col = startingCol; col != endingCol; col -= colAdjust) {
					int colPos = col;
					int val = newGrid.getBlock(col, row).getValue();
					if (!newGrid.getBlock(col, row).isEmpty()) {
						// while the position being inspected is in the grid and does not contain a
						// block whose values has already been changed this move
						while (newGrid.isValid(colPos + colAdjust, row)
								&& !newGrid.getBlock(colPos, row).hasChanged()) {
							if (newGrid.getBlock(colPos + colAdjust, row).isEmpty()) {
								// if (newGrid[colPos + colAdjust][row].getValue() == -1) {
								// move the block into empty space and create an empty space where the block
								// used to be

								newGrid.swap(colPos, row, colPos + colAdjust, row);
							} else if (newGrid.canMerge(colPos + colAdjust, row, colPos, row)) {
								if (!newGrid.getBlock(colPos + colAdjust, row).hasChanged()) {
									newGrid.setBlock(colPos + colAdjust, row,
											newGrid.getBlock(colPos, row).getValue() * 2, true);
									newGrid.setBlock(colPos, row);
								}
							} else { // theres nowhere to go, so u exit the loop
								break;
							}
							colPos += colAdjust;
						}
						// if block moves, add its information to the list of blocks that must be
						// animated
						anims.add(new Animation(col, row, val, colPos, row, val));
					}
				}
			}
		}

		// NORTH-SOUTH movement, pretty much same to above

		if (direction == DIR.NORTH || direction == DIR.SOUTH) {
			int startingRow = direction == DIR.SOUTH ? GRID_SIZE - 1 : 0;
			int endingRow = direction == DIR.SOUTH ? -1 : GRID_SIZE;
			int rowAdjust = direction == DIR.SOUTH ? 1 : -1;

			for (int col = 0; col < COLS; col++) {
				for (int row = startingRow; row != endingRow; row -= rowAdjust) {
					int rowPos = row;
					int val = newGrid.getBlock(col, rowPos).getValue();
					if (!newGrid.getBlock(col, rowPos).isEmpty()) {
						while (newGrid.isValid(col, rowPos + rowAdjust)
								&& !newGrid.getBlock(col, rowPos).hasChanged()) {
							if (newGrid.getBlock(col, rowPos + rowAdjust).isEmpty()) {
								newGrid.swap(col, rowPos, col, rowPos + rowAdjust);
							} else if (newGrid.canMerge(col, rowPos + rowAdjust, col, rowPos)) {
								if (!newGrid.getBlock(col, rowPos + rowAdjust).hasChanged()) {
									newGrid.setBlock(col, rowPos + rowAdjust,
											newGrid.getBlock(col, rowPos).getValue() * 2, true);
									newGrid.setBlock(col, rowPos);
								}
							} else {
								break;
							}
							rowPos += rowAdjust;
						}

						anims.add(new Animation(col, row, val, col, rowPos, val));
					}
				}
			}
		}

		newGrid.clearChangedFlags();
		if (newGrid.canPlaceBlock()) {
			newGrid.placeBlock();
		}

		backup_grid.gridCopy(grid); // this is so that there's a backup if u want to undo ur move
		grid.gridCopy(newGrid); // the newGrid should now be made the main grid

		// END MOVEMENT SECTION

		startAnimations();
	}

	public void startAnimations() {
		// turns draw into a for loop with animation_ticks as index
		animation_ticks = 0;
	}

	@Override
	public void keyPressed() {
		if (grid.isGameOver()) {
			// return pressed = start a fresh game with one block
			if (keyCode == 10) {
				grid.initBlocks();
				grid.placeBlock();
			}
			return;
		}

		// if key != LEFT (arrow), RIGHT, UP, DOWN, or U,
		// then ignore it by returning immediately
		if (!(Utility_Functions.isBetween(keyCode, 37, 40) || keyCode == 85))
			return;

		if (keyCode == 85) { // ASCII value for upper case U = undo
			grid.gridCopy(backup_grid); // copy the backup grid to the main grid
			return;
		}

		DIR dir;
		DIR[] dirs = { DIR.WEST, DIR.NORTH, DIR.EAST, DIR.SOUTH };
		// keycodes for LEFT ARROW, UP ARROW, RIGHT ARROW, and DOWN ARROW are 37--40.

		dir = dirs[keyCode - 37]; // that way we have a working index that connects left arrow to dir.west, etc.

		if (!grid.someBlockCanMoveInDirection(dir))
			return;
		else
			gameUpdate(dir);
	}

}
