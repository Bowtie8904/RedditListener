package bt.redditlistener.view;

import bt.gui.fx.core.FxScreen;
import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.css.FxStyleClass;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnAction;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseClicked;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseEntered;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseExited;
import bt.gui.fx.core.annot.setup.FxSetup;
import bt.gui.fx.util.ButtonHandling;
import bt.redditlistener.reddit.notif.RedditNotification;
import bt.redditlistener.view.comp.RedditNotificationListCell;
import bt.redditlistener.view.css.ButtonCss;
import bt.redditlistener.view.css.TextFieldCss;
import bt.scheduler.Threads;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author &#8904
 */
@FxStyleClass(ButtonCss.class)
@FxStyleClass(TextFieldCss.class)
@Component
@Slf4j
public class NewNotificationListScreen extends FxScreen
{
    @FxmlElement
    @FxHandler(type = FxOnMouseClicked.class, method = "onThreadClicked")
    private ListView<RedditNotificationListCell> list;

    @FxmlElement
    @FxSetup(css = ButtonCss.BLUE_BACKGROUND)
    @FxHandler(type = FxOnAction.class, method = "onClearButton", withParameters = false)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private Button clearButton;

    private HostServices hostServices;
    private boolean active;

    private long lastIconUpdate = System.currentTimeMillis() - 5000;
    private volatile boolean iconUpdateScheduled;

    public NewNotificationListScreen()
    {
        this.active = true;
    }

    public synchronized void addThread(RedditNotification thread)
    {
        this.list.getItems().add(0, createListCell(thread));

        if (this.list.getItems().size() > 200)
        {
            this.list.getItems().remove(this.list.getItems().size() - 1);
        }

        this.list.refresh();
    }

    private synchronized void onThreadsChange(Change<? extends RedditNotificationListCell> c)
    {
        if (System.currentTimeMillis() - this.lastIconUpdate > 500 && !this.iconUpdateScheduled)
        {
            setIcons(createTaskbarIcon());
            this.lastIconUpdate = System.currentTimeMillis();
        }
        else if (!this.iconUpdateScheduled)
        {
            this.iconUpdateScheduled = true;

            Threads.get().schedule(() ->
                                   {
                                       this.iconUpdateScheduled = false;

                                       Platform.runLater(() -> setIcons(createTaskbarIcon()));
                                   }, 2, TimeUnit.SECONDS);
        }
    }

    private synchronized void onClearButton()
    {
        this.list.getItems().clear();
    }

    private RedditNotificationListCell createListCell(RedditNotification thread)
    {
        return new RedditNotificationListCell(thread, this, this.list);
    }

    private void onThreadClicked(MouseEvent e)
    {
        if (e.getClickCount() >= 2)
        {
            var selected = this.list.getSelectionModel().getSelectedItem();

            if (selected != null)
            {
                var thread = selected.getItem();
                this.hostServices.showDocument(thread.getLink());
                this.list.getItems().remove(this.list.getSelectionModel().getSelectedItem());
            }
        }
    }

    /**
     * @return the active
     */
    public boolean isActive()
    {
        return this.active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public void kill()
    {
        super.kill();
        this.active = false;
    }

    /**
     * @see FxScreen#prepareScreen()
     */
    @Override
    protected void prepareScreen()
    {
        this.list.setItems(FXCollections.observableArrayList());
        this.list.getItems().addListener(this::onThreadsChange);
    }

    /**
     * @see FxScreen#prepareStage(Stage)
     */
    @Override
    protected void prepareStage(Stage stage)
    {
        stage.setTitle("New threads on Reddit");
        setIcons("/icon.png");
    }

    private Image createTaskbarIcon()
    {
        Image image = null;

        try
        {
            BufferedImage bufImage = ImageIO.read(getClass().getResourceAsStream("/icon.png"));

            int number = this.list.getItems().size();

            if (number > 0)
            {
                Graphics graphics = bufImage.getGraphics();
                graphics.setColor(Color.RED);
                graphics.fillRoundRect(0, 0, 2000, 1200, 500, 500);

                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font("Arial Black", Font.BOLD, 950));

                int stringX = 0;
                int stringY = 950;

                if (number < 10)
                {
                    stringX = 680;
                }
                else if (number < 100)
                {
                    stringX = 330;
                }
                else
                {
                    stringX = -17;
                }

                graphics.drawString(number + "", stringX, stringY);
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufImage, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            image = new Image(is);
        }
        catch (IOException e)
        {
            log.error("Failed to create taskbar icon", e);
        }

        return image;
    }

    public HostServices getHostServices()
    {
        return this.hostServices;
    }

    public void setHostServices(HostServices hostServices)
    {
        this.hostServices = hostServices;
    }

    /**
     * @see FxScreen#prepareScene(Scene)
     */
    @Override
    protected void prepareScene(Scene scene)
    {
    }
}