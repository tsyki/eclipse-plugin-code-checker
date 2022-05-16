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
		Util.save(localResponderByField); // OK(�t�B�[���h�͔���ΏۊO)
		Util.save(fieldResponder); // OK(�t�B�[���h�͔���ΏۊO)
		Responder localResponderByMethod = getResponder();
		Util.save(localResponderByMethod); // OK(���\�b�h�͔���ΏۊO)
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
		
		Util.save(getResponder()); // OK(���\�b�h�͔���ΏۊO)
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
		Util.save(responder); //OK(�����̏ꍇ�͔���ΏۊO)
	}
	
	private Responder getResponder() {
		return null;
	}

	// ���\�b�h����30�����ȏ�̏ꍇ�̃`�F�b�N�����̃e�X�g�p
	public void longlonglonglonglonglonglonglonglonglongName() {
		 
	}
	
	public static void main(String[] args) {
		new SampleLogic().run();
	}
}
