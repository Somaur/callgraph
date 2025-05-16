package callgraph.callgraph.actions;

import callgraph.callgraph.CallGraphGenerator;
import callgraph.callgraph.Utils;
import callgraph.callgraph.browser.BrowserManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * An action that generates a call graph for the method at the current cursor position.
 * This action is available in the editor context menu when the cursor is on a method.
 */
public class GenerateCallGraphAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // Check if indexing is in progress (dumb mode)
        if (DumbService.isDumb(project)) {
            Messages.showInfoMessage(project,
                    "Cannot generate call graph while indexing is in progress.\nPlease wait for indexing to complete.",
                    "Call Graph Generation");
            return;
        }

        PsiMethod method = Utils.getMethodAtCaret(project, e.getData(CommonDataKeys.EDITOR));
        if (method == null) return;

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CallGraph");
        if (toolWindow != null) {
            toolWindow.show(() -> {
                generateCallGraph(project, method);
            });
        }
    }
    
    /**
     * Generate the call graph for the given method.
     * This is extracted into a separate method to be called after the tool window is shown.
     */
    private void generateCallGraph(Project project, PsiMethod method) {
        BrowserManager browserManager = BrowserManager.getInstance(project);
        
        browserManager.whenBrowserReady(() -> {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating Call Graph") {
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    ApplicationManager.getApplication().runReadAction(() -> {
                        browserManager.showMessage("Generating call graph for " + method.getName() + "...");
                        
                        String graph = CallGraphGenerator.getInstance(project).generate(method);
                        
                        browserManager.showMessage("Sending graph to embedded browser...");
                        browserManager.updateNetwork(graph);

                        browserManager.setGenerateMessage("+FOR " + method.getName());
                    });
                }
            });
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // Always show the action, but it might be disabled during indexing
        e.getPresentation().setVisible(true);

        // Check if indexing is in progress (dumb mode)
        if (DumbService.isDumb(project)) {
            e.getPresentation().setEnabled(false);
            // Optionally set a different tooltip explaining why the action is disabled
            e.getPresentation().setDescription("Cannot generate call graph while indexing is in progress");
            return;
        }

        PsiMethod method = Utils.getMethodAtCaret(project, e.getData(CommonDataKeys.EDITOR));
        e.getPresentation().setEnabled(method != null);
    }
}
