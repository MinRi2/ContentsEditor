package MinRi2.ContentsEditor.ui;

import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/2/17
 */
public class ContentSelector extends BaseDialog{
    private ContentType contentType;
    private Boolf<UnlockableContent> selectable, consumer;

    private Table pane;

    public ContentSelector(){
        super("@content-selector");

        pane = new Table();

        setup();

        resized(this::rebuild);
        shown(this::rebuild);
    }

    private void setup(){
        cont.pane(pane).scrollX(false).grow();
        addCloseButton();
    }

    private void rebuild(){
        pane.clearChildren();

        Seq<UnlockableContent> seq = Vars.content.getBy(contentType);

        int index = 0;
        for(UnlockableContent content : seq){

            if(!selectable.get(content)) continue;

            pane.button(table -> {
                setupContentTable(table, content);
            }, Styles.defaultb, () -> {
                if(consumer.get(content)){
                    hide();
                }
            }).growX();

            if(++index % 3 == 0){
                pane.row();
            }
        }
    }

    private void setupContentTable(Table table, UnlockableContent content){
        table.image(content.uiIcon).size(64f).pad(8f).expandX().left();

        table.table(infoTable -> {
            infoTable.defaults().pad(4f).expandX().right();

            infoTable.add(content.localizedName);
            infoTable.row();
            infoTable.add(content.name).color(Pal.gray);
        }).expandX();
    }

    public void select(ContentType contentType, Boolf<UnlockableContent> selectable, Boolf<UnlockableContent> consumer){
        this.contentType = contentType;
        this.selectable = selectable;
        this.consumer = consumer;

        show();
    }
}
