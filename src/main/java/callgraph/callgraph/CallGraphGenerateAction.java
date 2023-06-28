package callgraph.callgraph;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class CallGraphGenerateAction extends AnAction {
    private final BrowserManager browserManager;
    private final CallGraphGenerator generator;

    public CallGraphGenerateAction(BrowserManager browserManager, CallGraphGenerator generator) {
        super("Generate Call Graph");
        this.browserManager = browserManager;
        this.generator = generator;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

        // Do the graph creation in separate thread with progress bar
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating Call Graph") {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                ApplicationManager.getApplication().runReadAction(() -> {
                    String graph = generator.generate(method);
                    browserManager.showMessage("Sending graph to embedded browser...");
                    browserManager.updateNetwork(graph);
                });
            }
        });
    }
}
