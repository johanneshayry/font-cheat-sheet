package org.vaadin;

import java.lang.reflect.Field;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.jsclipboard.JSClipboard;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.FontIcon;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 *
 */
@Theme("mytheme")
public class MyUI extends UI {

    static final String PROPERTY_NAME = "name";
    static final String PROPERTY_DESIGNER_URL = "iconUrl";
    static final String PROPERTY_HTML = "html";

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final AppDesign content = new AppDesign();
        setContent(content);

        content.iconDetailsForm.setVisible(false);

        BeanItemContainer<IconMeta> container = new BeanItemContainer<>(
                IconMeta.class);

        for (Field field : FontAwesome.class.getDeclaredFields()) {
            if (FontAwesome.class.isAssignableFrom(field.getType())) {
                try {
                    FontAwesome fontIcon = (FontAwesome) field.get(null);
                    container.addBean(
                            new FontAwesomeIconMeta(fontIcon, field.getName()));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        content.grid.setContainerDataSource(container);
        content.grid.setColumns(PROPERTY_HTML, PROPERTY_NAME,
                PROPERTY_DESIGNER_URL);
        content.grid.getColumn(PROPERTY_HTML).setRenderer(new HtmlRenderer());
        content.grid.setHeaderVisible(false);
        content.grid.setSelectionMode(SelectionMode.SINGLE);
        content.grid.addSelectionListener(new SelectionListener() {

            @Override
            public void select(SelectionEvent event) {
                if (!event.getSelected().isEmpty()) {
                    content.iconDetailsForm.setVisible(true);
                    IconMeta iconMeta = (IconMeta) event.getSelected()
                            .iterator().next();
                    content.iconHtml.setValue(iconMeta.getHtml());
                    content.iconName.setValue(iconMeta.getName());
                    JSClipboard clipName = new JSClipboard();
                    clipName.setText(iconMeta.getName());
                    clipName.apply(content.copyNameButton);
                    content.iconUrl.setValue(iconMeta.getIconUrl());
                    JSClipboard clipUrl = new JSClipboard();
                    clipUrl.setText(iconMeta.getIconUrl());
                    clipUrl.apply(content.copyUrlButton);
                } else {
                    content.iconDetailsForm.setVisible(false);
                }
            }
        });
    }

    public static abstract class IconMeta {
        protected final FontIcon icon;
        protected final String name;

        public IconMeta(FontIcon icon, String name) {
            this.icon = icon;
            this.name = name;
        }

        public FontIcon getIcon() {
            return icon;
        }

        public abstract String getIconUrl();

        public String getName() {
            return name;
        }

        public String getHtml() {
            return icon.getHtml();
        }
    }

    public static class FontAwesomeIconMeta extends IconMeta {

        public FontAwesomeIconMeta(FontIcon icon, String name) {
            super(icon, name);
        }

        @Override
        public String getIconUrl() {
            return "fonticon://FontAwesome/"
                    + Integer.toHexString(icon.getCodepoint());
        }
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
