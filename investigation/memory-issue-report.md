# CallGraph 内存暴涨问题调查报告

## 问题描述

当尝试为某些底层函数（如 `toString()`、`equals()`、`hashCode()` 等常用方法）生成调用图时，插件会发生内存暴涨（>18GB）的情况。

## 问题定位

问题出在 `src/main/java/callgraph/callgraph/CallGraphGenerator.java` 文件的 `findAndAddCallers` 方法中。

## 根本原因分析

### 1. 无深度限制的递归遍历

```java
private void findAndAddCallers(PsiMethod method, int depth) {
    // ... 
    for (PsiReference reference : allReferences) {
        // ...
        findAndAddCallers(caller, depth + 1);  // ❌ 无限递归，没有深度限制
    }
}
```

**问题**：虽然传入了 `depth` 参数，但代码中**从未使用它来限制递归深度**。这意味着递归会一直进行，直到遍历完所有可达的调用者。

### 2. 不完整的环检测机制

```java
if (references.containsKey(callReference.hashCode())) continue;  // 只检查调用点
// ...
final boolean nodeNotExists = !references.containsKey(caller.hashCode());  // 检查方法节点
if (nodeNotExists) {
    references.put(caller.hashCode(), reference.getElement());
    // ...
}
// ...
findAndAddCallers(caller, depth + 1);  // ❌ 即使节点已存在，仍然递归！
```

**问题**：即使 `caller` 方法节点已经存在（`nodeNotExists == false`），代码仍然会调用 `findAndAddCallers(caller, depth + 1)` 进行递归。这会导致：
- 同一个方法被多次递归处理
- 在存在循环调用的情况下，可能导致无限递归

### 3. 底层方法的爆炸性增长

对于底层方法（如 `Object.toString()`、`String.equals()` 等），问题会呈指数级放大：

```
假设一个底层方法 M 有 1000 个直接调用者
每个调用者平均又有 10 个调用者
递归 5 层后：1000 × 10^5 = 10亿个节点需要处理
```

### 4. 接口方法的额外搜索加剧问题

```java
for (PsiClass anInterface : method.getContainingClass().getInterfaces()) {
    PsiMethod methodBySignature = anInterface.findMethodBySignature(method, false);
    if (methodBySignature != null) {
        allReferences.addAll(ReferencesSearch.search(methodBySignature).findAll());
    }
}
```

这段代码会搜索接口中同签名方法的所有引用，进一步增加了需要处理的引用数量。

## 内存消耗分析

每次递归调用会产生以下内存消耗：

| 数据结构 | 单次消耗 | 说明 |
|---------|---------|------|
| `JSONObject` (node) | ~200-500 bytes | 每个方法节点 |
| `JSONObject` (edge) | ~100-200 bytes | 每条边 |
| `PsiReference` 集合 | ~1-10 KB | `ReferencesSearch.search().findAll()` 返回的集合 |
| 递归栈帧 | ~1-2 KB | 每层递归的栈帧 |
| `HashMap` entries | ~50-100 bytes | `references` 中的每个条目 |

对于一个有 10000 个调用者的底层方法，仅第一层就可能消耗：
- 节点：10000 × 500 bytes = 5 MB
- 边：10000 × 200 bytes = 2 MB
- 引用搜索：10000 × 5 KB = 50 MB

递归多层后，内存消耗呈指数增长，很容易达到 18GB+。

## 问题代码位置

**文件**：`src/main/java/callgraph/callgraph/CallGraphGenerator.java`

**方法**：`findAndAddCallers(PsiMethod method, int depth)`

**关键缺陷代码**：

```java
private void findAndAddCallers(PsiMethod method, int depth) {
    Collection<PsiReference> allReferences = ReferencesSearch.search(method).findAll();
    // ... 接口方法搜索 ...
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

        findAndAddCallers(caller, depth + 1);  // ❌ 无条件递归！即使节点已存在也会递归
    }
}
```

## 修复建议

### 方案 1：添加深度限制（推荐）

```java
private static final int MAX_DEPTH = 10;  // 可配置

private void findAndAddCallers(PsiMethod method, int depth) {
    if (depth > MAX_DEPTH) {
        return;  // 超过最大深度，停止递归
    }
    // ... 其余代码
}
```

### 方案 2：修复环检测逻辑

```java
private void findAndAddCallers(PsiMethod method, int depth) {
    // ...
    for (PsiReference reference : allReferences) {
        // ...
        final boolean nodeNotExists = !references.containsKey(caller.hashCode());

        if (nodeNotExists) {
            references.put(caller.hashCode(), caller);  // 存储方法本身，而非 reference.getElement()
            JSONObject callerNode = createMethodNode(caller, depth);
            nodes.add(callerNode);
            createGroupIfNotExists(caller);
            
            // ✅ 只有新节点才递归
            findAndAddCallers(caller, depth + 1);
        }

        // 边的处理保持不变
        references.put(callReference.hashCode(), reference.getElement());
        JSONObject edge = createEdge(method, callReference, caller);
        edges.add(edge);
    }
}
```

### 方案 3：添加节点数量限制

```java
private static final int MAX_NODES = 500;  // 可配置

private void findAndAddCallers(PsiMethod method, int depth) {
    if (nodes.size() >= MAX_NODES) {
        BrowserManager.getInstance(project).showMessage(
            "Warning: Graph truncated at " + MAX_NODES + " nodes to prevent memory issues."
        );
        return;
    }
    // ... 其余代码
}
```

### 方案 4：综合方案（最佳实践）

结合以上所有方案，并添加用户可配置的选项：

1. 最大深度限制（默认 10）
2. 最大节点数限制（默认 500）
3. 正确的环检测（只对新节点递归）
4. 可选：排除常见底层方法（如 `java.lang.Object` 的方法）

## 结论

插件的算法确实存在严重问题：

1. **深度参数形同虚设**：`depth` 参数被传递但从未用于限制递归
2. **环检测逻辑有缺陷**：即使节点已存在，仍会进行递归
3. **缺乏安全边界**：没有节点数量限制

这些问题导致在处理底层方法时，算法会尝试遍历整个代码库的调用链，造成内存暴涨。

## 优先级

**高优先级** - 此问题会导致 IDE 崩溃或系统内存耗尽，严重影响用户体验。

## 相关文件

- `src/main/java/callgraph/callgraph/CallGraphGenerator.java` - 核心问题所在
- `src/main/java/callgraph/callgraph/browser/handlers/GenerateGraphHandler.java` - 调用入口
- `src/main/java/callgraph/callgraph/actions/GenerateCallGraphAction.java` - Action 入口

---

## 修复记录

### 修复日期：2026-02-05

### 已实施的修复方案

#### 方案 1：添加深度限制 ✅

**修改文件**：
- `src/main/java/callgraph/callgraph/settings/CallGraphSettings.java`
- `src/main/java/callgraph/callgraph/CallGraphGenerator.java`

**实现内容**：
1. 在 `CallGraphSettings` 中添加了 `maxDepth` 配置项：
   - 默认值：10
   - 最小值：1
   - 最大值：100
   - 支持持久化存储

2. 在 `CallGraphGenerator.findAndAddCallers()` 方法开头添加深度检查：
   ```java
   int maxDepth = CallGraphSettings.getInstance(project).getMaxDepth();
   if (depth > maxDepth) {
       if (!depthLimitReached) {
           depthLimitReached = true;
           BrowserManager.getInstance(project).showMessage(
               "Warning: Maximum depth (" + maxDepth + ") reached. Some callers may not be shown."
           );
       }
       return;
   }
   ```

#### 方案 2：修复环检测逻辑 ✅

**修改文件**：
- `src/main/java/callgraph/callgraph/CallGraphGenerator.java`

**实现内容**：
1. 添加了 `processedMethods` HashSet 来追踪已递归处理过的方法
2. 修复了 `references` HashMap 的存储逻辑：
   - **节点 (caller.hashCode())**：存储 `caller` 方法本身，用于点击节点跳转到方法定义
   - **边 (callReference.hashCode())**：存储 `reference.getElement()` 调用点，用于点击边跳转到调用位置
3. 只有未处理过的方法才会进行递归：
   ```java
   if (!processedMethods.contains(caller.hashCode())) {
       processedMethods.add(caller.hashCode());
       findAndAddCallers(caller, depth + 1);
   }
   ```

**关于方案 2 的跳转逻辑分析**：

原始代码中 `caller.hashCode()` 存储的是 `reference.getElement()`（调用点），这实际上是一个 bug：
- 节点的 id 是 `method.hashCode()`
- 点击节点应该跳转到方法定义，而不是某个调用点
- 修复后，节点存储方法本身，边存储调用点，逻辑更加正确

#### UI 配置支持 ✅

**修改文件**：
- `src/main/java/callgraph/callgraph/settings/CallGraphSettingsDialog.java`

**实现内容**：
1. 添加了 "Performance" 标签页
2. 提供了 `maxDepth` 的 Spinner 控件，支持用户自定义递归深度
3. 添加了说明文字，解释该配置的作用和取值范围

### 修复后的代码行为

1. **深度限制**：递归深度超过配置的 `maxDepth` 时，停止递归并显示警告消息
2. **环检测**：同一个方法只会被递归处理一次，避免无限循环
3. **正确的跳转**：
   - 点击节点 → 跳转到方法定义
   - 点击边 → 跳转到调用位置
4. **用户可配置**：通过设置对话框的 "Performance" 标签页可以调整最大深度

### 预期效果

- 内存使用量将大幅降低，不再出现 18GB+ 的内存暴涨
- 对于底层方法，图表会在达到最大深度时停止扩展，并给出提示
- 用户可以根据需要调整深度限制
