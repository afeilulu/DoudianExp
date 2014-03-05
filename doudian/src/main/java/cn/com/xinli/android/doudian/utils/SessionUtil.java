package cn.com.xinli.android.doudian.utils;

import java.util.HashMap;
import java.util.Map;


public class SessionUtil {
	@SuppressWarnings("unchecked")

	private Map _objectContainer;
    private static SessionUtil session;

	@SuppressWarnings("unchecked")

	private SessionUtil(){
     _objectContainer = new HashMap();

	}

	public static SessionUtil getSession(){
      if(session == null){
          session = new SessionUtil();
             return session;
      }else{
             return session;
      }
}



	@SuppressWarnings("unchecked")
    public void put(Object key, Object value){
        _objectContainer.put(key, value);
    }

	public Object get(Object key){
        return _objectContainer.get(key);

	}

	public void cleanUpSession(){
        _objectContainer.clear();
    }

	public void remove(Object key){

	    _objectContainer.remove(key);

	}


}
