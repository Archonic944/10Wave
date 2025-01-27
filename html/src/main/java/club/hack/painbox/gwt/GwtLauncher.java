package club.hack.painbox.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import club.hack.painbox.Main;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.google.gwt.core.client.GWT; // Not sure if this is the right import
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig () {
        GwtApplicationConfiguration config = new GwtApplicationConfiguration(800, 500);
        return config;
    }

    @Override
    public ApplicationListener createApplicationListener () {
        return new Main();
    }

    @Override
    public Preloader.PreloaderCallback getPreloaderCallback() {
        return createPreloaderPanel(GWT.getHostPageBaseURL() + "preloadlogo.png");
    }

    @Override
    protected void adjustMeterPanel(Panel meterPanel, Style meterStyle) {
        meterPanel.setStyleName("gdx-meter");
        meterPanel.addStyleName("nostripes");
        meterStyle.setProperty("backgroundColor", "#ffffff");
        meterStyle.setProperty("backgroundImage", "none");
    }

    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        Document d = Document.get();
        d.setTitle("10Wave");
        BodyElement body = d.getBody();
        Element explanation = d.createElement("p");
        explanation.setInnerHTML("Clear 10 waves of enemies as fast as possible! <strong>The current record is 36 seconds.</strong> <br> Use A and D to move, W and S to switch gravity (but you barely need to do that), and the arrow keys to shoot.<br><br>Shoot as much as you want, but be careful not to get hit by your own bullets...<br><br><strong>Please enable autoplay in your browser settings to hear the amazing music!</strong><br><br><br><strong>Credits</strong><br>Programming, design, updating: Gabe (GH @Archonic944)<br>Music, SFX, Art: Toft (Spotify @whoistoft)<br>Art: Cindy (???)<br>Design, Art: Luke (???)<br><strong>Created for Counterspell 2024</strong>");
        //insert paragraph after the canvas
        body.insertAfter(explanation, getRootPanel().getElement());
    }
}
