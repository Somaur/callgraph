package callgraph.callgraph;

import com.intellij.lang.jvm.JvmNamedElement;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class MyToolWindowFactory implements ToolWindowFactory {

    private mxGraphComponent graphComponent;

    private HashMap<String, String> fromTo;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Create the graph component
        mxGraph graph = new mxGraph();
        graphComponent = new mxGraphComponent(graph);

        // Add the graph component to the tool window content pane
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(graphComponent, "", false);
        toolWindow.getContentManager().addContent(content);

        // Add an action to update the graph
        AnAction updateGraphAction = new AnAction("Update Graph") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                // Get the current method
                Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
                int offset = editor.getCaretModel().getOffset();
                PsiElement element = psiFile.findElementAt(offset);
                PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

                // Update the graph with the callers of the current method
                graph.getModel().beginUpdate();
                try {
                    graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
                    fromTo = new HashMap<>();
                    addCallersToGraph(method, null, graph);
                } finally {
                    mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
                    layout.execute(graph.getDefaultParent());
                    graph.getModel().endUpdate();
                    graphComponent.refresh();
                }
            }
        };

        // Add the action to the tool window toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(updateGraphAction);
        toolWindow.setTitleActions(List.of(actionGroup));
    }

    private void addCallersToGraph(PsiMethod method, mxCell methodVertex, mxGraph graph) {
        // Add the current method to the graph
        Object parent = graph.getDefaultParent();

        if (Objects.isNull(methodVertex)) {
            methodVertex = (mxCell) graph.insertVertex(parent, null, method.getContainingClass().getName() + "\n" + method.getName(), 50, 50, 200, 35, "fillColor=#ee5253;fontColor=white;strokeColor=white");
        }

        // Add the callers of the current method to the graph
        Collection<PsiReference> allReferences = MethodReferencesSearch.search(method).findAll();

        if (allReferences.isEmpty()) {
            PsiMethod[] superMethods = method.findSuperMethods();
            if (superMethods.length > 0) {
                PsiMethod superMethod = superMethods[0];
                if (PsiManager.getInstance(method.getProject()).isInProject(superMethod)) {
                    addCallersToGraph(superMethod, methodVertex, graph);
                    return;
                }
            }
        }

        for (PsiReference reference : allReferences) {
            PsiElement element = reference.getElement();
            if (element instanceof PsiReferenceExpression) {
                PsiMethod caller = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
                if (caller != null) {
                    if (Objects.equals(fromTo.get(caller.getContainingClass().getName() + "." + caller.getName()), method.getContainingClass().getName() + "." + method.getName())) {
                        continue;
                    }

                    fromTo.put(caller.getContainingClass().getName() + "." + caller.getName(), method.getContainingClass().getName() + "." + method.getName());

                    String color = "#8395a7";
                    String callerNameLowercase = caller.getContainingClass().getName().toLowerCase();
                    if (callerNameLowercase.contains("controller")) {
                        color = "#00d2d3";
                    } else if (callerNameLowercase.contains("service")) {
                        color = "#54a0ff";
                    } else if (callerNameLowercase.contains("facade")) {
                        color = "#ff9f43";
                    } else if (callerNameLowercase.contains("dao")) {
                        color = "#5f27cd";
                    }

                    mxCell callerVertex = (mxCell) graph.insertVertex(parent, null, caller.getContainingClass().getName() + "\n" + caller.getName(), 0, 0, 200, 35, "fillColor=" + color + ";fontColor=white;strokeColor=white");

                    String parameters = Arrays.stream(method.getParameters()).map(JvmNamedElement::getName).collect(Collectors.joining(", "));

                    graph.insertEdge(parent, null, parameters, callerVertex, methodVertex, "strokeColor=" + color + ";fontColor=white");
                    addCallersToGraph(caller, callerVertex, graph);
                }
            }
        }
    }
}
