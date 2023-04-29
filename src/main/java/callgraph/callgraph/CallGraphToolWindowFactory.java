package callgraph.callgraph;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public final class CallGraphToolWindowFactory implements ToolWindowFactory {
    private CallGraphGenerator generator;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JBCefBrowser browser = new JBCefBrowser();
        browser.loadHTML(loadHtmlFile());
        Disposer.register(project, browser);

        JComponent component = browser.getComponent();
        Content content = toolWindow.getContentManager().getFactory().createContent(component, null, false);
        toolWindow.getContentManager().addContent(content);

        AnAction updateGraphAction = new AnAction("Update Graph") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
                int offset = editor.getCaretModel().getOffset();
                PsiElement element = psiFile.findElementAt(offset);
                PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

                // Do the graph creation in separate thread with progress bar
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating Call Graph") {
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            generator = new CallGraphGenerator(method);
                            String graph = generator.generate();
                            browser.getCefBrowser().executeJavaScript(String.format("updateNetwork(%s)", graph), browser.getCefBrowser().getURL(), 0);
                        });
                    }
                });
            }
        };

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(updateGraphAction);
        toolWindow.setTitleActions(List.of(actionGroup));
    }

    private String loadHtmlFile() {
        try {
            return Utils.getResourceFileAsString("callgraph.html");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
