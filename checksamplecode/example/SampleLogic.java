package example;

public class SampleLogic implements Logic{
	private Responder fieldResponder;
	
	@Override
	public void run() {
		Responder simpleResponder = new SimpleResponder();
		Util.save(simpleResponder); // NG
		Responder logicResponder = new LogicResponder(this);
		Util.save(logicResponder); // OK
		Responder localResponderByField = fieldResponder;
		Util.save(localResponderByField); // OK(フィールドは判定対象外)
		Util.save(fieldResponder); // OK(フィールドは判定対象外)
		Responder localResponderByMethod = getResponder();
		Util.save(localResponderByMethod); // OK(メソッドは判定対象外)
		{
			Responder r = new SimpleResponder();
			Util.save(r); // NG
		}
		{
			Responder r = new LogicResponder(this);
			Util.save(r); // OK
		}
		{
			Responder r;
			r = new SimpleResponder(); 
			Util.save(r); // NG
		}
		{
			Responder r;
			r = new LogicResponder(this); 
			Util.save(r); // OK
		}
		{
			Responder r = e->{};
			Util.save(r); // NG
		}
		{
			Responder r = new SimpleResponder() {};
			Util.save(r); // NG
		}
		{
			Responder r = new LogicResponder(this) {};
			Util.save(r); // OK
		}
		
		Util.save(getResponder()); // OK(メソッドは判定対象外)
		Util.save(e->{}); // NG
		Util.save(new SimpleResponder()); //NG
		Util.save(new LogicResponder(this)); //OK
		Util.save(new Responder() { //NG
			@Override
			public void callback(String result) {
				
			}
		});
		Util.save(new LogicResponder(this) { //OK
			@Override
			public void callback(String result) {
				
			}
		});
		new Logic() {
			@Override
			public void run() {
				Util.save(new SimpleResponder()); //NG
				Util.save(new LogicResponder(this)); //OK
			}
		}.run();
	}
	
	public void hoge(Responder responder) {
		Util.save(responder); //OK(引数の場合は判定対象外)
	}
	
	private Responder getResponder() {
		return null;
	}

	// メソッド名が30文字以上の場合のチェック処理のテスト用
	public void longlonglonglonglonglonglonglonglonglongName() {
		 
	}
	
	public static void main(String[] args) {
		new SampleLogic().run();
	}
}
