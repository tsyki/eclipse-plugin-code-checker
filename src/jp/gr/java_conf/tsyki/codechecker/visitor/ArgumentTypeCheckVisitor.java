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
 * ����̃C���^�t�F�[�X(Logic)�����������N���X�œ���̌^�̈���(Responder)�������\�b�h���Ăяo�����ۂɁA���̎�����������̌^(LogicResponder)���������Ă��Ȃ���Όx�����o��visitor
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
		// ���g�̌^��Logic�����������^�łȂ���΃`�F�b�N���Ȃ��ŏI���
		if (!isImplements(LOGIC_FQCN, node)) {
			return super.visit(node);
		}
		// ������Responder�ł��郁�\�b�h���Ăяo���Ă���ꍇ�̓`�F�b�N�Ώ�
		IMethodBinding methodBinding = node.resolveMethodBinding();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			ITypeBinding parameterType = parameterTypes[i];
			// �����̌^��Responder�ł���ꍇ�A�������̌^��LogicResponder���ǂ������`�F�b�N
			if (RESPONDER_FQCN.equals(parameterType.getQualifiedName())) {
				// �����N���X�̏ꍇ��ClassInstanceCreation�A�ϐ��̏ꍇ��SimpleName�ł���A�ǂ����Expression�ł���
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
			marker.setAttribute(IMarker.MESSAGE, "Logic�N���X��Responder�𗘗p����ۂ�LogicResponder�𗘗p���Ă�������");
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
		// ���g���Y���N���X�ł���Ȃ�I���
		if (fqcn.equals(binding.getQualifiedName())) {
			return true;
		}
		// ���g���������Ă���C���^�[�t�F�[�X���ċA�I�ɒT��
		for (ITypeBinding implementInterface : binding.getInterfaces()) {
			if (isImplements(fqcn, implementInterface)) {
				return true;
			}
		}
		// ���g�̐e�N���X���ċA�I�ɒT��
		ITypeBinding superClass = binding.getSuperclass();
		return isImplements(fqcn, superClass);
	}

	private boolean isImplements(String fqcn, ASTNode node) {
		// �N���X�ł���Ȃ�A�ΏۃC���^�t�F�[�X���������Ă��邩�ǂ������ċA�I�ɒT��
		if (node instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) node;
			ITypeBinding binding = type.resolveBinding();
			return isImplements(fqcn, binding);
		}
		// �N���X�̐錾��������܂Őe��T��
		if (node.getParent() != null) {
			return isImplements(fqcn, node.getParent());
		}
		return false;
	}
}
