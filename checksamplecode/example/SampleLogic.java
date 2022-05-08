package example;

public class SampleLogic implements Logic{
	@Override
	public void run() {
		SimpleResponder responder = new SimpleResponder();
		Util.save(responder);
		Util.save(new SimpleResponder());
		Util.save(new LogicResponder(this));
	}
	
	// メソッド名が30文字以上の場合のチェック処理のテスト用
	public void longlonglonglonglonglonglonglonglonglongName() {
		 
	}
	
	public static void main(String[] args) {
		new SampleLogic().run();
	}
}
