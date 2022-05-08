package jp.gr.java_conf.tsyki.codechecker.visitor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
				// 匿名クラスの場合はClassInstanceCreation、変数の場合はSimpleNameであり、どちらもExpressionである
				Expression arg = (Expression) node.arguments().get(i);
				ITypeBinding typeBinding = ((Expression) arg).resolveTypeBinding();
				if (!isImplements(LOGIC_RESPONDER_FQCN, typeBinding)) {
					createMarker(node);
				}
			}
		}
		return super.visit(node);
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

	private boolean isImplements(String fqcn, ITypeBinding binding) {
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
