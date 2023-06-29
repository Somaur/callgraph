package callgraph.callgraph;

import callgraph.callgraph.browser.BrowserManager;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class CallGraphGenerator {
    private static CallGraphGenerator instance;
    private final JSONArray nodes;
    private final JSONArray edges;
    private final JSONObject groups;
    private final HashMap<Integer, PsiElement> references = new HashMap<>();
    private PsiMethod lastGeneratedMethod;

    private CallGraphGenerator() {
        this.nodes = new JSONArray();
        this.edges = new JSONArray();
        this.groups = new JSONObject();
    }

    public static CallGraphGenerator getInstance() {
        if (instance == null) {
            instance = new CallGraphGenerator();
        }
        return instance;
    }

    public String generate(PsiMethod mainMethod) {
        BrowserManager.getInstance().showMessage("Clearing the graph...");
        clear();

        lastGeneratedMethod = mainMethod;
        references.put(mainMethod.hashCode(), mainMethod);

        JSONObject mainNode = createMethodNode(mainMethod, 0);
        mainNode.put("shape", "circle");

        createGroupIfNotExists(mainMethod);

        nodes.add(mainNode);

        BrowserManager.getInstance().showMessage("Collecting the callers...");
        findAndAddCallers(mainMethod, 1);

        BrowserManager.getInstance().showMessage("Collecting the callers completed. Generating the graph...");

        return getJson();
    }

    public String getJson() {
        JSONObject graph = new JSONObject();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        graph.put("groups", groups);

        return graph.toJSONString();
    }

    private void clear() {
        nodes.clear();
        edges.clear();
        groups.clear();
        references.clear();
    }

    private void findAndAddCallers(PsiMethod method, int depth) {
        Collection<PsiReference> allReferences = ReferencesSearch.search(method).findAll();
        for (PsiClass anInterface : method.getContainingClass().getInterfaces()) {
            PsiMethod methodBySignature = anInterface.findMethodBySignature(method, false);
            if (methodBySignature != null) {
                allReferences.addAll(ReferencesSearch.search(methodBySignature).findAll());
            }
        }
        for (PsiReference reference : allReferences) {
            PsiElement callReference = reference.getElement();
            PsiMethod caller = PsiTreeUtil.getParentOfType(callReference, PsiMethod.class);
            if (references.containsKey(callReference.hashCode())) continue;
            if (caller == null || !caller.getProject().equals(method.getProject())) continue;

            final boolean nodeNotExists = !references.containsKey(caller.hashCode());

            if (nodeNotExists) {
                references.put(caller.hashCode(), reference.getElement());
                JSONObject callerNode = createMethodNode(caller, depth);
                nodes.add(callerNode);
                createGroupIfNotExists(caller);
            }

            references.put(callReference.hashCode(), reference.getElement());
            JSONObject edge = createEdge(method, callReference, caller);
            edges.add(edge);

            findAndAddCallers(caller, depth + 1);
        }
    }

    @NotNull
    private JSONObject createEdge(PsiMethod method, PsiElement element, PsiMethod caller) {
        JSONObject edge = new JSONObject();
        edge.put("id", element.hashCode());
        edge.put("from", caller.hashCode());
        edge.put("to", method.hashCode());

        PsiFile file = element.getContainingFile();
        Document document = file.getViewProvider().getDocument();
        int lineNumber = document.getLineNumber(element.getTextOffset()) + 1;
        edge.put("label", ":" + lineNumber);

        JSONObject group = getGroup(caller);
        edge.put("font", group.get("font"));

        return edge;
    }

    private JSONObject createMethodNode(PsiMethod method, int depth) {
        JSONObject callerNode = new JSONObject();
        callerNode.put("id", method.hashCode());
        callerNode.put("group", method.getContainingClass().getQualifiedName());
        callerNode.put("title", method.getContainingClass().getQualifiedName() + "\n" + method.getName());
        callerNode.put("level", depth);

        String label = method.getContainingClass().getName() + "\n" + method.getName();

        PsiAnnotation controllerAnnotation = getControllerAnnotation(method);
        if (controllerAnnotation != null) {
            label = "*" + prepareServiceName(method, controllerAnnotation) + "*" + "\n" + label;
        }

        callerNode.put("label", label);

        return callerNode;
    }

    private PsiAnnotation getControllerAnnotation(PsiJvmModifiersOwner method) {
        PsiAnnotation[] annotations = method.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (annotation.getQualifiedName().contains("GetMapping") || annotation.getQualifiedName().contains("PostMapping") || annotation.getQualifiedName().contains("PutMapping") || annotation.getQualifiedName().contains("DeleteMapping") || annotation.getQualifiedName().contains("RequestMapping")) {
                return annotation;
            }
        }
        return null;
    }

    private String prepareServiceName(PsiMethod method, PsiAnnotation annotation) {
        String serviceName = "";

        PsiAnnotation classRequestMappingAnnotation = getControllerAnnotation(method.getContainingClass());
        if (classRequestMappingAnnotation != null) {
            PsiAnnotationMemberValue value = classRequestMappingAnnotation.findAttributeValue("value");

            if (value != null) {
                String classPath = value.getText();
                classPath = fixControllerPath(classPath.substring(1, classPath.length() - 1));

                serviceName = "/" + classPath;
            }
        }

        PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
        if (value != null) {
            String methodPath = value.getText();
            methodPath = fixControllerPath(methodPath.substring(1, methodPath.length() - 1));

            serviceName = serviceName + "/" + methodPath;
        }

        return serviceName;
    }

    private String fixControllerPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private void createGroupIfNotExists(PsiMethod method) {
        if (!groups.containsKey(method.getContainingClass().getQualifiedName())) {
            JSONObject group = new JSONObject();

            JSONObject color = new JSONObject();
            String randomColorFromPalette = Utils.getRandomColorFromPalette();
            color.put("background", randomColorFromPalette);
            color.put("border", randomColorFromPalette);

            JSONObject hoverAndHighlightColor = new JSONObject();
            hoverAndHighlightColor.put("background", Utils.darkenHexColor(randomColorFromPalette, 0.1));
            hoverAndHighlightColor.put("border", Utils.darkenHexColor(randomColorFromPalette, 0.2));

            color.put("highlight", hoverAndHighlightColor);
            color.put("hover", hoverAndHighlightColor);

            JSONObject font = new JSONObject();
            font.put("color", Utils.getTextColorFromBackground(randomColorFromPalette));
            font.put("strokeColor", randomColorFromPalette);

            group.put("color", color);
            group.put("font", font);

            groups.put(method.getContainingClass().getQualifiedName(), group);
        }
    }

    private JSONObject getGroup(PsiMethod method) {
        return (JSONObject) groups.get(method.getContainingClass().getQualifiedName());
    }

    public PsiElement getReference(Integer hashCode) {
        return references.get(hashCode);
    }

    public PsiMethod getLastGeneratedMethod() {
        return lastGeneratedMethod;
    }
}
