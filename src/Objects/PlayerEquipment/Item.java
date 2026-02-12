package Objects.PlayerEquipment;

public class Item {
	public Item(boolean visible, String imgpath) {
		if (!visible) {
			throw new IllegalArgumentException("Bad Input; This constructor is ONLY for visible items, first argument should be true");
		}
	}
	public Item() {
		
	}
}
