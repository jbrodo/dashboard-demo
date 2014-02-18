package com.vaadin.demo.dashboard.pbthread;

import it.unimib.disco.essere.analyzer.build.maven.util.ArtifactValidator;
import it.unimib.disco.essere.analyzer.build.maven.util.MavenPathRetrieve;
import it.unimib.disco.essere.serial.searching.Repository;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;

public class DownloadThread extends Thread {
	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	final ProgressBar _indicator;
	final Button _button;
	final Repository _r;
	final String _checkout ;
	final Tree _tree;


	public DownloadThread(final ProgressBar indicator, final Button button, final Repository r, final String checkout, final Tree tree){
		_indicator=indicator;
		_button=button;
		_r=r;
		_checkout=checkout;
		_tree = tree;
	}

	@Override
	public void run() {

		current= 0.5;
		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_indicator.setValue(new Float(current));
			}
		});

		final boolean esitopositivo = DataProvider.doDownload(_r, _checkout); 
		current= 1.0;
		final MavenPathRetrieve m = DataProvider.getMavenPathRetrieve();

		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_indicator.setValue(new Float(current));
			}
		});

		// Show the "all done" for a while
		try {
			sleep(2000); // Sleep for 2 seconds
		} catch (InterruptedException e) {}
		
		if(esitopositivo){
			for(ArtifactValidator p:m._artifactHierarchy.keySet()){
				String s = p._model.toString();
				_tree.addItem(s);
				for(ArtifactValidator a:m._artifactHierarchy.get(p)){
					if(a!=null){
						String f = a._model.toString();
						_tree.addItem(f);
					}

				}
			}
			for(ArtifactValidator p:m._artifactHierarchy.keySet()){
				String s = p._model.toString();
				for(ArtifactValidator a:m._artifactHierarchy.get(p)){
					if(a!=null){
						String f = a._model.toString();
						_tree.setParent(f, s);
					}
				}
			}
			
		}
		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				// Restore the state to initial
				_indicator.setValue(new Float(0.0));
				_indicator.setEnabled(false);
				_indicator.setVisible(false);
				// Stop polling
				UI.getCurrent().setPollInterval(-1);
				_button.setEnabled(true);

				
				_tree.setVisible(true);
				//				//aggiungi tree
				//				_tree.addItem("root");
				//				_tree.addItem("item1");
				//				_tree.addItem("item2");
				//				_tree.addItem("item3");
				//				_tree.addItem("item4");
				//				_tree.addItem("item5");
				//				_tree.addItem("item6");
				//				_tree.setParent("item1","root");
				//				_tree.setParent("item2","root");
				//				_tree.setParent("item3","root");
				//				_tree.setParent("item4","item3");
				//				_tree.setParent("item6","item3");
				//				_tree.setParent("item5","item2");
				//				_tree.setVisible(true);
				Notification.show("Terminato Download");
			}
		});
	}
}
