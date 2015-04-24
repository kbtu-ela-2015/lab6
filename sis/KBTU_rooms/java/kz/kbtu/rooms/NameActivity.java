package kz.kbtu.rooms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import info.androidhive.webgroupchat.R;

public class NameActivity extends Activity {

	private Button btnJoin;
	private EditText txtName;
    private LinearLayout linearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_name);

		btnJoin = (Button) findViewById(R.id.btnJoin);
		txtName = (EditText) findViewById(R.id.name);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
		getActionBar().hide();

		btnJoin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (txtName.getText().toString().trim().length() > 0) {

                    View view = getLayoutInflater().inflate(R.layout.dialog_choose_room, null, false);

                    View.OnClickListener doc = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String room=null;
                            switch (view.getId()){
                                case R.id.buttonFIT:
                                    room = "FIT";   break;
                                case R.id.buttonFOGI:
                                    room = "FOGI";  break;
                                case R.id.buttonBS:
                                    room = "BS";    break;
                                case R.id.buttonISE:
                                    room = "ISE";   break;
                            }
                            String name = txtName.getText().toString().trim();

                            Intent intent = new Intent(NameActivity.this,
                                    MainActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("room",room);
                            startActivity(intent);
                        }
                    };
                    view.findViewById(R.id.buttonFIT).setOnClickListener(doc);
                    view.findViewById(R.id.buttonFOGI).setOnClickListener(doc);
                    view.findViewById(R.id.buttonBS).setOnClickListener(doc);
                    view.findViewById(R.id.buttonISE).setOnClickListener(doc);
                    linearLayout.removeViewAt(0);
                    linearLayout.removeViewAt(0);
                    linearLayout.removeViewAt(0);
                    TextView tv = new TextView(NameActivity.this);
                    tv.setGravity(Gravity.CENTER);
                    tv.setText(getResources().getString(R.string.choose_room));
                    tv.setTextColor(getResources().getColor(R.color.white));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    linearLayout.addView(tv);
                    linearLayout.addView(view);

				} else {
					Toast.makeText(getApplicationContext(),
							"Please enter your name", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
