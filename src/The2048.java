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

	// Every time a key is pressed, blocks need to move visually (if there is a way
	// to move).
	// All of the animations to be carried out are stored in anims.
	public ArrayList<Animation> anims = new ArrayList<Animation>();
	// number of movements to complete a block animation
	public final int TICK_STEPS = 20;
	// counter for number of iterations have been used to animate block movement
	// if animation_ticks == TICK_STEPS, then the display is not moving blocks
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
		;
		textFont(blockFont);
		textAlign(CENTER, CENTER);

		scoreFont = createFont("LucidaSans", 42);

		// Comment out this setBlock() and replace it with a placeBlock() once you have
		// written placeBlock().
		grid.placeBlock();
		grid.placeBlock();
		for (int i = 0; i < grid.getEmptyLocations().size(); i++) {
			System.out
					.println(grid.getEmptyLocations().get(i).getRow() + " " + grid.getEmptyLocations().get(i).getCol());
		}

		// System.out.print(grid.canMerge(0,0,1,1));

		System.out.println(grid.hasCombinableNeighbors());
		// System.out.println(grid.someBlockCanMoveInDirection(DIR.NORTH));

		backup_grid.gridCopy(grid); // save grid in backup_grid in case Undo is needed
	}

	// This is where the animation will take place
	// draw() exhibits the behavior of being inside an infinite loop
	@Override
	public void draw() {
		background(BACKGROUND_COLOR);
		grid.computeScore();
		grid.showScore();
		grid.showBlocks();

		// Don't show GAME OVER during an animation
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

			// animation_ticks is used to count up to TICK_STEPS, which
			// determines how many

			// Iterate on the anims ArrayList to
			for (int i = 0; i < anims.size(); i++) {
				Animation a = anims.get(i);
				float col = (float) (1.0 * ((a.getToCol() - a.getFromCol()) * animation_ticks) / TICK_STEPS
						+ a.getFromCol());
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
		// BEGIN MOVEMENT SECTION
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
						// While the position being inspected is in the grid and does not contain a
						// block
						// whose values has already been changed this move
						while (newGrid.isValid(colPos + colAdjust, row)
								&& !newGrid.getBlock(colPos, row).hasChanged()) {
							if (newGrid.getBlock(colPos + colAdjust, row).isEmpty()) {
								// if (newGrid[colPos + colAdjust][row].getValue() == -1) {
								// Move the block into the empty space and create an empty space where the block
								// was
								newGrid.swap(colPos, row, colPos + colAdjust, row);
							} else if (newGrid.canMerge(colPos + colAdjust, row, colPos, row)) {
								if (!newGrid.getBlock(colPos + colAdjust, row).hasChanged()) {
									newGrid.setBlock(colPos + colAdjust, row,
											newGrid.getBlock(colPos, row).getValue() * 2, true);
									newGrid.setBlock(colPos, row);
								}
							} else { // Nowhere to move to
								break; // Exit while loop
							}
							colPos += colAdjust;
						}
						// If a block moves, add its information to the list of blocks that must be
						// animated
						anims.add(new Animation(col, row, val, colPos, row, val));
					}
				}
			}
		}

		// NORTH-SOUTH movement
		//
		// Analogous to EAST-WEST movement
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
								break; // Exit while loop
							}
							rowPos += rowAdjust;
						}
						// If a block moves, add its information to the list of blocks that must be
						// animated
						anims.add(new Animation(col, row, val, col, rowPos, val));
					}
				}
			}
		}

		newGrid.clearChangedFlags();
		if (newGrid.canPlaceBlock()) {
			newGrid.placeBlock();
		}

		backup_grid.gridCopy(grid); // Copy the grid to backup in case Undo is needed
		grid.gridCopy(newGrid); // The newGrid should now be made the main grid

		// END MOVEMENT SECTION

		startAnimations();
	}

	public void startAnimations() {
		// Effectively turns draw() into a for loop with animation_ticks as the index
		animation_ticks = 0;
	}

	// KEY EVENTS SECTION KEY EVENTS SECTION

	// The only keys (and corresponding keyCodes) that are used to control the game
	// are:
	// * RETURN (10)--Restarts game if Game Over is being displayed
	// * LEFT ARROW (37)--Move blocks to the left
	// * UP ARROW (38)--Move blocks up
	// * RIGHT ARROW (39)--Move blocks right
	// * DOWN ARROW (40)--Move blocks down
	// * Upper-case 'U' (85)--Undo (revert one keypress)

	@Override
	public void keyPressed() {
		if (grid.isGameOver()) {
			// If RETURN is pressed, then start a fresh game with one block
			if (keyCode == 10) {
				grid.initBlocks();
				grid.placeBlock();
			}
			return;
		}

		// If a key is pressed and it isn't LEFT (arrow), RIGHT, UP, DOWN, or U,
		// then ignore it by returning immediately
		if (!(Utility_Functions.isBetween(keyCode, 37, 40) || keyCode == 85))
			return;

		if (keyCode == 85) { // ASCII value for upper case U (for Undo)
			grid.gridCopy(backup_grid); // Copy the backup grid to the main grid
			return;
		}

		DIR dir;
		DIR[] dirs = { DIR.WEST, DIR.NORTH, DIR.EAST, DIR.SOUTH };
		// Key codes for LEFT ARROW, UP ARROW, RIGHT ARROW, and DOWN ARROW are 37--40.
		// By subtracting 37, we get an appropriate index for the dirs array that
		// converts
		// LEFT ARROW to DIR.WEST, UP ARROW to DIR.
		dir = dirs[keyCode - 37];

		if (!grid.someBlockCanMoveInDirection(dir))
			return;
		else
			gameUpdate(dir);
	}

}
