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
import core.obj.RedditThread;
import core.view.NewThreadListScreen;
import javafx.scene.control.Button;

/**
 * @author &#8904
 *
 */
public class RedditThreadListCell extends ButtonListCell<RedditThread>
{
    @FxSetup(css = ButtonCss.BLUE_BACKGROUND)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private JFXButton dismiss;

    /**
     * @param item
     * @param buttons
     */
    public RedditThreadListCell(RedditThread item, NewThreadListScreen screen, JFXListView<RedditThreadListCell> list, Button... buttons)
    {
        super(item, buttons);

        this.dismiss = new JFXButton("Dismiss");
        this.dismiss.setOnAction(e ->
        {
            list.getItems().remove(this);
        });

        FxAnnotationUtils.populateFxHandlers(this);
        FxAnnotationUtils.setupFields(this);

        addButtons(this.dismiss);
    }
}