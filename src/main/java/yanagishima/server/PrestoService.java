package yanagishima.server;

import java.util.List;

public interface PrestoService {
	
	public List<String> doQuery(String query);

}
