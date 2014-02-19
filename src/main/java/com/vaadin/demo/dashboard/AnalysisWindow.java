package com.vaadin.demo.dashboard;



import it.unimib.disco.essere.serial.searching.Repository;

import java.util.Iterator;

import com.google.common.collect.Multimap;
import com.vaadin.demo.dashboard.pbthread.AnalysisThread;
import com.vaadin.demo.dashboard.pbthread.DownloadThread;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AnalysisWindow extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5229945610717075518L;
	Label synopsis = new Label();

	public AnalysisWindow(final Repository r, final String checkout) {
		VerticalLayout l = new VerticalLayout();
		l.setSpacing(true);
		//l.setHeight(null);
		l.setWidth("35em");
		
		
		setCaption("Analysis");
		setContent(l);
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(false);
		setScrollTop(10);
		setScrollLeft(10);
		setClosable(false);

		addStyleName("no-vertical-drag-hints");
		addStyleName("no-horizontal-drag-hints");
 
		
		HorizontalLayout downloadhl = new HorizontalLayout();
		downloadhl.setSpacing(true);
		downloadhl.setSizeFull();
		downloadhl.setMargin(true);
		final ProgressBar indicator = new ProgressBar(new Float(0.0));
		indicator.setVisible(false);
		indicator.setEnabled(false);
		indicator.setSizeFull();
		
		final Button download = new Button("Start download");
		download.addStyleName("default");
		final Tree tree = new Tree("Poms available");		
		download.addClickListener(new ClickListener(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -3883305257845654869L;
			@Override
			public void buttonClick(ClickEvent event) {
				
				if(checkout!=null){
					//Notification.show("Not implemented in this demo, selezionato:\n"+s);
					final DownloadThread t = new DownloadThread(indicator, download,r, checkout, tree);
					t.start();
					UI.getCurrent().setPollInterval(500);
					indicator.setEnabled(true);
					indicator.setVisible(true);
					download.setEnabled(false);
					tree.setVisible(false);
					
				}else{
					Notification.show("Questo non dovrebbe mai essere visualizzato, perché il checkout è stato passato \"NULL\"");
				}
			}
		});
		downloadhl.addComponent(download);
		downloadhl.addComponent(indicator);
		l.addComponent(downloadhl);
		
		HorizontalLayout details = new HorizontalLayout();
		details.setSpacing(true);
		details.setMargin(true);
		l.addComponent(details);
		
		
		//aggiungo tree
		details.addComponent(tree);
		
		
		HorizontalLayout footer = new HorizontalLayout();
		footer.addStyleName("footer");
		footer.setSizeFull();//Width("100%");
		footer.setMargin(true);

		Button close = new Button("Close");
		close.addStyleName("wide");
		close.addStyleName("default");
		close.addClickListener(new ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6383016597762565727L;

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		footer.addComponent(close);
		footer.setComponentAlignment(close, Alignment.TOP_RIGHT);
		l.addComponent(footer);
	}
}