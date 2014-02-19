package com.vaadin.demo.dashboard.pbthread;

import it.unimib.disco.essere.analyzer.build.maven.util.ArtifactValidator;
import it.unimib.disco.essere.analyzer.build.maven.util.MavenPathRetrieve;
import it.unimib.disco.essere.serial.searching.Repository;

import com.vaadin.demo.dashboard.DashboardUI;
import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;

public class DownloadThread extends Thread{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8928749412639224868L;

	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	final ProgressBar _downloadIndicator;
	final ProgressBar _mavenIndicator;
	final Button _downloadButton;
	//final Label _mavenLabel;
	final Repository _r;
	final String _checkout ;
	final Tree _tree;
	final ProgressBar _analysisIndicator;
	Boolean _ready = new Boolean(false);


	public DownloadThread(
			final ProgressBar downloadIndicator,
			final Button downloadButton,
			final ProgressBar mavenIndicator,
//			final Label mavenLabel,
			final Repository r,
			final String checkout,
			final Tree tree,
			final ProgressBar analysisIndicator){
		_downloadIndicator=downloadIndicator;
		_downloadButton=downloadButton;
		_r=r;
		_checkout=checkout;
		_tree = tree;
		_mavenIndicator = mavenIndicator;
		_analysisIndicator = analysisIndicator;
		//_mavenLabel = mavenLabel;
	}

	MavenPathRetrieve m = null;

	@Override
	public void run() {
		current= 0.5;
		
		
		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_downloadIndicator.setValue(new Float(current));
			}
		});
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_analysisIndicator.setValue(new Float(_analysisIndicator.getValue()+0.16));
			}
		});
		
		
		final boolean esitopositivo = DataProvider.doDownload(_r, _checkout); 
		current= 1.0;
		m = DataProvider.getMavenPathRetrieve();

		
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_downloadIndicator.setValue(new Float(current));
			}
		});
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_analysisIndicator.setValue(new Float(_analysisIndicator.getValue()+0.16));
			}
		});

		
		if(esitopositivo){
			for(ArtifactValidator p:m._artifactHierarchy.keySet()){
				String s = p._model.toString();
				_tree.addItem(s);
				if(m._artifactHierarchy.get(p).size()==0){
					_tree.setChildrenAllowed(s, false);
				}else{
					for(ArtifactValidator a:m._artifactHierarchy.get(p)){
						if(a!=null){
							String f = a._model.toString();
							_tree.addItem(f);
							_tree.setParent(f, s);
							// Make the f look like leaves.
							_tree.setChildrenAllowed(f, false);
						}
					}
					_tree.expandItemsRecursively(s);
				}
			}
			if(m._artifactHierarchy.keySet().size()==0){
				_tree.setCaption("There are no poms");
			}
		}else{
			_tree.setCaption("Download Impossible");
		}
		
		
		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable(){
			@Override
			public void run() {
				// Restore the state to initial
				_downloadIndicator.setValue(new Float(0.0));
				_downloadIndicator.setEnabled(false);
				_downloadIndicator.setVisible(false);
				// Stop polling
				UI.getCurrent().setPollInterval(-1);
				_downloadButton.setEnabled(true);

				if(m!=null){
					
					_tree.addItemClickListener(new ItemClickListener(){
						/**
						 * 
						 */
						private static final long serialVersionUID = 3491963846745582444L;

						@Override
						public void itemClick(final ItemClickEvent eventClickTree) {
							
							
							VerticalLayout l = new VerticalLayout();
							l.setWidth("400px");
							l.setMargin(true);
							l.setSpacing(true);
							final Window alert = new Window("Start maven build", l);
							alert.setModal(true);
							alert.setResizable(false);
							alert.setDraggable(false);
							alert.addStyleName("dialog");
							alert.setClosable(false);

							final String artifact = eventClickTree.getItemId().toString();
							Label message = new Label(
									"You have selected an artifact ("+artifact+"). Do you want run Maven on selected artifact?");
							l.addComponent(message);

							HorizontalLayout buttons = new HorizontalLayout();
							buttons.setWidth("100%");
							buttons.setSpacing(true);
							l.addComponent(buttons);

							Button cancel = new Button("Cancel");
							cancel.addStyleName("small");
							cancel.addClickListener(new ClickListener() {
								/**
								 * 
								 */
								private static final long serialVersionUID = 1126572341262073542L;

								@Override
								public void buttonClick(ClickEvent event) {
									alert.close();
								}
							});
							buttons.addComponent(cancel);

							final Button ok = new Button("Run maven");
							ok.addStyleName("default");
							ok.addStyleName("small");
							ok.addStyleName("wide");
							ok.addClickListener(new ClickListener() {
								/**
								 * 
								 */
								private static final long serialVersionUID = -8617041088191738400L;
								@Override
								public void buttonClick(ClickEvent event) {
									//setto la barra di maven attive
									
									final MavenBuildThread t = new MavenBuildThread(_mavenIndicator, artifact,_analysisIndicator);
									t.start();
									UI.getCurrent().setPollInterval(500);
									_mavenIndicator.setVisible(true);
									_mavenIndicator.setEnabled(true);
									alert.close();
									Notification
									.show("Maven build is run",
											"The maven job can be eavy and long than you have to wait a while.",
											Type.TRAY_NOTIFICATION);

								}
							});
							buttons.addComponent(ok);
							ok.focus();

							alert.addShortcutListener(new ShortcutListener("Cancel",
									KeyCode.ESCAPE, null) {
								/**
								 * 
								 */
								private static final long serialVersionUID = -2292479943080195118L;
								@Override
								public void handleAction(Object sender, Object target) {
									alert.close();
								}
							});
							UI.getCurrent().addWindow(alert);
							//getUI().addWindow(alert);
						}
					});
				}
				_tree.setVisible(true);
			}
		});
	}
}
