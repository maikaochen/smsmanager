package chen.smsmanager;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import chen.smsother.Sms;

public class NewMessageActivity extends Activity implements OnClickListener{

	private AutoCompleteTextView et_number;
	private EditText et_msg_content;
	private Button bt_send;
	
	private ContactAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_message);
		
		et_number = (AutoCompleteTextView) findViewById(R.id.et_number);
		et_msg_content = (EditText) findViewById(R.id.et_msg_content);
		bt_send = (Button) findViewById(R.id.bt_send_msg);
		
		//设置文字改变监听
		et_msg_content.addTextChangedListener(new MyTextWatcher());
		
		bt_send.setOnClickListener(this);
		
		mAdapter = new ContactAdapter(this, null);
		
		et_number.setAdapter(mAdapter);
		
		et_number.setOnItemClickListener(new MyOnItemClickListener());
	}
	
	
	private final class MyOnItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Cursor cursor = (Cursor) mAdapter.getItem(position);
			String number = cursor.getString(ContactAdapter.NUMBER_COLUMN_INDEX);
			et_number.setText(number);
		}
		
	}

	private final class MyTextWatcher implements TextWatcher{

		//改变之前
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		//已经改变
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}

		//改变之后
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			int length = s.toString().getBytes().length;
			//SmsMessage.MAX_USER_DATA_BYTES 一条短信的长度
			if(length > 8){
				Toast.makeText(getApplicationContext(), " too long", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String number = et_number.getText().toString();
		String msg_content = et_msg_content.getText().toString();
		
		SmsManager smsManager = SmsManager.getDefault();
		
		ArrayList<String> parts = smsManager.divideMessage(msg_content);
		
		for(String part:parts){
			smsManager.sendTextMessage(number, null, part, null, null);
			Uri uri = Sms.Sent.CONTENT_URI;
			ContentValues values = new ContentValues();
			values.put(Sms.ADDRESS, number);
			values.put(Sms.BODY, part);
			getContentResolver().insert(uri, values);
		}
		Toast.makeText(this, R.string.success_send_sms, Toast.LENGTH_SHORT).show();
		finish();
	}
}
