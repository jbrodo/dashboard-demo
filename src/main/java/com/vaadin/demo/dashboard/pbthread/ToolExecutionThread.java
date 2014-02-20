package com.vaadin.demo.dashboard.pbthread;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;

public class ToolExecutionThread extends Thread {
	// Volatile because read in another thread in access()
	volatile double current = 0.0;

	final ProgressBar _analysisIndicator;
	final ProgressBar _toolIndicator;
	final Button _button;
	final Button _analyse;
	boolean _done = false;
	public ToolExecutionThread(final ProgressBar analysiIndicator, final Button button, final ProgressBar toolIndicator, final Button analyse){
		_analysisIndicator=analysiIndicator;
		_button=button;
		_toolIndicator=toolIndicator;
		_analyse=analyse;
	}

	@Override
	public void run() {
		// Count up until 1.0 is reached
        
            current = 0.5;
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                    _toolIndicator.setValue(new Float(current));
                }
            });
            UI.getCurrent().access(new Runnable() {
    			@Override
    			public void run() {
    				_analysisIndicator.setValue(new Float(_analysisIndicator.getValue()+0.16));
    			}
    		});
            // Do some "heavy work"
            _done=DataProvider.doRunTool();
            current = 1.0;
            // Update the UI thread-safely
            UI.getCurrent().access(new Runnable() {
                @Override
                public void run() {
                    _toolIndicator.setValue(new Float(current));
                }
            });
            UI.getCurrent().access(new Runnable() {
    			@Override
    			public void run() {
    				float i = 1.0f - _analysisIndicator.getValue();
    				_analysisIndicator.setValue(new Float(_analysisIndicator.getValue()+i));
    			}
    		});
        
       

        // Update the UI thread-safely
        UI.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                // Restore the state to initial
//                _toolIndicator.setValue(new Float(0.0));
//                _toolIndicator.setEnabled(false);
                // Stop polling
                UI.getCurrent().setPollInterval(-1);
                
                _button.setEnabled(true);
                _analyse.setEnabled(true);
                Notification.show("Terminated analysis","This is only a draft mode, DFMC4J have done is job.",Type.TRAY_NOTIFICATION);
            }
        });
    }
}
