package chen.smsmanager;

import chen.smsother.Sms;
import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "chen.smsmanager.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    
    private final static String[] sms_projection = new String[]{Sms._ID,Sms.ADDRESS,Sms.BODY};
    
    private final static String[] columnNames = new String[]{BaseColumns._ID,
    	SearchManager.SUGGEST_COLUMN_TEXT_1,
    	SearchManager.SUGGEST_COLUMN_TEXT_2,
    	SearchManager.SUGGEST_COLUMN_QUERY};

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
    		String[] selectionArgs, String sortOrder) {
    	// TODO Auto-generated method stub
    	Log.i("i", "query  ...");
    	
    	if(selectionArgs != null){
        	String query = selectionArgs[0];
        	
        	if(TextUtils.isEmpty(query)){
        		return null;
        	}
        	
        	Uri uri1 = Sms.CONTENT_URI;
        	String where = Sms.BODY + " like '%" + query + "%'";
        	Cursor cursor = getContext().getContentResolver().query(uri1, sms_projection, where, null, Sms.DATE + " desc ");
        	return changeCursor(cursor);
    	}
        return null;
    }
    
    
    private Cursor changeCursor(Cursor cursor){
    	MatrixCursor result = new MatrixCursor(columnNames);
    	if(cursor != null){
    		while(cursor.moveToNext()){
    			Object[] columnValues = new Object[]{cursor.getString(cursor.getColumnIndex(Sms._ID)),
    					cursor.getString(cursor.getColumnIndex(Sms.ADDRESS)),
    					cursor.getString(cursor.getColumnIndex(Sms.BODY)),
    					cursor.getString(cursor.getColumnIndex(Sms.BODY))};
    			result.addRow(columnValues);
    		}
    	}
    	return result;
    }
}
