package MinRi2.ContentsEditor.node;

import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;
import org.jetbrains.annotations.Nullable;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeData{
    private static NodeData rootData;

    public final CTNode node;
    public final String nodeName;
    public JsonValue jsonData;

    protected ObjectMap<String, NodeData> children = new ObjectMap<>();

    /** Null when {@link CTNode} is Root */
    protected @Nullable NodeData parentData;

    private NodeData(String nodeName, CTNode node){
        this.nodeName = nodeName;
        this.node = node;
    }

    public static NodeData getRootData(){
        if(rootData == null){
            rootData = new NodeData("Root", NodeHelper.root);
            rootData.jsonData = new JsonValue(ValueType.object);
        }
        return rootData;
    }

    /**
     * Function 'addChild' doesn't set the child's previous jsonValue.
     * see {@link JsonValue}
     */
    public static void addChildValue(JsonValue jsonValue, String name, JsonValue childValue){
        childValue.name = name;
        childValue.parent = jsonValue;

        JsonValue current = jsonValue.child;
        if(current == null){
            jsonValue.child = childValue;
        }else{
            while(true){
                if(current.next == null){
                    current.next = childValue;
                    childValue.prev = current;
                    return;
                }
                current = current.next;
            }
        }
    }

    public void initJsonData(){
        if(parentData == null || jsonData != null){
            return;
        }

        parentData.initJsonData();

        // 都是对象
        JsonValue jsonData = new JsonValue(ValueType.object);
        addChildValue(parentData.jsonData, nodeName, jsonData);

        this.jsonData = jsonData;
    }

    public NodeData getChild(String childName){
        node.collectAll();

        CTNode child = node.getChildren().get(childName);

        if(child != null){
            return children.get(childName, () -> {
                NodeData nodeData = new NodeData(childName, child);
                nodeData.setParent(this);

                return nodeData;
            });
        }

        if(!childName.startsWith("#")){
            ObjInfo<?> objInfo = getObjInfo();
            if(objInfo != null && objInfo.getElementType() != null){
                return getChild("#" + childName);
            }
        }

        Log.warn("'@' don't have child '@'", nodeName, childName);

        return null;
    }

    public void setParent(NodeData parentData){
        this.parentData = parentData;
    }

    public JsonValue getJson(String name, ValueType valueType){
        initJsonData();

        JsonValue data = jsonData.get(name);

        if(data != null){
            return data;
        }

        // 对象，数组特殊类型需要创建JsonValue
        if(valueType == ValueType.object || valueType == ValueType.array){
            data = new JsonValue(valueType);
            addChildValue(jsonData, name, data);
            return data;
        }else{
            return jsonData;
        }
    }

    public void readJson(){
        for(JsonValue childData : jsonData){
            String jsonName = childData.name;

            if(jsonName.contains(".")){
                String[] names = jsonName.split("\\.");

                readDotJson(names, childData);

                continue;
            }

            NodeData nodeData = getChild(jsonName);

            if(nodeData == null){
                Log.warn("'@' don't have child '@'", getObjInfo().getObj(), jsonName);
                continue;
            }

            nodeData.jsonData = childData;
            nodeData.readJson();
        }
    }

    private void readDotJson(String[] names, JsonValue childData){
        NodeData currentData = this;
        JsonValue currentJsonValue = jsonData;

        for(int i = 0, len = names.length; i < len; i++){
            String name = names[i];
            NodeData nodeData = currentData.getChild(name);

            if(nodeData == null){
                Log.warn("'@' don't have child '@'", currentData.getObjInfo().getObj(), name);
                continue;
            }

            // TODO: StackOverflow
            JsonValue childJsonValue = i != len - 1 ? new JsonValue(ValueType.object) : childData;
            currentJsonValue.addChild(name, childJsonValue);
            nodeData.jsonData = childJsonValue;

            currentData = nodeData;
            currentJsonValue = childJsonValue;
        }
    }

    public void removeJson(String name){
        if(!hasJsonChild(name)){
            return;
        }

        jsonData.remove(name);

        // 删除子数据后 该jsonData就没有子数据了 清除掉
        if(jsonData.child == null && parentData != null){
            parentData.removeJson(nodeName);
            jsonData = null;
        }
    }

    public void clearJson(){
        for(Entry<String, NodeData> entry : children){
            NodeData childNodeData = entry.value;

            if(childNodeData.jsonData != null){
                childNodeData.clearJson();
            }
        }

        if(parentData != null){
            parentData.removeJson(nodeName);
            jsonData = null;
        }
    }

    public boolean hasJsonChild(String name){
        return jsonData != null && jsonData.has(name);
    }

    public boolean isRoot(){
        return this == rootData;
    }

    public ObjInfo<?> getObjInfo(){
        return NodeHelper.getObjectInfo(node);
    }

    @Override
    public String toString(){
        return nodeName + ": ";// + jsonData.toJson(OutputType.json);
    }
}
