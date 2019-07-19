//the grid class is more for the logic/graphics of the board; location class is for the actual spot (x, y) the block is on the board
public class Location {
	private int col;
	private int row;

	public Location(int col, int row) {
		this.col = col;
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}
}