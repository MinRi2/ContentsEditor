package MinRi2.ContentsEditor.node.modifier.equal;

import MinRi2.ContentsEditor.node.modifier.*;
import arc.func.*;
import arc.util.serialization.*;
import cf.wayzer.contentsTweaker.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public abstract class EqualModifier<T> extends BaseModifier<T>{
    protected EqualModifier(){
    }

    public static void init(){
        modifyConfig.addAll(
        new EqualModifierConfig(StringModifier::new, String.class),

        new EqualModifierConfig(NumberModifier::new,
        Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
        byte.class, short.class, int.class, long.class, float.class, double.class),

        new EqualModifierConfig(BooleanModifier::new, Boolean.class, boolean.class),

        new EqualModifierConfig(ContentTypeModifier::new,
        Block.class, Item.class, Liquid.class, StatusEffect.class, UnitType.class)
        );
    }

    @Override
    public JsonValue getJsonValue(){
        return nodeData.getJson("=", valueType);
    }

    public static class EqualModifierConfig extends ModifierConfig{

        public EqualModifierConfig(Prov<BaseModifier<?>> modifierProv, Class<?>... types){
            super(modifierProv, types);
        }

        @Override
        protected boolean canModified(CTNode node){
            return node.getChildren().containsKey("=");
        }
    }
}
