package chen.smsother;

import android.net.Uri;

public class Sms {

	public final static Uri CONVERSATION_URI = Uri.parse("content://sms/conversations");
	public final static Uri CONTENT_URI = Uri.parse("content://sms");
	
	public final static String _ID = "_id";
	public final static String THREAD_ID = "thread_id";
	public final static String ADDRESS = "address";
	public final static String BODY = "body";
	public final static String DATE = "date";
	public final static String TYPE = "type";
	
	public final static class Inbox{
		public final static int TYPE = 1;
		public final static Uri CONTENT_URI = Uri.parse("content://sms/inbox");
	}
	/*
	public final static class Outbox{
		public final static int TYPE = 3;
		public final static Uri CONTENT_URI = Uri.parse("content://sms/outbox");
	}
	*/
	public final static class Sent{
		public final static int TYPE = 2;
		public final static Uri CONTENT_URI = Uri.parse("content://sms/sent");
	}
	/*
	public final static class Draft{
		public final static int TYPE = 0;
		public final static Uri CONTENT_URI = Uri.parse("content://sms/draft");
	}
	*/
}
