package MinRi2.ContentsEditor.node.modifier.equal;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.node.modifier.*;
import arc.struct.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public class ContentTypeModifier extends EqualModifier<UnlockableContent>{
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
