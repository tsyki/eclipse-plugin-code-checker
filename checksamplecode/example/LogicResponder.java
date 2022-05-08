package example;

public class LogicResponder implements Responder{
	private Logic logic;
	
	public LogicResponder(Logic logic) {
		this.logic = logic;
	}
	
	@Override
	public void callback(String result) {
		System.out.println(logic.getClass().getName() +  " is finished.");
	}
	
}
