package example;

public class SampleLogic implements Logic{
	@Override
	public void run() {
		SimpleResponder responder = new SimpleResponder();
		Util.save(responder);
		Util.save(new SimpleResponder());
		Util.save(new LogicResponder(this));
	}
	
	// ���\�b�h����30�����ȏ�̏ꍇ�̃`�F�b�N�����̃e�X�g�p
	public void longlonglonglonglonglonglonglonglonglongName() {
		 
	}
	
	public static void main(String[] args) {
		new SampleLogic().run();
	}
}
