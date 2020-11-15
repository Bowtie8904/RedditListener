package core.view.comp;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;

import bt.gui.fx.core.annot.FxAnnotationUtils;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseEntered;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseExited;
import bt.gui.fx.core.annot.setup.FxSetup;
import bt.gui.fx.core.comp.ButtonListCell;
import bt.gui.fx.util.ButtonHandling;
import core.config.css.ButtonCss;
import core.obj.ObservableManager;
import core.obj.obs.RedditObservable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;

/**
 * @author &#8904
 *
 */
public class ObservableListCell extends ButtonListCell<RedditObservable>
{
    @FxSetup(css = ButtonCss.RED_BACKGROUND)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private JFXButton delete;

    /**
     * @param item
     * @param buttons
     */
    public ObservableListCell(RedditObservable item, ObservableManager manager, JFXListView<ObservableListCell> list, Button... buttons)
    {
        super(item, buttons);

        this.delete = new JFXButton("Delete");
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