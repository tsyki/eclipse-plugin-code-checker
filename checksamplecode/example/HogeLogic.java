package example;

public class HogeLogic extends SampleLogic {
	@Override
	public void run() {
		Util.save(new SimpleResponder());
		Util.save(new LogicResponder(this));
	}
}
