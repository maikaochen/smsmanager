package chen.smsmanager;

import chen.smsother.Sms;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.widget.ImageView;
import android.widget.TextView;

public class SmsDetailActivity extends Activity {

	
	private ImageView header;
	private TextView tv_name;
	private TextView tv_number;
	private TextView tv_type;
	private TextView tv_date;
	private TextView tv_body;
	
	private String _id;
	
	private QueryHandler mQueryHandler;
	
	private final static String[] SMS_PROJECTION = new String[]{Sms._ID,Sms.ADDRESS,Sms.DATE,Sms.BODY,Sms.TYPE};
	private final static int ID_COLUMN_INDEX = 0;
	private final static int ADDRESS_COLUMN_INDEX = 1;
	private final static int DATE_COLUMN_INDEX = 2;
	private final static int BODY_COLUMN_INDEX = 3;
	private final static int TYPE_COLUMN_INDEX = 4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sms_detail);
		
		_id = getIntent().getStringExtra("_id");
		
		header = (ImageView) findViewById(R.id.header);
		
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_number = (TextView) findViewById(R.id.tv_number);
		tv_type = (TextView) findViewById(R.id.tv_type);
		tv_date = (TextView) findViewById(R.id.tv_date);
		tv_body = (TextView) findViewById(R.id.tv_body);
		
		mQueryHandler = new QueryHandler(getContentResolver());
		
		startQuery();
	}
	
	private void startQuery() {
		// TODO Auto-generated method stub
		Uri uri = Uri.withAppendedPath(Sms.CONTENT_URI, Uri.encode(_id));
		mQueryHandler.startQuery(0, null, uri, SMS_PROJECTION, null, null, null);
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
			showHeader(cursor);
		}
	}

	/**
	 * 显示信息
	 * @param cursor
	 */
	public void showHeader(Cursor cursor) {
		// TODO Auto-generated method stub
		if(cursor != null){
			if(cursor.moveToFirst()){
				String address = cursor.getString(ADDRESS_COLUMN_INDEX);
				int type = cursor.getInt(TYPE_COLUMN_INDEX);
				long date = cursor.getLong(DATE_COLUMN_INDEX);
				String body = cursor.getString(BODY_COLUMN_INDEX);
				
				//根据电话号码查询联系人
				String name = null;
				Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
				Cursor contact_cursor = getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
				if(contact_cursor.moveToFirst()){
					name = contact_cursor.getString(0);
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
				
				tv_type.setText(typeId);
				
				
				Time time = new Time();
				time.setToNow();
				time.hour = 0;
				time.minute = 0;
				time.second = 0;
				
				long firstSecondOfToday = time.toMillis(false);
				
				String dateStr = null;
				if((date - firstSecondOfToday > 0) && (date - firstSecondOfToday < DateUtils.DAY_IN_MILLIS)){
					//show time
					dateStr = DateFormat.getTimeFormat(this).format(date);
				}else{
					// show date
					dateStr = DateFormat.getDateFormat(this).format(date);
				}
				tv_date.setText(dateStr);
				
				tv_body.setText(body);
			}
			
			cursor.close();
		}
	}
	
}
