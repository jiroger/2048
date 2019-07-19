// used to store grid information about blocks that need to move after a key press.

public class Animation {
	private int fromCol;
	private int fromRow;
	private int fromValue;
	private int toCol;
	private int toRow;
	private int toValue;

	public Animation(int fCol, int fRow, int fv, int tcol, int trow, int tv) {
		fromCol = fCol; // from refers to before block move
		fromRow = fRow;
		fromValue = fv;
		toCol = tcol; // to refers to after block is moved
		toRow = trow;
		toValue = tv;
	}

	public int getFromCol() {
		return fromCol;
	}

	public int getFromRow() {
		return fromRow;
	}

	public int getFromValue() {
		return fromValue;
	}

	public int getToCol() {
		return toCol;
	}

	public int getToRow() {
		return toRow;
	}

	public int getToValue() {
		return toValue;
	}
}