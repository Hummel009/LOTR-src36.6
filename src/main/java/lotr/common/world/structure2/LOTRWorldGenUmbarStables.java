package lotr.common.world.structure2;

public class LOTRWorldGenUmbarStables extends LOTRWorldGenSouthronStables {
	public LOTRWorldGenUmbarStables(boolean flag) {
		super(flag);
	}

	@Override
	protected boolean isUmbar() {
		return true;
	}
}
