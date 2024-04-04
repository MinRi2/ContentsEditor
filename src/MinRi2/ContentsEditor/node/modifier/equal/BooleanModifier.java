package MinRi2.ContentsEditor.node.modifier.equal;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.node.modifier.*;
import arc.util.serialization.*;
import arc.util.serialization.JsonValue.*;

/**
 * @author minri2
 * Create by 2024/4/4
 */
public class BooleanModifier extends EqualModifier<Boolean>{
    protected BooleanModifier(NodeData nodeData){
        super(nodeData);

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
