package example;

public class SimpleResponder implements Responder{
	@Override
	public void callback(String result) {
		System.out.println(result);
	}
}
