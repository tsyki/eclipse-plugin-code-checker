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

	// 配下のICompilationUnitを再帰的に引数のcompilationUnitsに追加する
	private void addCompilationUnits(Object target, Collection<ICompilationUnit> compilationUnits) {
		// javaファイル選択時のtargetの型はCompilationUnitであり、そのまま追加
		// NOTE CompilationUnitはIParentも実装しているため先に判定
		if(target instanceof ICompilationUnit) {
			compilationUnits.add((ICompilationUnit) target);
		}
		// プロジェクト選択時のtargetの型はJavaProjectであり、IParentを実装している。
		// srcフォルダ選択時のtargetの型はPackageFragmentRootであり、IParentを実装している。
		// srcフォルダ選択時のtargetの型はPackageFragmentであり、IParentを実装している。
		else if(target instanceof IParent) {
			// プロジェクトを選択した際のgetChildrenの中にはjarの中身も含まれており、チェック不要なためその場合は終了する
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

	// 対象パッケージがjarの中にあるかどうか
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
		// 右クリックメニューで選択された対象を取得
		ISelection selection = HandlerUtil.getActiveMenuSelectionChecked(event);
		Collection<ICompilationUnit> targetJavaFiles = new ArrayList<>();
		// 選択された対象配下のjavaファイルの情報を取得
		if(selection instanceof TreeSelection) {
			Object selected = ((TreeSelection) selection).getFirstElement();
			addCompilationUnits(selected, targetJavaFiles);
		}
		// 取得したファイル名を出力
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
