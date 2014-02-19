package com.vaadin.demo.dashboard.pbthread;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

public class ToolExecutionThread extends Thread {
	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	ProgressBar _indicator = null;
	Button _button = null;
	boolean _done = false;
	public ToolExecutionThread(ProgressBar indicator, Button button){
		_indicator=indicator;
		_button=button;
	}

	@Override
	public void run() {
		// Count up until 1.0 is reached
        
            current = 0.5;
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                    _indicator.setValue(new Float(current));
                }
            });
            // Do some "heavy work"
            _done=DataProvider.doRunTool();
            current = 1.0;
            // Update the UI thread-safely
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                    _indicator.setValue(new Float(current));
                }
            });
        
       

        // Update the UI thread-safely
        UI.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                // Restore the state to initial
                _indicator.setValue(new Float(0.0));
                _indicator.setEnabled(false);
                // Stop polling
                UI.getCurrent().setPollInterval(-1);
                
                _button.setEnabled(true);
                //Notification.show("Terminated analysis","This is only a draft mode, nothing happens.",Type.TRAY_NOTIFICATION);
            }
        });
    }
}
