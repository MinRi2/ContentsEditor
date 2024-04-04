package MinRi2.ContentsEditor.node.modifier;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ContentsEditor.ui.editor.*;
import MinRi2.ModCore.ui.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * @author minri2
 * Create by 2024/2/16
 */
public class NodeModifier{
    private NodeModifier(){
    }

    public static boolean modifiable(CTNode node){
        return BaseModifier.modifiable(node);
    }

    public static BaseModifier<?> getModifier(NodeData nodeData){
        return BaseModifier.getModifier(nodeData);
    }

    public static void setupModifierTable(Table table, NodeData nodeData){
        BaseModifier<?> modifier = getModifier(nodeData);

        table.table(infoTable -> {
            // Add node info
            NodeDisplay.displayNameType(infoTable, nodeData);
        }).fill();

        table.table(modifier::build).pad(4).grow();

        table.image().width(4f).color(Color.darkGray).growY().right();
        table.row();
        Cell<?> horizontalLine = table.image().height(4f).color(Color.darkGray).growX();
        horizontalLine.colspan(table.getColumns());

        table.background(MinTex.whiteuiRegion);
        table.setColor(modifier.modified() ? EPalettes.modified : EPalettes.unmodified);

        modifier.onModified(modified -> {
            Color color = modified ? EPalettes.modified : EPalettes.unmodified;
            table.addAction(Actions.color(color, 0.5f));
        });
    }

    public abstract static class BaseModifier<T> implements ModifyConsumer<T>{
        public static final Seq<ModifierConfig> modifyConfig = new Seq<>();

        static{
            modifyConfig.addAll(
            new ModifierConfig("=", StringModifier::new, String.class),

            new ModifierConfig("=", NumberModifier::new,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            byte.class, short.class, int.class, long.class, float.class, double.class),

            new ModifierConfig("=", BooleanModifier::new, Boolean.class, boolean.class),

            new ModifierConfig("=", ContentTypeModifier::new,
            Block.class, Item.class, Liquid.class, StatusEffect.class, UnitType.class)
            );
        }

        public final NodeData nodeData;
        protected String modifierName;
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

        public JsonValue getJsonData(){
            return nodeData.getData(modifierName, valueType);
        }

        public T getDefaultData(){
            Object object = nodeData.getObjInfo().getObj();
            return parse(object);
        }

        public boolean modified(){
            return nodeData.hasData(modifierName);
        }

        public void onModified(Boolc onModified){
            this.onModified = onModified;
        }

        @Override
        public Class<?> getDataType(){
            return nodeData.getObjInfo().getType();
        }

        /**
         * 获取节点数据的jsonData
         * 会创建数据链
         */
        @Override
        public final T getData(){
            if(nodeData.hasData(modifierName)){
                return getDataJson(getJsonData());
            }

            return getDefaultData();
        }

        @Override
        public final void saveData(T value){
            setDataJson(getJsonData(), value);

            if(onModified != null){
                onModified.get(true);
            }
        }

        @Override
        public final void removeData(){
            nodeData.removeData(modifierName);

            if(onModified != null){
                onModified.get(false);
            }
        }

        @Override
        public final boolean checkValue(T value){
            Class<?> type = nodeData.getObjInfo().getType();
            return checkTypeValid(value, type);
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

    public static class StringModifier extends BaseModifier<String>{
        protected StringModifier(NodeData nodeData){
            super(nodeData);

            modifierName = "=";
            builder = ModifierBuilder.textBuilder;
            valueType = ValueType.stringValue;
        }

        @Override
        protected void setDataJson(JsonValue jsonData, String value){
            jsonData.set(value);
        }

        @Override
        protected String getDataJson(JsonValue jsonData){
            return jsonData.asString();
        }

        @Override
        public String parse(Object object){
            return String.valueOf(object);
        }
    }

    public static class NumberModifier extends StringModifier{
        public NumberModifier(NodeData nodeData){
            super(nodeData);
        }

        @Override
        public boolean checkTypeValid(String string, Class<?> type){
            try{
                if(type == byte.class || type == Byte.class){
                    Byte.parseByte(string);
                }else if(type == short.class || type == Short.class){
                    Integer.parseInt(string);
                }else if(type == long.class || type == Long.class){
                    Long.parseLong(string);
                }else if(type == float.class || type == Float.class){
                    Float.parseFloat(string);
                }else if(type == double.class || type == Double.class){
                    Double.parseDouble(string);
                }
                return true;
            }catch(Exception ignored){
                return false;
            }
        }
    }

    public static class BooleanModifier extends BaseModifier<Boolean>{
        protected BooleanModifier(NodeData nodeData){
            super(nodeData);

            modifierName = "=";
            builder = ModifierBuilder.booleanBuilder;
            valueType = ValueType.booleanValue;
        }

        @Override
        protected void setDataJson(JsonValue jsonData, Boolean value){
            jsonData.set(value);
        }

        @Override
        protected Boolean getDataJson(JsonValue jsonData){
            return jsonData.asBoolean();
        }

        @Override
        public Boolean parse(Object object){
            return (Boolean)object;
        }
    }

    public static class ContentTypeModifier extends BaseModifier<UnlockableContent>{
        public static ObjectMap<Class<?>, ContentType> contentClassTypeMap = ObjectMap.of(
        Block.class, ContentType.block,
        Item.class, ContentType.item,
        Liquid.class, ContentType.liquid,
        StatusEffect.class, ContentType.status,
        UnitType.class, ContentType.unit
        );

        private final ContentType contentType;

        protected ContentTypeModifier(NodeData nodeData){
            super(nodeData);

            modifierName = "=";
            builder = ModifierBuilder.contentBuilder;
            valueType = ValueType.stringValue;

            Class<?> type = nodeData.getObjInfo().getType();
            contentType = contentClassTypeMap.get(type);
        }

        @Override
        protected void setDataJson(JsonValue jsonData, UnlockableContent value){
            jsonData.set(value.name);
        }

        @Override
        protected UnlockableContent getDataJson(JsonValue jsonData){
            return Vars.content.getByName(contentType, jsonData.asString());
        }

        @Override
        public UnlockableContent parse(Object object){
            return (UnlockableContent)object;
        }

    }
}
