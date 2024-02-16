package MinRi2.ContentsEditor.node;

import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import arc.util.serialization.JsonWriter.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;
import mindustry.ctype.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeData implements Iterable<NodeData>{
    private static NodeData rootData;

    public final CTNode node;
    public final String nodeName;
    public JsonValue jsonData;

    protected ObjectMap<String, NodeData> children = new ObjectMap<>();

    // Only for removing. Null when node is rootNode.
    @Nullable
    protected NodeData parentData;
    protected ObjInfo<?> objInfo;

    private NodeData(String nodeName, CTNode node, JsonValue jsonData){
        this.nodeName = nodeName;
        this.node = node;
        this.jsonData = jsonData;
    }

    public static NodeData getRootData(){
        if(rootData == null){
            rootData = new NodeData("Root", NodeHelper.root, new JsonValue(ValueType.object));
        }
        return rootData;
    }

    public NodeData getOrCreate(String childName){
        node.collectAll();
        CTNode child = node.getChildren().get(childName);

        if(child == null){
            return null;
        }

        return children.get(childName, () -> {
            JsonValue childJsonData = new JsonValue(ValueType.object);
            jsonData.addChild(childName, childJsonData);

            NodeData nodeData = new NodeData(childName, child, childJsonData);
            nodeData.setParent(this);

            return nodeData;
        });
    }

    public void remove(){
        if(parentData != null){
            parentData.remove(nodeName);
        }
    }

    public void remove(String childName){
        jsonData.remove(childName);
        children.remove(childName);
    }

    public void clearChildren(){
        Iterator<String> iterator = children.keys();
        while(iterator.hasNext()){
            String childName = iterator.next();
            jsonData.remove(childName);
            iterator.remove();
        }
    }

    public boolean contains(String childName){
        return children.containsKey(childName);
    }

    public void setParent(NodeData parentData){
        this.parentData = parentData;
    }

    public ObjInfo<?> getObjInfo(){
        if(objInfo == null){
            objInfo = NodeHelper.getObjectInfo(node);
        }
        return objInfo;
    }

    public String getDisplayName(){
        ObjInfo<?> objInfo = getObjInfo();

        if(objInfo != null){
            Object obj = objInfo.getObj();

            if(obj instanceof UnlockableContent content){
                return content.localizedName;
            }

            if(obj instanceof ContentType contentType){
                return contentType.name();
            }
        }

        return nodeName;
    }

    public boolean isRoot(){
        return this == rootData;
    }

    @NotNull
    @Override
    public Iterator<NodeData> iterator(){
        return children.values();
    }

    @Override
    public String toString(){
        return nodeName + ": " + jsonData.toJson(OutputType.json);
    }
}
