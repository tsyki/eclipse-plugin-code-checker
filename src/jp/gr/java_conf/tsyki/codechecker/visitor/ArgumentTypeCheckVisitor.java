package jp.gr.java_conf.tsyki.codechecker.visitor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * 特定のインタフェース(Logic)を実装したクラスで特定の型の引数(Responder)を持つメソッドを呼び出した際に、その実引数が特定の型(LogicResponder)を実装していなければ警告を出すvisitor
 */
public class ArgumentTypeCheckVisitor extends ASTVisitor {
	private ICompilationUnit unit;

	private static final String LOGIC_FQCN = "example.Logic";

	private static final String RESPONDER_FQCN = "example.Responder";

	private static final String LOGIC_RESPONDER_FQCN = "example.LogicResponder";

	public ArgumentTypeCheckVisitor(ICompilationUnit unit) {
		this.unit = unit;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		// 自身の型がLogicを実装した型でなければチェックしないで終わり
		if (!isImplements(LOGIC_FQCN, node)) {
			return super.visit(node);
		}
		// 引数がResponderであるメソッドを呼び出している場合はチェック対象
		IMethodBinding methodBinding = node.resolveMethodBinding();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			ITypeBinding parameterType = parameterTypes[i];
			// 引数の型がResponderである場合、実引数の型がLogicResponderかどうかをチェック
			if (RESPONDER_FQCN.equals(parameterType.getQualifiedName())) {
				// 匿名クラスの場合はClassInstanceCreation、変数の場合はSimpleName、ラムダ式の場合はLamdaExpression、メソッドの場合はMethodInvocationであり、いずれもExpressionである
				Expression arg = (Expression) node.arguments().get(i);
				// メソッドの結果が渡されている場合は実際の型の判別ができないため、チェックしない
				if (arg instanceof MethodInvocation) {
					continue;
				}
				// 実引数が変数かつ、ローカル変数の場合、その変数に代入されている型がLogicResponderであるかチェック
				// フィールドやメソッドの引数の場合はLogicResponderが渡されるかは不明であるためチェックしない。
				// ローカル変数の場合でもnewした値を設定するのではなく、メソッドの呼び出し結果を代入する場合はチェックしない
				if (arg instanceof SimpleName) {
					SimpleName variable = (SimpleName) arg;
					IBinding bind = variable.resolveBinding();
					if (bind instanceof IVariableBinding) {
						IVariableBinding varBind = (IVariableBinding) bind;
						// フィールドか引数ならチェックしない
						if (varBind.isField() || varBind.isParameter()) {
							continue;
						}
						// ローカル変数の場合は親メソッドを取得して代入をチェック
						MethodDeclaration method = getParentMethod(variable);
						// 同名のローカル変数でもkeyは以下のように異なる
						// Lexample/SampleLogic;.run()V#0#r
						// Lexample/SampleLogic;.run()V#1#r
						VariableAssignVisitor visitor = new VariableAssignVisitor(bind.getKey());
						method.accept(visitor);
						if (visitor.existsNotLogicResponderAssigned()) {
							createMarker(node);
						}
					}
				} else {
					// 引数が変数でない場合はその式の型をチェック
					ITypeBinding typeBinding = ((Expression) arg).resolveTypeBinding();
					if (!isImplements(LOGIC_RESPONDER_FQCN, typeBinding)) {
						createMarker(node);
					}
				}
			}
		}
		return super.visit(node);
	}

	// 指定の変数にLogicResponder以外が代入されているかどうかをチェックするvisitor
	private static class VariableAssignVisitor extends ASTVisitor {
		private String searchVariableKey;

		private boolean existsNotLogicResponderAssigned;

		public VariableAssignVisitor(String searchVariableKey) {
			this.searchVariableKey = searchVariableKey;
		}

		public boolean existsNotLogicResponderAssigned() {
			return existsNotLogicResponderAssigned;
		}

		private boolean isNotLogicResponderAssigned(Expression leftSide, Expression rightSide) {
			if (leftSide instanceof SimpleName) {
				// 調査対象の変数であるかをチェック
				IBinding bind = ((SimpleName) leftSide).resolveBinding();
				if (!searchVariableKey.equals(bind.getKey())) {
					return false;
				}
				// newしている場合はそのクラスの型をチェック
				if (rightSide instanceof ClassInstanceCreation) {
					ITypeBinding typeBinding = rightSide.resolveTypeBinding();
					if (!isImplements(LOGIC_RESPONDER_FQCN, typeBinding)) {
						return true;
					}
				}
				// ラムダ式の場合はLogicResponderではないので探索終了
				else if (rightSide instanceof LambdaExpression) {
					existsNotLogicResponderAssigned = true;
					return true;
				}
				// メソッド、変数の代入の場合はそれ以上見ない
			}
			return false;
		}

		// 変数宣言と同時に代入した場合
		@Override
		public boolean visit(VariableDeclarationFragment node) {
			SimpleName leftSide = node.getName();
			Expression rightSide = node.getInitializer();
			if (isNotLogicResponderAssigned(leftSide, rightSide)) {
				existsNotLogicResponderAssigned = true;
				return false;
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(Assignment node) {
			Expression leftSide = node.getLeftHandSide();
			Expression rightSide = node.getRightHandSide();
			if (isNotLogicResponderAssigned(leftSide, rightSide)) {
				existsNotLogicResponderAssigned = true;
				return false;
			}
			return super.visit(node);
		}
	}

	private MethodDeclaration getParentMethod(ASTNode node) {
		if (node instanceof MethodDeclaration) {
			return (MethodDeclaration) node;
		}
		// クラスの宣言が見つかるまで親を探索
		if (node.getParent() != null) {
			return getParentMethod(node.getParent());
		}
		return null;
	}

	private void createMarker(MethodInvocation node) {
		try {
			IMarker marker = unit.getCorrespondingResource().createMarker(CodeCheckerConstants.MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, "LogicクラスでResponderを利用する際はLogicResponderを利用してください");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(IMarker.LINE_NUMBER,
					((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()));
			marker.setAttribute(IMarker.CHAR_START, node.getStartPosition());
			marker.setAttribute(IMarker.CHAR_END, node.getStartPosition() + node.getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean isImplements(String fqcn, ITypeBinding binding) {
		if (binding == null) {
			return false;
		}
		// 自身が該当クラスであるなら終わり
		if (fqcn.equals(binding.getQualifiedName())) {
			return true;
		}
		// 自身が実装しているインターフェースを再帰的に探索
		for (ITypeBinding implementInterface : binding.getInterfaces()) {
			if (isImplements(fqcn, implementInterface)) {
				return true;
			}
		}
		// 自身の親クラスを再帰的に探索
		ITypeBinding superClass = binding.getSuperclass();
		return isImplements(fqcn, superClass);
	}

	private boolean isImplements(String fqcn, ASTNode node) {
		// クラスであるなら、対象インタフェースを実装しているかどうかを再帰的に探索
		if (node instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) node;
			ITypeBinding binding = type.resolveBinding();
			return isImplements(fqcn, binding);
		}
		// クラスの宣言が見つかるまで親を探索
		if (node.getParent() != null) {
			return isImplements(fqcn, node.getParent());
		}
		return false;
	}
}
