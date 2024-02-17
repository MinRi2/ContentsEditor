package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.serialization.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;

/**
 * @author minri2
 * Create by 2024/2/16
 */
public class NodeModifier extends Table{
    public static final Seq<ModifierConfig> modifierBuilders = new Seq<>();

    public static ModifierBuilder textBuilder = (table, parameters) -> {
        Boolf<String> typeChecker = parameters.typeChecker;

        String value = "" + parameters.defaultValue;
        JsonValue jsonData = parameters.jsonDataProv.get();
        if(jsonData != null){
            value = jsonData.asString();
        }

        table.field(value, parameters.consumer).valid(typeChecker::get).growX();
    }, booleanBuilder = (table, parameters) -> {

        boolean value = (boolean)parameters.defaultValue;
        JsonValue jsonData = parameters.jsonDataProv.get();
        if(jsonData != null){
            value = jsonData.asBoolean();
        }
        table.add("BOOLEANEDIT: " + value);
    };

    static{
        modifierBuilders.addAll(
        new ModifierConfig(ModifierType.set, textBuilder, String.class),
        new NumberModifierConfig(),
        new ModifierConfig(ModifierType.set, booleanBuilder, Boolean.class, boolean.class)
        );
    }

    private final NodeData nodeData;

    public NodeModifier(NodeData nodeData){
        this.nodeData = nodeData;

        setup();

        background(MinTex.whiteuiRegion);
        setColor(EPalettes.editSky);
    }

    public static boolean modifiable(CTNode node){
        return NodeModifier.getBuilder(node) != null;
    }

    public static ModifierConfig getBuilder(CTNode node){
        ObjInfo<?> objInfo = NodeHelper.getObjectInfo(node);

        if(objInfo == null){
            return null;
        }

        Class<?> type = objInfo.getType();

        if(type.isAnonymousClass()){
            type = type.getSuperclass();
        }

        Class<?> finalType = type;
        return modifierBuilders.find(b -> b.canModify(finalType));
    }

    public void setup(){
        table(cont -> {
            // Add node info
            NodeDisplay.display(cont, nodeData);

            cont.table(this::setupModifierTable).grow();
        }).grow();

        image().width(4f).color(Color.darkGray).growY().right();
        row();
        Cell<?> horizontalLine = image().height(4f).color(Color.darkGray).growX();
        horizontalLine.colspan(getColumns());
    }

    private void setupModifierTable(Table table){
        ModifierConfig builder = getBuilder(nodeData.node);

        // impossible
        if(builder == null){
            return;
        }

        builder.build(table, nodeData);
    }

    public enum ModifierType{
        set("="),
        add("+"),
        append("+=");

        public final String modifierName;

        ModifierType(String modifierName){
            this.modifierName = modifierName;
        }
    }

    public interface ModifierBuilder{
        /**
         * 构建UI，提供修改结果
         */
        void build(Table table, BuilderParameters parameters);
    }

    public static class BuilderParameters{
        public static final BuilderParameters parameters = new BuilderParameters();

        public Object defaultValue;
        public Prov<JsonValue> jsonDataProv;
        public Boolf<String> typeChecker;
        public Cons<String> consumer;

        private BuilderParameters(){

        }

        public BuilderParameters set(ModifierConfig config, NodeData nodeData){
            Class<?> type = nodeData.getObjInfo().getType();
            String modifierName = config.getModifierName();

            defaultValue = nodeData.getObjInfo().getObj();
            jsonDataProv = () -> nodeData.jsonData == null ? null : nodeData.jsonData.get(modifierName);
            typeChecker = string -> config.checkTypeValid(string, type);
            consumer = string -> nodeData.setStringData(config.getModifierName(), string);

            return this;
        }
    }

    public static class ModifierConfig{
        private final Seq<Class<?>> modifyTypes = new Seq<>();
        private final ModifierBuilder builder;
        private final ModifierType modifierType;

        public ModifierConfig(ModifierType modifierType, ModifierBuilder builder, Class<?>... modifyTypes){
            this.modifierType = modifierType;
            this.builder = builder;
            this.modifyTypes.addAll(modifyTypes);
        }

        public String getModifierName(){
            return modifierType.modifierName;
        }

        public boolean canModify(Class<?> clazz){
            return modifyTypes.contains(clazz);
        }

        public final void build(Table table, NodeData nodeData){
            BuilderParameters parameters = BuilderParameters.parameters.set(this, nodeData);
            builder.build(table, parameters);
        }

        public boolean checkTypeValid(String string, Class<?> type){
            return true;
        }
    }

    public static class NumberModifierConfig extends ModifierConfig{

        public NumberModifierConfig(){
            super(ModifierType.set, textBuilder,
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            byte.class, short.class, int.class, long.class, float.class, double.class);
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
