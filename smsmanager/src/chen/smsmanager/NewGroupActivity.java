package chen.smsmanager;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import chen.smsother.Groups;

public class NewGroupActivity extends Activity implements OnClickListener{

	
	private EditText et_group_name;
	private Button bt_new_group;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.new_group);
		
		et_group_name = (EditText) findViewById(R.id.et_group_name);
		bt_new_group = (Button) findViewById(R.id.bt_new_group);
		
		bt_new_group.setOnClickListener(this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		String group_name = et_group_name.getText().toString();
		
		if("".equals(group_name)){
			Toast.makeText(this, R.string.group_name_is_null, 1).show();
			return;
		}
		
		//把群组数据插入
		/**
		 * 1 先判断插入的群组是否存在，存在就提示用户，不存在就插入
		 */
		Uri uri= Groups.CONTENT_URI;
		String selection = Groups.GROUP_NAME + " = ?";
		String[] selectionArgs = new String[]{group_name};
		Cursor group_cursor = getContentResolver().query(uri, null, selection, selectionArgs, null);
		if(group_cursor.moveToFirst()){
			Toast.makeText(this, R.string.exist_group, 1).show();
			group_cursor.close();
			return ;
		}else{
			ContentValues values = new ContentValues();
			values.put(Groups.GROUP_NAME, group_name);
			getContentResolver().insert(uri, values);
			Toast.makeText(this, R.string.success_create_group, 1).show();
			group_cursor.close();
			finish();
		}
	}
}
