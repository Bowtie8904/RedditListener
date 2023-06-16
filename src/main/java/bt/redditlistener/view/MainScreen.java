package bt.redditlistener.view;

import bt.gui.fx.core.annot.FxmlElement;
import bt.gui.fx.core.annot.css.FxStyleClass;
import bt.gui.fx.core.annot.handl.FxHandler;
import bt.gui.fx.core.annot.handl.chang.type.*;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnAction;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseClicked;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseEntered;
import bt.gui.fx.core.annot.handl.evnt.type.FxOnMouseExited;
import bt.gui.fx.core.annot.setup.FxSetup;
import bt.gui.fx.core.tray.DefaultFxSystemTrayScreen;
import bt.gui.fx.util.ButtonHandling;
import bt.redditlistener.reddit.ObservableManager;
import bt.redditlistener.reddit.RedditConnector;
import bt.redditlistener.reddit.observ.*;
import bt.redditlistener.view.comp.ObservableListCell;
import bt.redditlistener.view.css.ButtonCss;
import bt.redditlistener.view.css.TextFieldCss;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

/**
 * @author &#8904
 */
@FxStyleClass(ButtonCss.class)
@FxStyleClass(TextFieldCss.class)
@Component
public class MainScreen extends DefaultFxSystemTrayScreen
{
    public static final String TEXTFIELD_REGEX = ".{1,200}";

    @FxmlElement
    @FxHandler(type = FxOnMouseClicked.class, method = "onListClicked")
    private ListView<ObservableListCell> list;

    @FxmlElement
    @FxSetup(css = ButtonCss.GREEN_BACKGROUND)
    @FxHandler(type = FxOnAction.class, method = "onSaveButton", withParameters = false)
    @FxHandler(type = FxOnMouseEntered.class, methodClass = ButtonHandling.class, method = "onMouseEnter", withParameters = false, passField = true)
    @FxHandler(type = FxOnMouseExited.class, methodClass = ButtonHandling.class, method = "onMouseExit", withParameters = false, passField = true)
    private Button saveButton;

    @FxmlElement
    @FxHandler(type = FxLimitLength.class, value = "200", property = "textProperty")
    @FxHandler(type = FxNoLeadingSpaces.class, property = "textProperty")
    @FxHandler(type = FxNoTrailingSpaces.class, property = "textProperty")
    @FxHandler(type = FxOnTextMatchRemoveCss.class, method = TextFieldCss.ERROR, value = TEXTFIELD_REGEX, property = "textProperty")
    @FxHandler(type = FxOnTextNotMatchAddCss.class, method = TextFieldCss.ERROR, value = TEXTFIELD_REGEX, property = "textProperty")
    @FxHandler(type = FxOnTextMatch.class, method = "enableSaveButton", value = TEXTFIELD_REGEX, property = "textProperty", withParameters = false)
    @FxHandler(type = FxOnTextNotMatch.class, method = "disableSaveButton", value = TEXTFIELD_REGEX, property = "textProperty", withParameters = false)
    private TextField textField;

    @FxmlElement
    private ComboBox<String> kindDropDown;

    @FxmlElement
    private Label rateLimitLabel;

    private HostServices hostServices;

    @Autowired
    private ObservableManager observableManager;

    @Autowired
    private RedditConnector redditApplication;

    public MainScreen() throws IOException
    {
        this(ImageIO.read(MainScreen.class.getResourceAsStream("/icon.png")));
    }

    /**
     * @param trayImage
     */
    public MainScreen(Image trayImage)
    {
        super(trayImage);
        Platform.setImplicitExit(false);
        var settings = getSystemTraySettings();
        settings.addOption("Open", e -> onIconifiedChange(false));
        settings.addOption("Terminate", e ->
        {
            Platform.runLater(() ->
                              {
                                  kill();
                              });
        });
    }

    private void onListClicked(MouseEvent e)
    {
        if (e.getClickCount() >= 2)
        {
            var obs = this.list.getSelectionModel().getSelectedItem().getItem();
            this.hostServices.showDocument(obs.getLink());
        }
    }

    private void enableSaveButton()
    {
        this.saveButton.setDisable(false);
    }

    private void disableSaveButton()
    {
        this.saveButton.setDisable(true);
    }

    private void onSaveButton()
    {
        switch (this.kindDropDown.getSelectionModel().getSelectedItem())
        {
            case "/r/":
                this.observableManager.addObservable(new SubredditObservable(this.textField.getText()));
                break;
            case "/u/":
                this.observableManager.addObservable(new RedditUserObservable(this.textField.getText()));
                break;
            case "/mod/":
                this.observableManager.addObservable(new ModQueueObservable(this.textField.getText()));
                break;
            case "/thread/":
                this.observableManager.addObservable(RedditThreadObservable.newFor(this.textField.getText()));
                break;
        }

        this.textField.setText("");
    }

    /**
     * @return the subredditManager
     */
    public ObservableManager getObservableManager()
    {
        return this.observableManager;
    }

    /**
     * @param observableManager the subredditManager to set
     */
    public void setObservableManager(ObservableManager observableManager)
    {
        this.observableManager = observableManager;
        this.observableManager.observables().addListener(this::onObservableChange);
    }

    public void onObservableChange(Change<? extends RedditObservable> c)
    {
        while (c.next())
        {
            for (var obs : c.getAddedSubList())
            {
                this.list.getItems().add(createListCell(obs));
            }
        }
    }

    private ObservableListCell createListCell(RedditObservable obs)
    {
        return new ObservableListCell(obs, this.observableManager, this.list);
    }

    /**
     * @see bt.gui.fx.core.FxScreen#kill()
     */
    @Override
    public void kill()
    {
        super.kill();
        System.exit(0);
    }

    /**
     * @see bt.gui.fx.core.FxScreen#prepareScreen()
     */
    @Override
    protected void prepareScreen()
    {
        this.kindDropDown.setItems(FXCollections.observableArrayList("/r/", "/u/", "/mod/", "/thread/"));
        this.kindDropDown.getSelectionModel().select(0);

        setRateLimitLabel(0);

        this.redditApplication.remainingRateLimitProperty().addListener((obs, ol, ne) ->
                                                                        {
                                                                            Platform.runLater(() ->
                                                                                              {
                                                                                                  setRateLimitLabel(ne.intValue());
                                                                                              });
                                                                        });
    }

    private void setRateLimitLabel(int remaining)
    {
        this.rateLimitLabel.setText(remaining + " remaining requests");
    }

    /**
     * @see bt.gui.fx.core.FxScreen#prepareStage(Stage)
     */
    @Override
    protected void prepareStage(Stage stage)
    {
        stage.setTitle("Listener for Reddit");
        setIcons("/icon.png");

        stage.xProperty().addListener((obs, ol, ne) ->
                                      {
                                          this.x = ne.intValue();
                                      });

        stage.yProperty().addListener((obs, ol, ne) ->
                                      {
                                          this.y = ne.intValue();
                                      });
    }

    /**
     * @see bt.gui.fx.core.FxScreen#prepareScene(Scene)
     */
    @Override
    protected void prepareScene(Scene scene)
    {
    }

    /**
     * @see bt.gui.fx.core.FxScreen#onIconifiedChange(boolean)
     */
    @Override
    public void onIconifiedChange(boolean isIconified)
    {
        if (isIconified)
        {
            sendToSystemTray();
        }
        else
        {
            Platform.runLater(() ->
                              {
                                  openFromSystemTray();
                                  this.stage.setIconified(false);
                              });
        }
    }

    public HostServices getHostServices()
    {
        return this.hostServices;
    }

    public void setHostServices(HostServices hostServices)
    {
        this.hostServices = hostServices;
    }
}