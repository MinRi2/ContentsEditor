package MinRi2.ContentsEditor.node;

import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;
import org.jetbrains.annotations.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeData{
    private static NodeData rootData;

    public final CTNode node;
    public final String nodeName;
    /** Init when {@link #setStringData(String, String)} */
    public JsonValue jsonData;

    protected ObjectMap<String, NodeData> children = new ObjectMap<>();

    /** Null when {@link #node} is Root */
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

    public void initJsonData(){
        if(jsonData != null || parentData == null){
            return;
        }

        parentData.initJsonData();

        JsonValue jsonData = new JsonValue(ValueType.object);
        parentData.jsonData.addChild(nodeName, jsonData);

        this.jsonData = jsonData;
    }

    public NodeData getChild(String childName){
        node.collectAll();
        CTNode child = node.getChildren().get(childName);

        if(child == null){
            return null;
        }

        return children.get(childName, () -> {
            NodeData nodeData = new NodeData(childName, child);
            nodeData.setParent(this);

            return nodeData;
        });
    }

    public void setParent(NodeData parentData){
        this.parentData = parentData;
    }

    public JsonValue getData(String name, ValueType valueType){
        initJsonData();

        JsonValue data = jsonData.get(name);

        if(data == null){
            data = new JsonValue(valueType);
            jsonData.addChild(name, data);
        }

        return data;
    }

    public void removeData(String name){
        if(hasData(name)){
            return;
        }
        
        jsonData.remove(name);

        // This jsonData is empty after removing. Remove jsonData from parent.
        if(jsonData.size == 0 && parentData != null){
            parentData.removeData(nodeName);
        }
    }

    public boolean hasData(String name){
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
