package com.vaadin.demo.dashboard.pbthread;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;

public class MavenBuildThread extends Thread {
	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	final ProgressBar _indicator;
	final String _artifact;
	Boolean _done = new Boolean(true); 
	final ProgressBar _analysisIndicator;
	final ProgressBar _toolIndicator;
	final Button _toolsexec;
	public MavenBuildThread(
			final ProgressBar indicator,
			final String artifact,
			final ProgressBar analysisIndicator,
			final Button toolsexec,
			final ProgressBar toolIndicator){
		_indicator=indicator;
		_artifact=artifact;
		_analysisIndicator=analysisIndicator;
		_toolIndicator=toolIndicator;
		_toolsexec=toolsexec;
	}

	@Override
	public void run() {
		// Count up until 1.0 is reached
		current = 0.5;
		
		
		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_indicator.setValue(new Float(current));
			}
		});
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_analysisIndicator.setValue(new Float(_analysisIndicator.getValue()+0.16));
			}
		});
		
		
		// Do some "heavy work"
		_done=DataProvider.doMavenBuild(_artifact);
		current = 1.0;
		
		
		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_indicator.setValue(new Float(current));
			}
		});
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				_analysisIndicator.setValue(new Float(_analysisIndicator.getValue()+0.16));
			}
		});

		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable() {
			@Override
			public void run() {
				// Restore the state to initial
				_indicator.setCaption("Maven building done: "+_done);
				// Stop polling
				UI.getCurrent().setPollInterval(-1);
				if(_done){
					_toolsexec.setEnabled(true);
					_toolsexec.setVisible(true);
					
					Notification.show("Maven artifact building ("+_artifact+") is done","The maven projec is builded",Type.TRAY_NOTIFICATION);
				}else{
					Notification.show("Maven artifact building ("+_artifact+") is NOT done","The maven projec is not builded",Type.TRAY_NOTIFICATION);
				}
			}
		});
	}
}
