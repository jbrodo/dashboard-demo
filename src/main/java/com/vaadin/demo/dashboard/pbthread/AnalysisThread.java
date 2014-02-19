package com.vaadin.demo.dashboard.pbthread;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;

public class AnalysisThread extends Thread {
	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	ProgressBar _indicator = null;
	Button _button = null;

	public AnalysisThread(ProgressBar indicator, Button button){
		_indicator=indicator;
		_button=button;
	}

	@Override
	public void run() {
		// Count up until 1.0 is reached
        while (current < 1.0) {
            current += 0.01;

            // Do some "heavy work"
            try {
                sleep(50); // Sleep for 50 milliseconds
            } catch (InterruptedException e) {}

            // Update the UI thread-safely
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                    _indicator.setValue(new Float(current));
                }
            });
        }
        
        // Show the "all done" for a while
        try {
            sleep(2000); // Sleep for 2 seconds
        } catch (InterruptedException e) {}

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
                //Notification.show("Terminated analysis","This is only a draft mode, nothing happens.",Type.TRAY_NOTIFICATION);
            }
        });
    }
}
