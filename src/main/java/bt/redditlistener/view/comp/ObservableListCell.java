package bt.redditlistener.view.comp;

import bt.gui.fx.core.annot.FxAnnotationUtils;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseEntered;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseExited;
import bt.gui.fx.core.annot.setup.FxSetup;
import bt.gui.fx.core.comp.ButtonListCell;
import bt.gui.fx.util.ButtonHandling;
import bt.redditlistener.reddit.ObservableManager;
import bt.redditlistener.reddit.observ.RedditObservable;
import bt.redditlistener.view.css.ButtonCss;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

/**
 * @author &#8904
 */
public class ObservableListCell extends ButtonListCell<RedditObservable>
{
    @FxSetup(css = ButtonCss.RED_BACKGROUND)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private Button delete;

    /**
     * @param item
     * @param buttons
     */
    public ObservableListCell(RedditObservable item, ObservableManager manager, ListView<ObservableListCell> list, Button... buttons)
    {
        super(item, buttons);

        this.delete = new Button("Delete");
        this.delete.setOnAction(e ->
                                {
                                    manager.observables().remove(item);
                                    list.getItems().remove(this);
                                    item.onDelete();
                                });

        this.delete.setAlignment(Pos.CENTER_RIGHT);

        FxAnnotationUtils.populateFxHandlers(this);
        FxAnnotationUtils.setupFields(this);

        addButtons(this.delete);
    }
}