package no.uib.ii.algo.st8;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Main extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startActivity(new Intent(this, Workspace.class));
	}
}
