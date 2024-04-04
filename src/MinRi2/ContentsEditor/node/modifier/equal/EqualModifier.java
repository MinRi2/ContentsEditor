package MinRi2.ContentsEditor.node.modifier.equal;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.node.modifier.*;
import arc.util.serialization.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public abstract class EqualModifier<T> extends BaseModifier<T>{
    protected EqualModifier(NodeData nodeData){
        super(nodeData);
    }

    public static void init(){
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

    @Override
    protected void resetModify(){
        nodeData.remove();
    }

    @Override
    public JsonValue getJsonValue(){
        return nodeData.getData("=", valueType);
    }
}
