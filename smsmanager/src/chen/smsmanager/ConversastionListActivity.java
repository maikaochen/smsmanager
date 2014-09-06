package chen.smsmanager;

import java.util.ArrayList;

import chen.smsother.Sms;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConversastionListActivity extends Activity implements OnClickListener{

	private String thread_id;
	
	private ImageView header ;
	private TextView tv_name;
	private TextView tv_number;
	private ListView mListView;
	private EditText et_msg_content;
	private Button bt_send_msg;
	
	private QueryHandler mQueryHandler;
	private ConversationListAdapter mAdapter;
	private final static String[] SMS_PROJECTION = new String[]{Sms._ID,Sms.ADDRESS,Sms.BODY,Sms.TYPE,Sms.DATE};
	private final static int ID_COLUMN_INDEX = 0;
	private final static int ADDRESS_COLUMN_INDEX = 1;
	private final static int BODY_COLUMN_INDEX = 2;
	private final static int TYPE_COLUMN_INDEX = 3;
	private final static int DATE_COLUMN_INDEX = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation_list);
		
		thread_id = getIntent().getStringExtra("thread_id");
		
		initView();
		
		startQuery();
		
	}

	private void startQuery() {
		// TODO Auto-generated method stub
		Uri uri = Sms.CONTENT_URI;
		String selection = Sms.THREAD_ID + " = ?";
		String[] selectionArgs = new String[]{thread_id};
		mQueryHandler.startQuery(0, null, uri, SMS_PROJECTION, selection, selectionArgs, null);
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		// TODO Auto-generated method stub
		header = (ImageView) findViewById(R.id.header);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_number = (TextView) findViewById(R.id.tv_number);
		mListView = (ListView) findViewById(R.id.listview);
		et_msg_content = (EditText) findViewById(R.id.et_msg_content);
		bt_send_msg = (Button) findViewById(R.id.bt_send_msg);
		
		bt_send_msg.setOnClickListener(this);
		
		mQueryHandler = new QueryHandler(getContentResolver());
		mAdapter = new ConversationListAdapter(this, null);
		
		mListView.setAdapter(mAdapter);
	}
	
	private final class ConversationListViews{
		TextView tv_type;
		TextView tv_date;
		TextView tv_body;
	}
	
	private final class ConversationListAdapter extends CursorAdapter{
		
		private LayoutInflater mInflater;
		private long firstSecondOfToday;

		public ConversationListAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
			mInflater = LayoutInflater.from(context);
			
			Time time = new Time();
			time.setToNow();
			time.hour = 0;
			time.minute = 0;
			time.second = 0;
			firstSecondOfToday = time.toMillis(false);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = mInflater.inflate(R.layout.conversation_list_item, null);
			ConversationListViews views = new ConversationListViews();
			views.tv_type = (TextView) view.findViewById(R.id.tv_type);
			views.tv_date = (TextView) view.findViewById(R.id.tv_date);
			views.tv_body = (TextView) view.findViewById(R.id.tv_body);
			
			view.setTag(views);
			
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			//得到控件
			//得到数据
			//绑定数据
			
			ConversationListViews views = (ConversationListViews) view.getTag();
			
			int type = cursor.getInt(TYPE_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			int typeId = R.string.receive_at;
			switch (type) {
				case Sms.Inbox.TYPE:
					typeId = R.string.receive_at;
					break;
				case Sms.Sent.TYPE:
					typeId = R.string.send_at;
					break;
	
				default:
					break;
			}
			views.tv_type.setText(typeId);
			
			String dateStr = null;
			if((date - firstSecondOfToday > 0) && (date - firstSecondOfToday < DateUtils.DAY_IN_MILLIS)){
				//show time
				dateStr = DateFormat.getTimeFormat(context).format(date);
			}else{
				//show date
				dateStr = DateFormat.getDateFormat(context).format(date);
			}
			
			views.tv_date.setText(dateStr);
			
			views.tv_body.setText(body);
			
			
		}
		
	}
	
	private final class QueryHandler extends AsyncQueryHandler{

		public QueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			mAdapter.changeCursor(cursor);
			
			showHeader();
		}
	}

	private final static String[] CONTACT_PROJECTION = new String[]{PhoneLookup.DISPLAY_NAME};
	private final static int DISPLAY_NAME_COLUMN_INDEX = 0;

	private String address;
	/**
	 * 显示头信息
	 */
	public void showHeader() {
		// TODO Auto-generated method stub
		Cursor cursor = (Cursor) mAdapter.getItem(0);
		address = cursor.getString(ADDRESS_COLUMN_INDEX);
		//根据电话号码查询联系人
		String name = null;
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		Cursor contact_cursor = getContentResolver().query(uri, CONTACT_PROJECTION, null, null, null);
		if(contact_cursor.moveToFirst()){
			name = contact_cursor.getString(DISPLAY_NAME_COLUMN_INDEX);
		}
		contact_cursor.close();
		
		if(name != null){
			header.setImageResource(R.drawable.ic_contact_picture);
			tv_name.setText(name);
			tv_number.setText(address);
		}else{
			header.setImageResource(R.drawable.ic_unknown_picture_normal);
			tv_name.setText(address);
		}
	}
	
	
	/**
	 * 应该把cursor关闭
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Cursor cursor = mAdapter.getCursor();
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
			case R.id.bt_send_msg:
				String msg_content = et_msg_content.getText().toString();
				
				SmsManager smsManager = SmsManager.getDefault();
				
				ArrayList<String> parts = smsManager.divideMessage(msg_content);
				
				for(String part:parts){
					smsManager.sendTextMessage(address, null, part, null, null);
					Uri uri = Sms.Sent.CONTENT_URI;
					ContentValues values = new ContentValues();
					values.put(Sms.ADDRESS, address);
					values.put(Sms.BODY, part);
					getContentResolver().insert(uri, values);
				}
				Toast.makeText(this, R.string.success_send_sms, Toast.LENGTH_SHORT).show();
				
				break;
				
			default:
				break;
		}
		
	}
}
