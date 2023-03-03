package callgraph.callgraph;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class GenerateCallGraphAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();

// Get the current PSI file from the editor
        PsiFile psiFile = PsiDocumentManager.getInstance(e.getProject()).getPsiFile(editor.getDocument());

// Get the current caret position in the editor
        int offset = editor.getCaretModel().getOffset();

// Find the method containing the caret position
        PsiElement element = psiFile.findElementAt(offset);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        System.out.println(method.getName());
    }
}
