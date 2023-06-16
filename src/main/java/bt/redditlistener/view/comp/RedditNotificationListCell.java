package bt.redditlistener.view.comp;

import bt.gui.fx.core.annot.FxAnnotationUtils;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseEntered;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseExited;
import bt.gui.fx.core.annot.setup.FxSetup;
import bt.gui.fx.core.comp.ButtonListCell;
import bt.gui.fx.util.ButtonHandling;
import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.view.NewNotificationListScreen;
import bt.redditlistener.view.css.ButtonCss;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

/**
 * @author &#8904
 */
public class RedditNotificationListCell extends ButtonListCell<RedditNotification>
{
    @FxSetup(css = ButtonCss.BLUE_BACKGROUND)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private Button dismiss;

    /**
     * @param item
     * @param buttons
     */
    public RedditNotificationListCell(RedditNotification item, NewNotificationListScreen screen, ListView<RedditNotificationListCell> list, Button... buttons)
    {
        super(item, buttons);

        this.dismiss = new Button("Dismiss");
        this.dismiss.setOnAction(e ->
                                 {
                                     list.getItems().remove(this);
                                 });

        FxAnnotationUtils.populateFxHandlers(this);
        FxAnnotationUtils.setupFields(this);

        addButtons(this.dismiss);
    }
}