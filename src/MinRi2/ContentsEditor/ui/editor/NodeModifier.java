package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;

/**
 * @author minri2
 * Create by 2024/2/16
 */
public class NodeModifier extends Table{
    private final NodeData nodeData;

    private final BaseModifier<?> modifier;

    public NodeModifier(NodeData nodeData){
        this.nodeData = nodeData;
        this.modifier = getModifier(nodeData);

        setup();

        background(MinTex.whiteuiRegion);
        setColor(EPalettes.editSky);
    }

    public static boolean modifiable(CTNode node){
        return BaseModifier.modifiable(node);
    }

    public static BaseModifier<?> getModifier(NodeData nodeData){
        return BaseModifier.getModifier(nodeData);
    }

    public void setup(){
        table(cont -> {
            // Add node info
            NodeDisplay.display(cont, nodeData);

            cont.table(modifier::build).grow();
        }).grow();

        image().width(4f).color(Color.darkGray).growY().right();
        row();
        Cell<?> horizontalLine = image().height(4f).color(Color.darkGray).growX();
        horizontalLine.colspan(getColumns());
    }

    public interface ModifyConsumer<T>{
        boolean checkTypeValid(T value);

        T getData();

        void setJsonData(T value);
    }

    public interface ModifierBuilder<T>{
        ModifierBuilder<String> textBuilder = (table, consumer) -> {
            String value = consumer.getData();
            table.field(value, consumer::setJsonData).valid(consumer::checkTypeValid).growX();
        };
        ModifierBuilder<Boolean> booleanBuilder = (table, consumer) -> {
            boolean value = consumer.getData();
            table.add("BOOLEANEDIT: " + value);
        };

        /**
         * 构建UI，提供修改结果
         */
        void build(Table table, ModifyConsumer<T> consumer);
    }

    public abstract static class BaseModifier<T> implements ModifyConsumer<T>{
        public static final Seq<ModifierConfig> modifyConfig = new Seq<>();

        static{
            modifyConfig.addAll(
            new ModifierConfig(StringModifier::new, String.class),
            new ModifierConfig(NumberModifier::new,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            byte.class, short.class, int.class, long.class, float.class, double.class)
            );
        }

        public final NodeData nodeData;
        protected String modifierName;
        protected ModifierBuilder<T> builder;
        protected ValueType valueType;

        protected BaseModifier(NodeData nodeData){
            this.nodeData = nodeData;
        }

        public static boolean modifiable(CTNode node){
            ObjInfo<?> objInfo = NodeHelper.getObjectInfo(node);

            if(objInfo == null){
                return false;
            }

            Class<?> type = objInfo.getType();

            for(ModifierConfig config : modifyConfig){
                if(config.canModify(type)){
                    return true;
                }
            }

            return false;
        }

        public static BaseModifier<?> getModifier(NodeData nodeData){
            ObjInfo<?> objInfo = nodeData.getObjInfo();

            if(objInfo == null){
                return null;
            }

            Class<?> type = objInfo.getType();

            for(ModifierConfig config : modifyConfig){
                if(config.canModify(type)){
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

        @Override
        public final void setJsonData(T value){
            setJsonData(getJsonData(), value);
        }

        @Override
        public final boolean checkTypeValid(T value){
            Class<?> type = nodeData.getObjInfo().getType();
            return checkTypeValid(value, type);
        }

        protected abstract void setJsonData(JsonValue jsonData, T value);

        protected boolean checkTypeValid(T value, Class<?> type){
            return true;
        }

        public static class ModifierConfig{
            public final Func<NodeData, BaseModifier<?>> modifierProv;
            public final Seq<Class<?>> modifierTypes = new Seq<>();

            public ModifierConfig(Func<NodeData, BaseModifier<?>> modifierProv, Class<?>... types){
                this.modifierProv = modifierProv;
                modifierTypes.addAll(types);
            }

            public boolean canModify(Class<?> type){
                return modifierTypes.contains(type);
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
        public String getData(){
            if(nodeData.hasData(modifierName)){
                return getJsonData().asString();
            }
            return String.valueOf(nodeData.getObjInfo().getObj());
        }

        @Override
        protected void setJsonData(JsonValue jsonData, String value){
            jsonData.set(value);
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
}
