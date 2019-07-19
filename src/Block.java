
public class Block {
	private final int EMPTY = 0;
	private int value = EMPTY; // 0 means blank; value always in powers of 2
	// if a block moves, it could merge with another block of the same value.
	// hasChanged being set to true means that for this keypress, the block has
	// changed already and other blocks cannot merge with it.
	private boolean hasChanged = false;

	public Block() {
		this(0, false);
	}

	public Block(int val, boolean changed) {
		value = val;
		hasChanged = changed;
	}

	public void setValue(int val) {
		value = val;
	}

	public int getValue() {
		return value;
	}

	public void setChanged(boolean changed) {
		hasChanged = changed;
	};

	public boolean hasChanged() {
		return hasChanged;
	};

	public boolean isEmpty() {
		return value == EMPTY;
	}
}