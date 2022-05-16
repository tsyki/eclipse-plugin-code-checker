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
				// �����N���X�̏ꍇ��ClassInstanceCreation�A�ϐ��̏ꍇ��SimpleName�A�����_���̏ꍇ��LamdaExpression�A���\�b�h�̏ꍇ��MethodInvocation�ł���A�������Expression�ł���
				Expression arg = (Expression) node.arguments().get(i);
				// ���\�b�h�̌��ʂ��n����Ă���ꍇ�͎��ۂ̌^�̔��ʂ��ł��Ȃ����߁A�`�F�b�N���Ȃ�
				if (arg instanceof MethodInvocation) {
					continue;
				}
				// ���������ϐ����A���[�J���ϐ��̏ꍇ�A���̕ϐ��ɑ������Ă���^��LogicResponder�ł��邩�`�F�b�N
				// �t�B�[���h�⃁�\�b�h�̈����̏ꍇ��LogicResponder���n����邩�͕s���ł��邽�߃`�F�b�N���Ȃ��B
				// ���[�J���ϐ��̏ꍇ�ł�new�����l��ݒ肷��̂ł͂Ȃ��A���\�b�h�̌Ăяo�����ʂ�������ꍇ�̓`�F�b�N���Ȃ�
				if (arg instanceof SimpleName) {
					SimpleName variable = (SimpleName) arg;
					IBinding bind = variable.resolveBinding();
					if (bind instanceof IVariableBinding) {
						IVariableBinding varBind = (IVariableBinding) bind;
						// �t�B�[���h�������Ȃ�`�F�b�N���Ȃ�
						if (varBind.isField() || varBind.isParameter()) {
							continue;
						}
						// ���[�J���ϐ��̏ꍇ�͐e���\�b�h���擾���đ�����`�F�b�N
						MethodDeclaration method = getParentMethod(variable);
						// �����̃��[�J���ϐ��ł�key�͈ȉ��̂悤�ɈقȂ�
						// Lexample/SampleLogic;.run()V#0#r
						// Lexample/SampleLogic;.run()V#1#r
						VariableAssignVisitor visitor = new VariableAssignVisitor(bind.getKey());
						method.accept(visitor);
						if (visitor.existsNotLogicResponderAssigned()) {
							createMarker(node);
						}
					}
				} else {
					// �������ϐ��łȂ��ꍇ�͂��̎��̌^���`�F�b�N
					ITypeBinding typeBinding = ((Expression) arg).resolveTypeBinding();
					if (!isImplements(LOGIC_RESPONDER_FQCN, typeBinding)) {
						createMarker(node);
					}
				}
			}
		}
		return super.visit(node);
	}

	// �w��̕ϐ���LogicResponder�ȊO���������Ă��邩�ǂ������`�F�b�N����visitor
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
				// �����Ώۂ̕ϐ��ł��邩���`�F�b�N
				IBinding bind = ((SimpleName) leftSide).resolveBinding();
				if (!searchVariableKey.equals(bind.getKey())) {
					return false;
				}
				// new���Ă���ꍇ�͂��̃N���X�̌^���`�F�b�N
				if (rightSide instanceof ClassInstanceCreation) {
					ITypeBinding typeBinding = rightSide.resolveTypeBinding();
					if (!isImplements(LOGIC_RESPONDER_FQCN, typeBinding)) {
						return true;
					}
				}
				// �����_���̏ꍇ��LogicResponder�ł͂Ȃ��̂ŒT���I��
				else if (rightSide instanceof LambdaExpression) {
					existsNotLogicResponderAssigned = true;
					return true;
				}
				// ���\�b�h�A�ϐ��̑���̏ꍇ�͂���ȏ㌩�Ȃ�
			}
			return false;
		}

		// �ϐ��錾�Ɠ����ɑ�������ꍇ
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
		// �N���X�̐錾��������܂Őe��T��
		if (node.getParent() != null) {
			return getParentMethod(node.getParent());
		}
		return null;
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

	private static boolean isImplements(String fqcn, ITypeBinding binding) {
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
