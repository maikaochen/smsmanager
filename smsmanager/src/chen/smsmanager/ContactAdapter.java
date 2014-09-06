package chen.smsmanager;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ContactAdapter extends CursorAdapter {
	
	private LayoutInflater mInflater;
	private Context context;
	
	private final static String[] CONTACT_PROJECTION = new String[]{ContactsContract.Contacts._ID,
		ContactsContract.Contacts.DISPLAY_NAME,
		ContactsContract.CommonDataKinds.Phone.NUMBER};
	private final static int DISPLAY_NAME_COLUMN_INDEX = 1;
	public final static int NUMBER_COLUMN_INDEX = 2;

	public ContactAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
		this.context = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = mInflater.inflate(R.layout.contact_item, null);
		ContactViews views = new ContactViews();
		views.tv_name = (TextView) view.findViewById(R.id.tv_name);
		views.tv_number = (TextView) view.findViewById(R.id.tv_number);
		
		view.setTag(views);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
        ContactViews views = (ContactViews) view.getTag();
        
        String name = cursor.getString(DISPLAY_NAME_COLUMN_INDEX);
        String number = cursor.getString(NUMBER_COLUMN_INDEX);
        
        
        views.tv_name.setText(name);
        views.tv_number.setText(number);
        
	}

	
	
	
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		// TODO Auto-generated method stub
		if(TextUtils.isEmpty(constraint)){
			return null;
		}
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		//select * from sms where number like '%1%'
		String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " like '%" + constraint + "%'";
		Cursor c = context.getContentResolver().query(uri, CONTACT_PROJECTION, selection, null, null);
		return c;
	}




	private final class ContactViews{
		TextView tv_name;
		TextView tv_number;
	}
}
