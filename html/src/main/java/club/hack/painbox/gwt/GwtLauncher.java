package club.hack.painbox.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import club.hack.painbox.Main;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.google.gwt.core.client.GWT; // Not sure if this is the right import
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Panel;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
        @Override
        public GwtApplicationConfiguration getConfig () {
            return new GwtApplicationConfiguration(800, 500);
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

}
