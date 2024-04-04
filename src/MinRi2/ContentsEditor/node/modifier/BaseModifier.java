package MinRi2.ContentsEditor.node.modifier;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.node.modifier.equal.*;
import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;

import java.util.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public abstract class BaseModifier<T> implements ModifyConsumer<T>{
    public static final Seq<ModifierConfig> modifyConfig = new Seq<>();

    static{
        EqualModifier.init();
    }

    public final NodeData nodeData;
    protected ModifierBuilder<T> builder;
    protected ValueType valueType;

    private Boolc onModified;

    protected BaseModifier(NodeData nodeData){
        this.nodeData = nodeData;
    }

    public static boolean modifiable(CTNode node){
        ObjInfo<?> objInfo = NodeHelper.getObjectInfo(node);

        if(objInfo == null){
            return false;
        }

        for(ModifierConfig config : modifyConfig){
            if(config.canModify(node)){
                return true;
            }
        }

        return false;
    }

    public static BaseModifier<?> getModifier(NodeData nodeData){
        CTNode node = nodeData.node;
        for(ModifierConfig config : modifyConfig){
            if(config.canModify(node)){
                return config.modifierProv.get(nodeData);
            }
        }

        return null;
    }

    public void build(Table table){
        builder.build(table, this);
    }

    public boolean modified(){
        return isModified(getData());
    }

    public void onModified(Boolc onModified){
        this.onModified = onModified;
    }

    @Override
    public T getData(){
        JsonValue jsonValue = nodeData.jsonData;
        if(jsonValue == null || (!jsonValue.isValue() && nodeData.jsonData.size == 0)){
            return getDefaultValue();
        }

        return getDataJson(getJsonValue());
    }

    public abstract JsonValue getJsonValue();

    public T getDefaultValue(){
        Object object = nodeData.getObjInfo().getObj();
        return parse(object);
    }

    @Override
    public Class<?> getDataType(){
        return nodeData.getObjInfo().getType();
    }

    @Override
    public final void modify(T value){
        boolean modified = isModified(value);
        if(modified){
            setDataJson(getJsonValue(), value);

            if(onModified != null){
                onModified.get(modified);
            }
        }else{
            reset();
        }
    }

    @Override
    public final void reset(){
        resetModify();

        if(onModified != null){
            onModified.get(false);
        }
    }

    protected abstract void resetModify();

    @Override
    public final boolean checkValue(T value){
        Class<?> type = nodeData.getObjInfo().getType();
        return checkTypeValid(value, type);
    }

    /**
     * @param value 修改后的值
     * @return 数据修改后是否与默认值相同
     */
    protected boolean isModified(T value){
        return !Objects.equals(getDefaultValue(), value);
    }

    /**
     * 将数据保存
     * 由子类实现
     * @param jsonData 保存到的JsonValue
     */
    protected abstract void setDataJson(JsonValue jsonData, T value);

    /**
     * 从JsonValue中读取数据
     * 由子类实现
     * @param jsonData 读取的JsonValue
     */
    protected abstract T getDataJson(JsonValue jsonData);

    /**
     * 给定类型 判断数据是否符合类型
     */
    protected boolean checkTypeValid(T value, Class<?> type){
        return true;
    }

    public abstract T parse(Object object);

    public static class ModifierConfig{
        public final String modifierName;
        public final Func<NodeData, BaseModifier<?>> modifierProv;
        public final Seq<Class<?>> modifierTypes = new Seq<>();

        public ModifierConfig(String modifierName, Func<NodeData, BaseModifier<?>> modifierProv, Class<?>... types){
            this.modifierName = modifierName;
            this.modifierProv = modifierProv;
            modifierTypes.addAll(types);
        }

        public boolean canModify(CTNode node){
            ObjInfo<?> objInfo = NodeHelper.getObjectInfo(node);

            if(objInfo == null){
                return false;
            }

            node.collectAll();
            return node.getChildren().containsKey(modifierName)
            && modifierTypes.contains(objInfo.getType());
        }
    }
}
