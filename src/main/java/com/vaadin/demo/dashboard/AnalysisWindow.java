package com.vaadin.demo.dashboard;



import it.unimib.disco.essere.serial.searching.Repository;

import com.vaadin.demo.dashboard.pbthread.DownloadThread;
import com.vaadin.demo.dashboard.pbthread.ToolExecutionThread;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
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

	public AnalysisWindow(final Repository r, final String checkout, final ProgressBar analysisIndicator, final Button analyse) {
		VerticalLayout l = new VerticalLayout();
		l.setSpacing(true);
		//l.setHeight(null);
		l.setWidth("55em");
		
		//l.setSizeFull();
		
		
		setCaption("Analysis");
		setContent(l);
		center();
		setCloseShortcut(KeyCode.ESCAPE, null);
		setResizable(false);
		setScrollTop(5);
		setScrollLeft(5);
		setHeight(null);
		setClosable(false);

		addStyleName("no-vertical-drag-hints");
		addStyleName("no-horizontal-drag-hints");

		HorizontalLayout details = new HorizontalLayout();
		details.setSizeFull();
		details.setSpacing(true);
		details.setMargin(true);
		
		l.addComponent(details);

		FormLayout fields = new FormLayout();
		fields.setSizeFull();
		fields.setSpacing(true);
		fields.setMargin(true);
		
		details.addComponent(fields);

		HorizontalLayout downloadhl = new HorizontalLayout();
		downloadhl.setSpacing(true);
		downloadhl.setCaption("Download");
		downloadhl.setSizeFull();
		downloadhl.setMargin(true);
		fields.addComponent(downloadhl);

		final ProgressBar indicator = new ProgressBar(new Float(0.0));
		indicator.setVisible(false);
		indicator.setEnabled(false);
		indicator.setSizeFull();

		final Button download = new Button("Start download");
		download.addStyleName("default");
		downloadhl.addComponent(download);
		downloadhl.addComponent(indicator);

		final Tree tree = new Tree("Poms available");
		tree.setSizeFull();
		tree.setVisible(false);
		//aggiungo tree
		fields.addComponent(tree);

		//aggiungo maven indicator
		final ProgressBar mavenIndicator = new ProgressBar(new Float(0.0));
		mavenIndicator.setCaption("Maven building progress");
		mavenIndicator.setVisible(false);
		mavenIndicator.setEnabled(false);
		mavenIndicator.setSizeFull();
		fields.addComponent(mavenIndicator);
		
		HorizontalLayout toolhl = new HorizontalLayout();
		toolhl.setSpacing(true);
		//toolhl.setCaption("Tools");
		toolhl.setSizeFull();
		toolhl.setMargin(true);
		fields.addComponent(toolhl);
		
		final Button toolsexec = new Button("Analysis");
//		toolsexec.setCaption("Run Analysis");
		toolsexec.setVisible(false);
		toolsexec.setEnabled(false);
		toolhl.addComponent(toolsexec);
		final ProgressBar toolIndicator = new ProgressBar(new Float(0.0));
//		toolIndicator.setCaption("Software tool Analysis running");
		toolIndicator.setVisible(false);
		toolIndicator.setEnabled(false);
		toolIndicator.setSizeFull();
		toolhl.addComponent(toolIndicator);
		
		
		//download button have a listener
		download.addClickListener(new ClickListener(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -3883305257845654869L;
			@Override
			public void buttonClick(ClickEvent event) {

				if(checkout!=null){
					//Notification.show("Not implemented in this demo, selezionato:\n"+s);
					final DownloadThread t = new DownloadThread(
							indicator,download,mavenIndicator,r,checkout,
							tree, analysisIndicator,toolsexec, toolIndicator);
					t.start();
					UI.getCurrent().setPollInterval(500);
					indicator.setEnabled(true);
					indicator.setVisible(true);
					download.setEnabled(false);
				}else{
					Notification.show("Questo non dovrebbe mai essere visualizzato, perché il checkout è stato passato \"NULL\"");
				}
			}
		});

		
		toolsexec.addClickListener(new ClickListener(){

			/**
			 * 
			 */
			private static final long serialVersionUID = -5680161680249552116L;

			@Override
			public void buttonClick(ClickEvent event) {
				final ToolExecutionThread t = new ToolExecutionThread(analysisIndicator,toolsexec,toolIndicator,analyse);
				t.start();
				UI.getCurrent().setPollInterval(500);
				toolIndicator.setEnabled(true);
				toolIndicator.setVisible(true);
				toolsexec.setEnabled(false);
			}
			
		}) ;

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
