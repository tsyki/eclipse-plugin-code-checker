# eclipse-plugin-code-checker

Eclipseプラグインでのコード解析のサンプル。  
プロジェクトエクスプローラーの右クリックメニューに「My Code Checker」のメニューを追加し、選択したファイル、もしくは配下のファイルを解析し、警告を表示する。  

## 警告を出す対象
* メソッド名が30文字以上(LongMethodNameCheckVisitor)
* 特定のインタフェース(Logic)を実装したクラスで特定の型の引数(Responder)を持つメソッドを呼び出した際に、その実引数が特定の型(LogicResponder)を実装していない場合(ArgumentTypeCheckVisitor)
    * 実引数がローカル変数の場合は、ローカル変数の型ではなく実際に代入されている値の型を見る
