package jp.gr.java_conf.tsyki.codechecker.handlers;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;

public class CodeCheckHandler extends AbstractHandler {

	// �z����ICompilationUnit���ċA�I�Ɉ�����compilationUnits�ɒǉ�����
	private void addCompilationUnits(Object target, Collection<ICompilationUnit> compilationUnits) {
		// java�t�@�C���I������target�̌^��CompilationUnit�ł���A���̂܂ܒǉ�
		// NOTE CompilationUnit��IParent���������Ă��邽�ߐ�ɔ���
		if(target instanceof ICompilationUnit) {
			compilationUnits.add((ICompilationUnit) target);
		}
		// �v���W�F�N�g�I������target�̌^��JavaProject�ł���AIParent���������Ă���B
		// src�t�H���_�I������target�̌^��PackageFragmentRoot�ł���AIParent���������Ă���B
		// src�t�H���_�I������target�̌^��PackageFragment�ł���AIParent���������Ă���B
		else if(target instanceof IParent) {
			// �v���W�F�N�g��I�������ۂ�getChildren�̒��ɂ�jar�̒��g���܂܂�Ă���A�`�F�b�N�s�v�Ȃ��߂��̏ꍇ�͏I������
			if(isBinaryPackage(target)) {
				return;
			}
			IJavaElement[] children;
			try {
				children = ((IParent) target).getChildren();
			} catch (JavaModelException e) {
				e.printStackTrace();
				return;
			}
			for(IJavaElement child: children) {
				addCompilationUnits(child, compilationUnits);
			}
		}

	}

	// �Ώۃp�b�P�[�W��jar�̒��ɂ��邩�ǂ���
	private boolean isBinaryPackage(Object target) {
		if(target instanceof IPackageFragmentRoot) {
			try {
				if(((IPackageFragmentRoot) target).getKind() == IPackageFragmentRoot.K_BINARY) {
					return true;
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
				return true;
			}
		}
		return false;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// �E�N���b�N���j���[�őI�����ꂽ�Ώۂ��擾
		ISelection selection = HandlerUtil.getActiveMenuSelectionChecked(event);
		Collection<ICompilationUnit> targetJavaFiles = new ArrayList<>();
		// �I�����ꂽ�Ώ۔z����java�t�@�C���̏����擾
		if(selection instanceof TreeSelection) {
			Object selected = ((TreeSelection) selection).getFirstElement();
			addCompilationUnits(selected, targetJavaFiles);
		}
		// �擾�����t�@�C�������o��
		StringBuilder sb = new StringBuilder();
		for(ICompilationUnit targetJavaFile : targetJavaFiles) {
			sb.append(targetJavaFile.getElementName());
			sb.append("\n");
		}
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(
				window.getShell(),
				"Selected",
				sb.toString());

		return null;
	}
}
