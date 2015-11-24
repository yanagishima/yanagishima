package yanagishima.provider;

import me.geso.tinyorm.TinyORM;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;

public class TinyORMProvider implements Provider<TinyORM> {

	@Inject
	private Connection connection;

	@Override
	public TinyORM get() {
		return new TinyORM(connection);
	}
}
