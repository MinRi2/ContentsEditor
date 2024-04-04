package MinRi2.ContentsEditor.node.modifier;

import MinRi2.ContentsEditor.node.modifier.NodeModifier.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import MinRi2.ModCore.ui.element.*;
import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.ui.*;

public interface ModifierBuilder<T>{
    ModifierBuilder<String> textBuilder = (table, consumer) -> {
        // lambda????
        final String[] value = {consumer.getData()};

        TextField field = table.field(value[0], consumer::saveData)
        .valid(consumer::checkValue).pad(4f).width(100f).get();

        addResetButton(table, consumer, () -> {
            value[0] = consumer.getData();
            field.setText(value[0]);
        });
    };

    ModifierBuilder<Boolean> booleanBuilder = (table, consumer) -> {
        // lambda????
        final boolean[] value = {consumer.getData()};

        BorderColorImage image = new BorderColorImage();
        image.colorAction(value[0] ? Color.green : Color.red);

        Cons<Boolean> setColor = bool -> {
            value[0] = bool;
            image.colorAction(bool ? Color.green : Color.red);
        };

        table.button(b -> {
            b.add(image).size(32f).pad(8f).expandX().left();
            b.label(() -> value[0] ? "[green]true" : "[red]false").expandX();
        }, Styles.clearNonei, () -> {
            setColor.get(!value[0]);
            consumer.saveData(value[0]);
        }).grow();

        addResetButton(table, consumer, () -> setColor.get(consumer.getData()));
    };

    ModifierBuilder<UnlockableContent> contentBuilder = (table, consumer) -> {
        // lambda????
        final UnlockableContent[] value = {null};
        final @SuppressWarnings("unchecked") Cons<UnlockableContent>[] setValue = new Cons[1];

        table.button(b -> {
            setValue[0] = c -> {
                value[0] = c;

                b.clearChildren();

                TextureRegion icon;
                String displayName;
                if(value[0] == null){
                    icon = Core.atlas.find("error");
                    displayName = "null";
                }else{
                    icon = value[0].uiIcon;
                    displayName = value[0].localizedName;
                }

                b.image(icon).scaling(Scaling.fit).size(40f).pad(8f).expandX().left();
                b.add(displayName).pad(4f).ellipsis(true).width(64f);
            };

            setValue[0].get(consumer.getData());
        }, Styles.clearNonei, () -> {
            Class<?> dataType = consumer.getDataType();
            ContentType contentType = ContentTypeModifier.contentClassTypeMap.get(dataType);

            EUI.selector.select(contentType, c -> c != value[0], c -> {
                setValue[0].get(c);
                consumer.saveData(value[0]);
                return true;
            });
        }).grow();

        addResetButton(table, consumer, () -> setValue[0].get(consumer.getData()));
    };

    static void addResetButton(Table table, ModifyConsumer<?> consumer, Runnable clicked){
        table.button(Icon.undo, Styles.clearNonei, () -> {
            consumer.removeData();
            clicked.run();
        }).width(32f).pad(4f).growY().expandX().right().with(b -> {
            ElementUtils.addTooltip(b, "@node-modifier.undo", true);
        });
    }

    /**
     * 构建UI，提供修改结果
     */
    void build(Table table, ModifyConsumer<T> consumer);
}