package MinRi2.ContentsEditor.node.modifier;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.node.modifier.equal.*;
import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;

import java.util.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public abstract class BaseModifier<T> implements ModifyConsumer<T>, Poolable{
    public static final Seq<ModifierConfig> modifyConfig = new Seq<>();

    static{
        EqualModifier.init();
    }

    protected ModifierBuilder<T> builder;
    protected ValueType valueType;
    protected NodeData nodeData;
    private Boolc onModified;

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
                return config.getModifier(nodeData);
            }
        }

        return null;
    }

    public void build(Table table){
        builder.build(table, this);
    }

    public void onModified(Boolc onModified){
        this.onModified = onModified;
    }

    public abstract JsonValue getJsonValue();

    public T getDefaultValue(){
        Object object = nodeData.getObjInfo().getObj();
        return parse(object);
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

    @Override
    public void reset(){
        this.nodeData = null;
    }

    public void setNodeData(NodeData data){
        this.nodeData = data;
    }

    @Override
    public boolean isModified(){
        return isModified(getData());
    }

    @Override
    public Class<?> getDataType(){
        return nodeData.getObjInfo().getType();
    }

    @Override
    public T getData(){
        JsonValue jsonValue = nodeData.jsonData;
        if(jsonValue == null || (!jsonValue.isValue() && nodeData.jsonData.size == 0)){
            return getDefaultValue();
        }

        return getDataJson(getJsonValue());
    }

    @Override
    public final void onModify(T value){
        boolean modified = isModified(value);
        if(modified){
            setDataJson(getJsonValue(), value);

            if(onModified != null){
                onModified.get(true);
            }
        }else{
            this.resetModify();
        }
    }

    @Override
    public void resetModify(){
        nodeData.clearJson();

        if(onModified != null){
            onModified.get(false);
        }
    }

    @Override
    public final boolean checkValue(T value){
        Class<?> type = nodeData.getObjInfo().getType();
        return checkTypeValid(value, type);
    }

    public static class ModifierConfig{
        public final Seq<Class<?>> modifierTypes = new Seq<>();
        private final BaseModifier<?> example;
        private final Pool<BaseModifier<?>> pool;

        public ModifierConfig(Prov<BaseModifier<?>> modifierProv, Class<?>... types){
            example = modifierProv.get();

            Class<BaseModifier<?>> modifierClass = (Class<BaseModifier<?>>)example.getClass();
            pool = Pools.get(modifierClass, modifierProv);

            modifierTypes.addAll(types);
        }

        public boolean canModify(CTNode node){
            ObjInfo<?> objInfo = NodeHelper.getObjectInfo(node);

            if(objInfo == null){
                return false;
            }

            node.collectAll();
            return canModified(node) && modifierTypes.contains(objInfo.getType());
        }

        protected boolean canModified(CTNode node){
            return true;
        }

        public BaseModifier<?> getModifier(NodeData nodeData){
            BaseModifier<?> modifier = pool.obtain();
            modifier.setNodeData(nodeData);
            return modifier;
        }
    }
}
